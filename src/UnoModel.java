import java.util.*;

/**
 * Main model class for the Uno game. This class manages all core game state.
 * @author Danilo Bukvic Ajan Balaganesh Aydan Eng Aws Ali
 * @version 1.0
 */
public class UnoModel {

    private final List<UnoView> views = new ArrayList<>();
    private final List<UnoPlayer> players = new ArrayList<>();
    private final Deque<UnoCard> drawPile = new ArrayDeque<>();
    private final Deque<UnoCard> discard = new ArrayDeque<>();

    private int current = 0;
    private int gameDirection = +1;
    private boolean mustPressNext = false;
    private int cardsToDraw = 0;       // penalty for DRAW_TWO
    private UnoColor activeColor = UnoColor.NONE; // chosen after WILD
    private String info = "Welcome to Uno!";
    private int nextSteps = 1;

    /**
     * Constructor for UnoModel.
     *
     * @param numPlayers Number of players (2-4).
     * @param names      List of player names.
     */
    public UnoModel(int numPlayers, List<String> names) {
        if (numPlayers < 2 || numPlayers > 4) {
            throw new IllegalArgumentException("Number of players must be 2–4.");
        }

        // Create however many players
        for (int i = 0; i < numPlayers; i++) {
            players.add(new UnoPlayer(names.get(i)));
        }

        // Build and shuffle the deck
        List<UnoCard> deck = buildDeck();
        Collections.shuffle(deck, new Random());
        deck.forEach(drawPile::push);

        // Deal 7 cards to each player
        for (int k = 0; k < 7; k++) {
            for (UnoPlayer p : players) {
                p.hand.add(drawPile.pop());
            }
        }

        UnoCard first = drawPile.pop();
        discard.push(first);
        activeColor = (first.color == UnoColor.WILD ? UnoColor.NONE : first.color);
        info = "First card on top is " + first.toText() + ". " + currentPlayerName() + ", its your turn.";
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
        List<UnoCard> handCopy = new ArrayList<>(players.get(current).hand);
        UnoCard top = discard.peek();
        assert top != null;
        String topText = top.toText() + (activeColor != UnoColor.NONE ? " [" + activeColor + "]" : "");
        String currName = currentPlayerName();

        // Create the event object
        UnoEvent event = new UnoEvent(
                this,
                handCopy,
                topText,
                currName,
                info,
                mustPressNext,
                activeColor
        );

        for (UnoView v : views) {
            v.handleUpdate(event);
        }
    }

    private void broadcastEnd(String message) {
        for (UnoView v : views) v.handleEnd(message);
    }

    /**
     * Peeks at a card in the current player's hand without playing it.
     *
     * @param handIndex Index of card.
     * @return The UnoCard object or null if index invalid.
     */
    public UnoCard peekCardInHand(int handIndex) {
        List<UnoCard> h = players.get(current).hand;
        if (handIndex < 0 || handIndex >= h.size()) return null;
        return h.get(handIndex);
    }

    /**
     * Helper to check if a specific card in the current player's hand is a Wild card.
     * Used by Controller to determine if a color prompt is needed.
     *
     * @param handIndex The index of the card in the hand.
     * @return True if the card is Wild, false otherwise.
     */
    public boolean isCardWild(int handIndex) {
        List<UnoCard> h = players.get(current).hand;
        if (handIndex < 0 || handIndex >= h.size()) return false;
        return h.get(handIndex).isWild();
    }

    /**
     * Plays a regular card from the hand.
     *
     * @param handIndex The index of the card to play.
     * @throws IllegalStateException if the move is invalid or it's not the time to play.
     */
    public void play(int handIndex) {
        ensureAwaitingAction();
        List<UnoCard> h = players.get(current).hand;
        UnoCard chosen = h.get(handIndex);
        UnoCard top = discard.peek();

        if (!chosen.matches(top, activeColor)) {
            throw new IllegalStateException("You cannot play " + chosen + " on " + top + ".");
        }

        // playing the card
        h.remove(handIndex);
        discard.push(chosen);
        activeColor = chosen.color;
        handleCardEffect(chosen, h);
    }

