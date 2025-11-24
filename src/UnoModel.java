import java.util.*;
/**
 * Main model class for the Uno game. This class manages all core game state.
 * @author Danilo Bukvic Ajan Balaganesh Aydan Eng Aws Ali
 * @version 3.0
 */
public class UnoModel {
    private final List<UnoView> views = new ArrayList<>();
    private final List<UnoPlayer> players = new ArrayList<>();
    private final Deque<UnoCard> drawPile = new ArrayDeque<>();
    private final Deque<UnoCard> discard = new ArrayDeque<>();

    private int current = 0;
    private int gameDirection = 1;
    private boolean mustPressNext = false;
    private UnoColor activeColor = UnoColor.NONE; // chosen after WILD
    private String info = "Welcome to Uno!";
    private int nextSteps = 1;

    private boolean isDark = false;

    /**
     * Constructor for UnoModel.
     * @param numPlayers Number of players (2-4).
     * @param names List of player names.
     */
    public UnoModel(int numPlayers, List<String> names, List<Boolean> isAI) {
        if (numPlayers < 2 || numPlayers > 4) {
            throw new IllegalArgumentException("Number of players must be 2–4.");
        }

        // Create however many players
        for (int i = 0; i < numPlayers; i++) {
            players.add(new UnoPlayer(names.get(i), isAI.get(i)));
        }

        initializeDeck();

        // Deal 7 cards to each player
        for (int k = 0; k < 7; k++) {
            for (UnoPlayer p : players) {
                p.hand.add(drawPile.pop());
            }
        }
    }

    // Moved deck initialization to private helper method
    private void initializeDeck() {
        List<UnoCard> deck = buildFlipDeck();
        Collections.shuffle(deck, new Random());
        deck.forEach(drawPile::push);

        UnoCard first = drawPile.pop();
        discard.push(first);
        activeColor = first.getColor(isDark);

        // If first card is Wild-ish, pick a default color (simplify for start)
        if (first.isWild(isDark)) activeColor = isDark ? UnoColor.TEAL : UnoColor.RED;

        info = "First card: " + first.toText(isDark) + ". " + currentPlayerName() + ", start!";
    }

    /**
     * Adds a view to the model.
     * @param v The view to add.
     */
    public void addView(UnoView v) {
        views.add(v);
        notifyViews();
    }

    /**
     * Creates an UnoEvent and notifies all registered views.
     */
    private void notifyViews() {
        UnoPlayer p = players.get(current);
        List<UnoCard> handCopy = new ArrayList<>(p.hand);
        UnoCard top = discard.peek();
        String topText = top != null ? top.toText(isDark) : "None";
        if (activeColor != UnoColor.NONE) topText += " [" + activeColor + "]";

        UnoEvent event = new UnoEvent(this, handCopy, topText, p.name, info, mustPressNext, activeColor, isDark, p.isAI);
        for (UnoView v : views) v.handleUpdate(event);
    }

    /**
     * Peeks at a card in the current player's hand without playing it.
     * @param handIndex Index of card.
     * @return The UnoCard object or null if index invalid.
     */
    public UnoCard peekCardInHand(int handIndex) {
        if (handIndex >= 0 && handIndex < players.get(current).hand.size()) return players.get(current).hand.get(handIndex);
        return null;
    }

    /**
     * Helper to check if a specific card in the current player's hand is a Wild card.
     * Used by Controller to determine if a color prompt is needed.
     * @param handIndex The index of the card in the hand.
     * @return True if the card is Wild, false otherwise.
     */
    public boolean isCardWild(int handIndex) {
        List<UnoCard> h = players.get(current).hand;
        if (handIndex < 0 || handIndex >= h.size()) return false;
        return h.get(handIndex).isWild(isDark);
    }

    /**
     * Executes an AI Turn. Finds the best legal move and plays it.
     */
    public void playAITurn() {
        if (mustPressNext) {
            nextPlayer(); // AI just finishing turn
            return;
        }

        UnoPlayer ai = players.get(current);
        if (!ai.isAI) return;

        // AI Strategy:
        // 1. Can we win this turn? (Hand size 1 and matches)
        // 2. Play Action cards (Skip, Draw 5, etc.) to hurt opponents.
        // 3. Play matching color/rank.
        // 4. Play Wilds last to save them (unless only option).

        int bestIdx = -1;
        int wildIdx = -1;

        UnoCard top = discard.peek();

        for (int i = 0; i < ai.hand.size(); i++) {
            UnoCard c = ai.hand.get(i);
            if (c.matches(top, activeColor, isDark)) {
                if (c.isWild(isDark)) {
                    wildIdx = i;
                } else if (isActionCard(c)) {
                    bestIdx = i; // prioritize actions
                    break;
                } else {
                    if (bestIdx == -1) bestIdx = i; // standard match
                }
            }
        }

        if (bestIdx == -1 && wildIdx != -1) bestIdx = wildIdx; // Use wild if no other option

        if (bestIdx != -1) {
            // Play card
            if (isCardWild(bestIdx)) {
                // Pick random valid color
                UnoColor[] opts = isDark ?
                        new UnoColor[]{UnoColor.TEAL, UnoColor.PINK, UnoColor.PURPLE, UnoColor.ORANGE} :
                        new UnoColor[]{UnoColor.RED, UnoColor.BLUE, UnoColor.GREEN, UnoColor.YELLOW};
                playWild(bestIdx, opts[new Random().nextInt(opts.length)]);
            } else {
                play(bestIdx);
            }
        } else {
            draw();
        }
    }

