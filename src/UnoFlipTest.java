import org.junit.Test;
import org.junit.Before;
import java.util.*;
import static org.junit.Assert.*;

/**
 * Test class for UnoFlip flipping implementation
 * @author Ajan Balaganesh Danilo Bukvic Aydan Eng Aws Ali
 * @version 3.0
 */
public class UnoFlipTest {
    private UnoModel model;
    private UnoViewStub view;

    /**
     * Sets up the model before each test.
     */
    @Before
    public void setup() {
        // 2 Humans
        model = new UnoModel(2, Arrays.asList("A", "B"), Arrays.asList(false, false));
        view = new UnoViewStub();
        model.addView(view);
    }

    /**
     * Tests that Flip Card puts GUI into dark mode and model is in dark mode state
     */
    @Test
    public void testFlipCardTogglesState() {
        assertFalse("Model should start in Light mode", model.isDark());

        UnoCard flip = new UnoCard(UnoColor.RED, UnoRank.FLIP, UnoColor.TEAL, UnoRank.FLIP);
        model.forceHand(0, new ArrayList<>(List.of(flip)));
        model.setTopCard(new UnoCard(UnoColor.RED, UnoRank.ONE, UnoColor.TEAL, UnoRank.ONE));
        model.play(0);

        assertTrue("Model should be dark after playing Flip", model.isDark());
        assertTrue("View event should reflect dark state", view.lastEvent.isDark());
    }

    /**
     * Tests the Draw Five card logic.
     * Verifies that when a Draw Five card is played, the next player draws 5 cards,
     * their turn is skipped, and the game state updates correctly without ending prematurely.
     */
    @Test
    public void testDrawFive() {
        UnoCard drawFiveCard = new UnoCard(UnoColor.TEAL, UnoRank.DRAW_FIVE, UnoColor.TEAL, UnoRank.DRAW_FIVE);
        UnoCard dummy = new UnoCard(UnoColor.TEAL, UnoRank.ONE, UnoColor.TEAL, UnoRank.ONE);
        model.forceHand(0, new ArrayList<>(List.of(drawFiveCard, dummy)));
        model.setTopCard(new UnoCard(UnoColor.TEAL, UnoRank.ONE, UnoColor.TEAL, UnoRank.ONE));

        int initialDeck = model.getDrawPileSize();

        model.play(0);

        assertEquals(1, view.lastEvent.getHand().size());
        assertEquals("TEAL-DRAW_FIVE", model.getDiscardTop().toText(true));
        assertTrue("Must press next to continue", view.lastEvent.isMustPressNext());

        model.nextPlayer();

        assertEquals("Deck should decrease by 5", initialDeck - 5, model.getDrawPileSize());
        assertEquals("Turn should return to Alice (skip)", "A", view.lastEvent.getCurrentPlayerName());
    }
}