    /**
     * Plays a wild card from the hand with a selected color.
     *
     * @param handIndex   The index of the card.
     * @param chosenColor The color selected by the player.
     */
    public void playWild(int handIndex, UnoColor chosenColor) {
        ensureAwaitingAction();
        List<UnoCard> h = players.get(current).hand;
        UnoCard chosen = h.get(handIndex);

        // Play wild card
        h.remove(handIndex);
        discard.push(chosen);

        activeColor = chosenColor;
        finishPlay(h, " set color to " + activeColor + ". Press Next to continue.");
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
        info = currentPlayerName() + ", your turn.";
        notifyViews();
    }

    private void handleCardEffect(UnoCard chosen, List<UnoCard> currentHand) {
        switch (chosen.rank) {
            case REVERSE -> {
                gameDirection = -gameDirection;
                nextSteps = 1;
                info = "Direction reversed.";
            }
            case SKIP -> {
                nextSteps = 2;
                info = "Skip!! The next player is getting skipped.";
            }
            case DRAW_TWO -> {
                cardsToDraw = 2;
                int drawIndex = properIndex(current + gameDirection);
                for (int i = 0; i < cardsToDraw; i++) {
                    players.get(drawIndex).hand.add(popOrRecycle());
                }
                nextSteps = 2;
                info = "Draw Two! Next player will draw 2 and be skipped.";
            }
            default -> {
                nextSteps = 1;
            }
        }
        finishPlay(currentHand, " played " + chosen + ". Press Next to continue.");
    }

    private void finishPlay(List<UnoCard> currentHand, String tailMsg) {
        if (currentHand.isEmpty()) {
            info = currentPlayerName() + " won!";
            broadcastEnd(info);
            notifyViews();
            return;
        }

        mustPressNext = true;
        info = currentPlayerName() + tailMsg;
        notifyViews();
    }

    private UnoCard popOrRecycle() {
        if (drawPile.isEmpty()) {
            recycle();
        }

        return drawPile.pop();
    }

    private void recycle() {
        if (discard.isEmpty()) return;
        UnoCard top = discard.pop();
        List<UnoCard> back = new ArrayList<>(discard);
        discard.clear();
        discard.push(top);
        Collections.shuffle(back, new Random());

        for (UnoCard c : back) {
            drawPile.push(c);
        }
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

    private List<UnoCard> buildDeck() {
        List<UnoCard> deck = new ArrayList<>();

        // Regular colored cards
        for (UnoColor c : new UnoColor[]{UnoColor.RED, UnoColor.GREEN, UnoColor.BLUE, UnoColor.YELLOW}) {
            deck.add(new UnoCard(c, UnoRank.ZERO));

            UnoRank[] nums = {UnoRank.ONE, UnoRank.TWO, UnoRank.THREE, UnoRank.FOUR, UnoRank.FIVE, UnoRank.SIX, UnoRank.SEVEN, UnoRank.EIGHT, UnoRank.NINE};

            // Two of each 1-9 digit cards per color
            for (UnoRank r : nums) {
                deck.add(new UnoCard(c, r));
                deck.add(new UnoCard(c, r));
            }

            // Two of each action card both skip and reverse per colour
            deck.add(new UnoCard(c, UnoRank.SKIP));
            deck.add(new UnoCard(c, UnoRank.SKIP));
            deck.add(new UnoCard(c, UnoRank.REVERSE));
            deck.add(new UnoCard(c, UnoRank.REVERSE));
            deck.add(new UnoCard(c, UnoRank.DRAW_TWO));
            deck.add(new UnoCard(c, UnoRank.DRAW_TWO));
        }

        // Four wild cards
        for (int i = 0; i < 4; i++) {
            deck.add(new UnoCard(UnoColor.WILD, UnoRank.WILD));
        }

        return deck;
    }

    // For Testing Purposes
    public int getCurrentPlayerIndex() { return current; }
    public UnoCard getDiscardTop() { return discard.peek(); }
    public int getDrawPileSize() { return drawPile.size(); }
    public void setTopCard(UnoCard c) {
        discard.push(c);
        if (c.color != UnoColor.WILD) activeColor = c.color;
    }
    public void forceCurrentPlayerHand(List<UnoCard> cards) {
        players.get(current).hand.clear();
        players.get(current).hand.addAll(cards);
    }
}
