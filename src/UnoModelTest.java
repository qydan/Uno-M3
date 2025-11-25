import org.junit.Test;
import org.junit.Before;
import java.util.*;
import static org.junit.Assert.*;
/**
 * Test class for UnoModel
 * @author Ajan Balaganesh Danilo Bukvic Aydan Eng Aws Ali
 * @version 3.0
 */
public class UnoModelTest {
    private UnoModel model;
    private UnoViewStub view;

    /**
     * Sets up the model and view before each test.
     */
    @Before
    public void setUp() {
        model = new UnoModel(2, Arrays.asList("Alice", "Bob"), Arrays.asList(false, false));
        view = new UnoViewStub();
        model.addView(view);
    }

    /**
     * Tests that the initial game state is correct.
     */
    @Test
    public void testInitialState() {
        assertNotNull("View should receive initial update", view.lastEvent);
        assertEquals("Alice", view.lastEvent.getCurrentPlayerName());
        assertEquals(7, view.lastEvent.getHand().size());
        assertFalse("Should start on Light side", view.lastEvent.isDark());
    }

    /**
     * Tests that playing an invalid card throws an exception.
     */
    @Test(expected = IllegalStateException.class)
    public void testPlayInvalidCardThrowsException() {
        model.setTopCard(new UnoCard(UnoColor.RED, UnoRank.FIVE, UnoColor.TEAL, UnoRank.FIVE));

        // Bluw 9 only
        List<UnoCard> hand = new ArrayList<>();
        hand.add(new UnoCard(UnoColor.BLUE, UnoRank.NINE, UnoColor.PINK, UnoRank.NINE));
        model.forceHand(0, hand);

        model.play(0); // Should fail as BLUE doesn't match RED
    }

    /**
     * Tests that drawing a card increases hand size and sets mustPressNext flag.
     */
    @Test
    public void testDrawCard() {
        int initialSize = view.lastEvent.getHand().size();
        model.draw();
        assertEquals("Hand should increase by 1", initialSize + 1, view.lastEvent.getHand().size());
        assertTrue("Must press next after drawing", view.lastEvent.isMustPressNext());
    }

    /**
     * Tests turn progression from one player to the next.
     */
    @Test
    public void testTurnProgression() {
        // Top card is RED 5
        model.setTopCard(new UnoCard(UnoColor.RED, UnoRank.FIVE, UnoColor.TEAL, UnoRank.FIVE));

        List<UnoCard> hand = new ArrayList<>();
        hand.add(new UnoCard(UnoColor.RED, UnoRank.SIX, UnoColor.TEAL, UnoRank.SIX));
        // Extra card so Alice doesn't win immediately
        hand.add(new UnoCard(UnoColor.BLUE, UnoRank.NINE, UnoColor.PINK, UnoRank.NINE));

        model.forceHand(0, hand);

        // Alice plays index 0 (RED 6)
        model.play(0);

        // Now she has 1 card left, so the game continues and waits for 'Next'
        model.nextPlayer();
        assertEquals("Bob", view.lastEvent.getCurrentPlayerName());
    }

    /**
     * Tests that playing a valid card updates the top of discard and hand correctly.
     */
    @Test
    public void testPlayValidCard() {
        model.setTopCard(new UnoCard(UnoColor.RED, UnoRank.FIVE, UnoColor.TEAL, UnoRank.FIVE));

        List<UnoCard> hand = new ArrayList<>();
        hand.add(new UnoCard(UnoColor.RED, UnoRank.SIX, UnoColor.TEAL, UnoRank.SIX));
        model.forceHand(0, hand);

        model.play(0);

        assertEquals("RED-SIX", model.getDiscardTop().toText(false));
        assertEquals(0, view.lastEvent.getHand().size());
    }

    /**
     * Tests that playing a skip everyone card is  correctly.
     */
    @Test
    public void testSkipEveryoneEffect() {
        // Alice gets a Skip Everyone card AND an extra card so she doesn't win immediately
        UnoCard skipAll = new UnoCard(UnoColor.ORANGE, UnoRank.SKIP_EVERYONE, UnoColor.ORANGE, UnoRank.SKIP_EVERYONE);
        UnoCard extra = new UnoCard(UnoColor.RED, UnoRank.ONE, UnoColor.TEAL, UnoRank.ONE);

        model.forceHand(0, new ArrayList<>(List.of(skipAll, extra)));
        model.setTopCard(new UnoCard(UnoColor.ORANGE, UnoRank.ONE, UnoColor.ORANGE, UnoRank.ONE));
        model.play(0);

        // Must press next
        assertTrue(view.lastEvent.isMustPressNext());

        // Advance turn
        model.nextPlayer();

        // It should still be Alice's turn
        assertEquals("Alice", view.lastEvent.getCurrentPlayerName());
    }

    /**
     * Tests the logic for the Wild Draw Color card.
     * Verifies that when a Wild Draw Color card is played, the next player draws cards
     * from the deck until they find the chosen color, and their turn is skipped, returning to the original player.
     */
    @Test
    public void testWildDrawColorLogic() {
        UnoCard wdc = new UnoCard(UnoColor.WILD, UnoRank.WILD_DRAW_COLOR, UnoColor.WILD, UnoRank.WILD_DRAW_COLOR);
        UnoCard dummy = new UnoCard(UnoColor.RED, UnoRank.ONE, UnoColor.TEAL, UnoRank.ONE);
        model.forceHand(0, new ArrayList<>(List.of(wdc, dummy)));

        model.setTopCard(new UnoCard(UnoColor.TEAL, UnoRank.ONE, UnoColor.TEAL, UnoRank.ONE));

        int initialDeck = model.getDrawPileSize();

        model.playWild(0, UnoColor.RED);

        model.nextPlayer();

        assertTrue("Deck size should decrease", model.getDrawPileSize() < initialDeck);
        assertEquals("Alice", view.lastEvent.getCurrentPlayerName());
    }

    /**
     * Tests the winning condition and score calculation.
     * Verifies that when a player plays their last card, the game correctly triggers the end-game
     * sequence (handleEnd) and the message indicates that the player has won.
     */
    @Test
    public void testWinningAndScoring() {
        UnoCard winner = new UnoCard(UnoColor.RED, UnoRank.ONE, UnoColor.TEAL, UnoRank.ONE);
        model.forceHand(0, new ArrayList<>(List.of(winner)));

        UnoCard c1 = new UnoCard(UnoColor.WILD, UnoRank.WILD_DRAW_COLOR, UnoColor.WILD, UnoRank.WILD_DRAW_COLOR);
        UnoCard c2 = new UnoCard(UnoColor.TEAL, UnoRank.DRAW_FIVE, UnoColor.TEAL, UnoRank.DRAW_FIVE);
        model.forceHand(1, new ArrayList<>(List.of(c1, c2)));

        model.setTopCard(new UnoCard(UnoColor.RED, UnoRank.TWO, UnoColor.TEAL, UnoRank.TWO));

        model.play(0);

        assertTrue("HandleEnd should be called", view.handleEndCalled);

        assertTrue(view.lastInfo.contains("WON"));
    }
}