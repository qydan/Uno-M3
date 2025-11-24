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
    private int cardstoDraw = 0;       // penalty for DRAW_TWO
    private UnoColor activeColor = UnoColor.NONE; // chosen after WILD
    private String info = "Welcome to Uno!";
    private int nextSteps = 1;

    public UnoModel(int numPlayers, List<String> names) {
        if (numPlayers < 2 || numPlayers > 4) {
            throw new IllegalArgumentException("Number of players must be 2–4.");
        }

        //Create however many players
        for (int i = 0; i < numPlayers; i++) {
            players.add(new UnoPlayer(names.get(i)));
        }

        //build and shuffle the deck
        List<UnoCard> deck = buildDeck();
        Collections.shuffle(deck, new Random());
        deck.forEach(drawPile::push);

        // deal 7 cards to each player
        for (int k = 0; k < 7; k++) {
            for (UnoPlayer p : players) {
                p.hand.add(drawPile.pop());
            }
        }


        UnoCard first = drawPile.pop();
        discard.push(first);
        activeColor = (first.color == UnoColor.WILD ? UnoColor.NONE : first.color);
        info = "First catd on top is " + first.toText() + ". " + currentPlayerName() + ", its your turn.";
    }


    public void addView(UnoView v) {
        views.add(v);
        notifyViews();
    }

    private void notifyViews() {
        List<UnoCard> handCopy = new ArrayList<>(players.get(current).hand);
        UnoCard top = discard.peek();
        assert top != null;
        String topText = top.toText() + (activeColor != UnoColor.NONE ? " [" + activeColor + "]" : "");
        String currName = currentPlayerName();
        for (UnoView v : views) {
            v.handleUpdate(handCopy, topText, currName, info, mustPressNext);
        }
    }

    private void broadcastEnd(String message) {
        for (UnoView v : views) v.handleEnd(message);
    }

    public UnoCard peekCardInHand(int handIndex) {
        List<UnoCard> h = players.get(current).hand;
        if (handIndex < 0 || handIndex >= h.size()) return null;
        return h.get(handIndex);
    }

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
        handlecardeffect(chosen, h);
    }

    public void playWild(int handIndex, UnoColor chosenColor) {
        ensureAwaitingAction();
        List<UnoCard> h = players.get(current).hand;


        UnoCard chosen = h.get(handIndex);


        // Play wild card
        h.remove(handIndex);
        discard.push(chosen);


        activeColor = chosenColor;
        finishPlay(h, " set colour to " + activeColor + ". Press Next to continue.");
    }

    public void draw() {
        ensureAwaitingAction();

        UnoPlayer p = players.get(current);

        p.hand.add(popOrRecycle());
        mustPressNext = true;
        info = p.name + " drew 1 card. Press Next to continue.";
        notifyViews();
    }

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

    private void handlecardeffect(UnoCard chosen, List<UnoCard> currentHand) {
        switch (chosen.rank) {
            case REVERSE -> {
                gameDirection= -gameDirection;
                nextSteps = 1;
                info = "Direction reversed.";
            }
            case SKIP -> {
                nextSteps = 2;
                info = "Skip!! The next player is getting skipped.";
            }
            case DRAW_TWO -> {
                cardstoDraw = 2;
                int drawIndex = properIndex(current + gameDirection);
                for (int i = 0; i < cardstoDraw; i++) {
                    players.get(drawIndex).hand.add(popOrRecycle());
                }
                nextSteps = 2;
                info = "Draw Two! Next player will draw 2 and be skipped.";
            }
            default -> {
                nextSteps = 1;}
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
        UnoCard top = discard.pop();
        List<UnoCard> back = new ArrayList<>(discard);
        discard.clear();
        discard.push(top);
        Collections.shuffle(back, new Random());

        for (UnoCard c : back) {
            drawPile.push(c);
        }
    }

    //Just a helper to deal with getting th proper player index
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

        //regular colored cards
        for (UnoColor c : new UnoColor[]{UnoColor.RED, UnoColor.GREEN, UnoColor.BLUE, UnoColor.YELLOW}) {
            deck.add(new UnoCard(c, UnoRank.ZERO));

            UnoRank[] nums = {UnoRank.ONE, UnoRank.TWO, UnoRank.THREE, UnoRank.FOUR, UnoRank.FIVE, UnoRank.SIX, UnoRank.SEVEN, UnoRank.EIGHT, UnoRank.NINE};

            // two of each 1-9 digit cards per color
            for (UnoRank r : nums) {
                deck.add(new UnoCard(c, r));
                deck.add(new UnoCard(c, r));
            }

            //Two of each action card both skip and recerse per colour
            deck.add(new UnoCard(c, UnoRank.SKIP));
            deck.add(new UnoCard(c, UnoRank.SKIP));
            deck.add(new UnoCard(c, UnoRank.REVERSE));
            deck.add(new UnoCard(c, UnoRank.REVERSE));
            deck.add(new UnoCard(c, UnoRank.DRAW_TWO));
            deck.add(new UnoCard(c, UnoRank.DRAW_TWO));
        }

        //Four wild cards
        for (int i = 0; i < 4; i++) {
            deck.add(new UnoCard(UnoColor.WILD, UnoRank.WILD));
        }

        return deck;
    }
}