    private boolean isActionCard(UnoCard c) {
        UnoRank r = c.getRank(isDark);
        return r == UnoRank.SKIP || r == UnoRank.REVERSE || r == UnoRank.DRAW_ONE ||
                r == UnoRank.DRAW_FIVE || r == UnoRank.SKIP_EVERYONE || r == UnoRank.FLIP;
    }

    /**
     * Plays a regular card from the hand.
     * @param handIndex The index of the card to play.
     * @throws IllegalStateException if the move is invalid, or it's not the time to play.
     */
    public void play(int handIndex) {
        ensureAwaitingAction();
        UnoPlayer p = players.get(current);
        UnoCard chosen = p.hand.get(handIndex);
        UnoCard top = discard.peek();

        if (!chosen.matches(top, activeColor, isDark)) {
            throw new IllegalStateException("Illegal move: " + chosen.toText(isDark));
        }

        p.hand.remove(handIndex);
        discard.push(chosen);
        activeColor = chosen.getColor(isDark); // Update active color naturally
        handleCardEffect(chosen, p.hand);
    }

    /**
     * Plays a wild card from the hand with a selected color.
     * @param handIndex   The index of the card.
     * @param chosenColor The color selected by the player.
     */
    public void playWild(int handIndex, UnoColor chosenColor) {
        ensureAwaitingAction();
        UnoPlayer p = players.get(current);
        UnoCard chosen = p.hand.get(handIndex);

        p.hand.remove(handIndex);
        discard.push(chosen);
        activeColor = chosenColor;

        handleCardEffect(chosen, p.hand);
    }

    /**
     * Draws a card from the deck for the current player.
     */
    public void draw() {
        ensureAwaitingAction();
        UnoPlayer p = players.get(current);
        p.hand.add(popOrRecycle());
        mustPressNext = true;
        info = p.name + " drew 1 card. Press Next to continue.";
        notifyViews();
    }

    /**
     * Advances the turn to the next player.
     * @throws IllegalStateException if the player hasn't played or drawn yet.
     */
    public void nextPlayer() {
        if (!mustPressNext) {
            throw new IllegalStateException("You must perform an action first.");
        }

        current = properIndex(current + gameDirection * nextSteps);
        mustPressNext = false;
        nextSteps = 1;
        info = currentPlayerName() + "'s turn.";
        notifyViews();
    }

    private void handleCardEffect(UnoCard chosen, List<UnoCard> currentHand) {
        UnoRank r = chosen.getRank(isDark);
        String msg = " played " + chosen.toText(isDark);
        switch (r) {
            case FLIP -> {
                isDark = !isDark;
                // Flip discard pile top, visually just the top card matters, but logically the whole deck flips
                // Don't actually rotate the list, we just toggle isDark.
                UnoCard top = discard.peek();
                assert top != null;
                activeColor = top.getColor(isDark); // Update active color to the new side of the same card
                if (top.isWild(isDark)) {
                    // If flipped onto a wild, we need a color
                    // Simplified to default color mapping
                    activeColor = isDark ? UnoColor.TEAL : UnoColor.RED;
                }
                msg += " FLIP!";
            }
            case DRAW_FIVE -> {
                int victim = properIndex(current + gameDirection);
                for(int i=0; i<5; i++) players.get(victim).hand.add(popOrRecycle());
                nextSteps = 2; // Skip them
                msg += " (Next draws 5 and skips)";
            }
            case SKIP_EVERYONE -> {
                nextSteps = 0;
                msg += " (Play again!)";
            }
            case WILD_DRAW_COLOR -> {
                // Next player draws until color match
                int victim = properIndex(current + gameDirection);
                UnoPlayer vp = players.get(victim);
                boolean found = false;
                int count = 0;
                while(!found) {
                    UnoCard c = popOrRecycle();
                    vp.hand.add(c);
                    count++;
                    assert c != null;
                    if (c.getColor(isDark) == activeColor) found = true;
                }
                nextSteps = 2; // They lose turn
                msg += " (Next drew " + count + " to find " + activeColor + ")";
            }
            case REVERSE -> {
                gameDirection = -gameDirection;
                nextSteps = 1;
                info = "Direction reversed.";
            }
            case SKIP -> {
                nextSteps = 2;
                info = "Skip! The next player is getting skipped.";
            }
            case DRAW_ONE -> {
                int count = 1;
                int victim = properIndex(current + gameDirection);
                for(int i=0; i<count; i++) players.get(victim).hand.add(popOrRecycle());
                nextSteps = 2;
                msg += " Draw 1";
            }
            case WILD_DRAW_TWO -> {
                int victim = properIndex(current + gameDirection);
                for(int i=0; i<2; i++) players.get(victim).hand.add(popOrRecycle());
                nextSteps = 2;
                msg += " Wild Draw 2";
            }
            default -> nextSteps = 1;
        }

        if (currentHand.isEmpty()) {
            info = players.get(current).name + " WON! Score: " + calculateScore();
            notifyViews();
            for(UnoView v : views) v.handleEnd(info);
            return;
        }

        mustPressNext = true;
        info = players.get(current).name + msg;
        notifyViews();
    }

