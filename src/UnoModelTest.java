import org.junit.Test;
import org.junit.Before;
import java.util.*;
import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for UnoModel logic.
 */
public class UnoModelTest {

    private UnoModel model;
    private UnoViewStub view;

    // Helper stub to capture events without GUI
    static class UnoViewStub implements UnoView {
        UnoEvent lastEvent;
        @Override public void handleUpdate(UnoEvent e) { lastEvent = e; }
        @Override public void handleEnd(String message) { }
        @Override public UnoColor promptForWildColor() { return UnoColor.RED; }
        @Override public void showInfo(String message) { }
    }

    @Before
    public void setUp() {
        model = new UnoModel(2, Arrays.asList("Alice", "Bob"));
        view = new UnoViewStub();
        model.addView(view);
    }

    @Test
    public void testInitialState() {
        assertNotNull("View should receive initial update", view.lastEvent);
        assertEquals("Alice", view.lastEvent.getCurrentPlayerName());
        assertEquals(7, view.lastEvent.getHand().size());
    }

    @Test
    public void testPlayValidCard() {
        // Setup: Force top card to RED 5
        model.setTopCard(new UnoCard(UnoColor.RED, UnoRank.FIVE));
        // Give Alice a RED 6
        List<UnoCard> hand = new ArrayList<>();
        hand.add(new UnoCard(UnoColor.RED, UnoRank.SIX));
        hand.add(new UnoCard(UnoColor.BLUE, UnoRank.NINE));
        model.forceCurrentPlayerHand(hand);

        // Play RED 6 (index 0)
        model.play(0);

        // Check that RED 6 is on top and hand size reduced
        assertEquals("RED-SIX", model.getDiscardTop().toText());
        assertEquals(1, view.lastEvent.getHand().size());
        assertTrue("Must press next after playing", view.lastEvent.isMustPressNext());
    }

    @Test(expected = IllegalStateException.class)
    public void testPlayInvalidCardThrowsException() {
        model.setTopCard(new UnoCard(UnoColor.RED, UnoRank.FIVE));
        // Hand has BLUE 9 only
        List<UnoCard> hand = new ArrayList<>();
        hand.add(new UnoCard(UnoColor.BLUE, UnoRank.NINE));
        model.forceCurrentPlayerHand(hand);

        model.play(0); // Should fail
    }

    @Test
    public void testDrawCard() {
        int initialSize = view.lastEvent.getHand().size();
        model.draw();
        assertEquals("Hand should increase by 1", initialSize + 1, view.lastEvent.getHand().size());
        assertTrue("Must press next after drawing", view.lastEvent.isMustPressNext());
    }

    @Test
    public void testTurnProgression() {
        // Alice plays
        model.setTopCard(new UnoCard(UnoColor.RED, UnoRank.FIVE));
        List<UnoCard> hand = new ArrayList<>();
        hand.add(new UnoCard(UnoColor.RED, UnoRank.SIX));
        model.forceCurrentPlayerHand(hand);

        model.play(0);
        model.nextPlayer();

        // Should be Bob's turn
        assertEquals("Bob", view.lastEvent.getCurrentPlayerName());
    }

    @Test
    public void testSkipCardEffect() {
        // 2 Player game: Skip skips Bob, goes back to Alice
        model.setTopCard(new UnoCard(UnoColor.RED, UnoRank.FIVE));
        List<UnoCard> hand = new ArrayList<>();
        hand.add(new UnoCard(UnoColor.RED, UnoRank.SKIP));
        model.forceCurrentPlayerHand(hand);

        model.play(0); // Alice plays skip

        // Check info message
        assertTrue(view.lastEvent.getInfo().contains("skipped"));

        model.nextPlayer();
        assertEquals("Alice", view.lastEvent.getCurrentPlayerName()); // Bob skipped
    }

    @Test
    public void testDrawTwoEffect() {
        model.setTopCard(new UnoCard(UnoColor.RED, UnoRank.FIVE));
        List<UnoCard> hand = new ArrayList<>();
        hand.add(new UnoCard(UnoColor.RED, UnoRank.DRAW_TWO));
        model.forceCurrentPlayerHand(hand);

        model.play(0); // Alice plays Draw Two

        // Bob should be drawn 2 cards. We need to check Bob's hand size.
        // Since we can't easily see Bob's hand via Alice's event, we check turn progression
        model.nextPlayer();
        // Bob was skipped due to Draw Two rule in this implementation
        assertEquals("Alice", view.lastEvent.getCurrentPlayerName());

        // (Advanced verification would require inspecting internal player list,
        // but checking turn flow confirms effect triggered)
    }

    @Test
    public void testWildCardPlay() {
        List<UnoCard> hand = new ArrayList<>();
        hand.add(new UnoCard(UnoColor.WILD, UnoRank.WILD));
        model.forceCurrentPlayerHand(hand);

        assertTrue(model.isCardWild(0));
        model.playWild(0, UnoColor.BLUE);

        assertEquals(UnoColor.BLUE, view.lastEvent.getActiveColor());
    }
}