    // Uno Flip scoring now kept
    private int calculateScore() {
        int score = 0;
        for (UnoPlayer p : players) {
            for (UnoCard c : p.hand) {
                UnoRank r = c.getRank(isDark);
                switch (r) {
                    case WILD_DRAW_COLOR -> score += 60;
                    case WILD_DRAW_TWO -> score += 50;
                    case WILD -> score += 40;
                    case DRAW_FIVE, FLIP, DRAW_ONE, SKIP, REVERSE-> score += 20;
                    case SKIP_EVERYONE -> score += 30;
                    default -> score += (r.ordinal() < 10 ? r.ordinal() : 0);
                }
            }
        }
        return score;
    }

    private UnoCard popOrRecycle() {
        if (drawPile.isEmpty()) {
            recycle();
        }
        return drawPile.isEmpty() ? null : drawPile.pop();
    }

    private void recycle() {
        if (discard.isEmpty()) return;
        UnoCard top = discard.pop();
        List<UnoCard> back = new ArrayList<>(discard);
        discard.clear();
        discard.push(top);
        Collections.shuffle(back);
        back.forEach(drawPile::push);
    }

    // Just a helper to deal with getting the proper player index
    private int properIndex(int idx) {
        int n = players.size();
        return ((idx % n) + n) % n;
    }

    private void ensureAwaitingAction() {
        if (mustPressNext) {
            throw new IllegalStateException("Press next to continue.");
        }
    }

    private String currentPlayerName() {
        return players.get(current).name;
    }

    private List<UnoCard> buildFlipDeck() {
        List<UnoCard> deck = new ArrayList<>();
        // Simple mapping for M3:
        // Light: Red <-> Dark: Orange
        // Light: Blue <-> Dark: Pink
        // Light: Green <-> Dark: Teal
        // Light: Yellow <-> Dark: Purple

        UnoColor[] lights = {UnoColor.RED, UnoColor.BLUE, UnoColor.GREEN, UnoColor.YELLOW};
        UnoColor[] darks = {UnoColor.ORANGE, UnoColor.PINK, UnoColor.TEAL, UnoColor.PURPLE};

        // Generate base numeric cards
        for (int i=0; i<4; i++) {
            UnoColor l = lights[i];
            UnoColor d = darks[i];

            // 1-9
            for (int n=1; n<=9; n++) {
                UnoRank lr = UnoRank.values()[n]; // ONE to NINE
                UnoRank dr = UnoRank.values()[n]; // Same rank dark side often
                deck.add(new UnoCard(l, lr, d, dr));
                deck.add(new UnoCard(l, lr, d, dr));
            }

            // Action Cards
            deck.add(new UnoCard(l, UnoRank.SKIP, d, UnoRank.SKIP_EVERYONE));
            deck.add(new UnoCard(l, UnoRank.REVERSE, d, UnoRank.REVERSE));
            deck.add(new UnoCard(l, UnoRank.DRAW_ONE, d, UnoRank.DRAW_FIVE)); // Light Draw 1 maps to Dark Draw 5

            // Flip Cards
            deck.add(new UnoCard(l, UnoRank.FLIP, d, UnoRank.FLIP));
            deck.add(new UnoCard(l, UnoRank.FLIP, d, UnoRank.FLIP));
        }

        // Wilds
        for(int i=0; i<4; i++) {
            // Light Wild -> Dark Wild Draw Color
            deck.add(new UnoCard(UnoColor.WILD, UnoRank.WILD, UnoColor.WILD, UnoRank.WILD_DRAW_COLOR));
            // Light Wild Draw 2 -> Dark Wild Draw Color
            deck.add(new UnoCard(UnoColor.WILD, UnoRank.WILD_DRAW_TWO, UnoColor.WILD, UnoRank.WILD_DRAW_COLOR));
        }

        return deck;
    }

    // Testing Helpers
    public void setTopCard(UnoCard c) { discard.push(c); activeColor = c.getColor(isDark); }
    public UnoCard getDiscardTop() { return discard.peek(); }
    public boolean isDark() { return isDark; }
    public void forceHand(int playerIdx, List<UnoCard> cards) { players.get(playerIdx).hand.clear(); players.get(playerIdx).hand.addAll(cards); }
}
