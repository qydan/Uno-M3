import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

/**
 * Test class for AI player implementation
 * @author Ajan Balaganesh Danilo Bukvic Aydan Eng Aws Ali
 * @version 3.0
 */
public class UnoAiTest {

    /**
     * Tests that the UnoModel correctly identifies and initializes AI players.
     * Verifies that a game with a Human and a Bot is made correctly
     * and that the hand gen for the Human player functions as expected.
     */
    @Test
    public void testAISelection() {
        UnoModel m = new UnoModel(2, Arrays.asList("Human", "Bot"), Arrays.asList(false, true));
        assertNotNull(m.peekCardInHand(0)); // Human exists
    }

    /**
     * Tests that the AI logic successfully performs a valid move during its turn.
     * This test ensures that when playAITurn() is called:
     *  The AI player either matches a card or draws a card.
     */
    @Test
    public void testAIPerformsMove() {
        // Setup model with 2 AI players
        UnoModel m = new UnoModel(2, Arrays.asList("Bot1", "Bot2"), Arrays.asList(true, true));

        // Attach view stub
        UnoViewStub stub = new UnoViewStub();
        m.addView(stub);

        assertNotNull("View should receive initial event", stub.lastEvent);
        assertEquals("Bot1", stub.lastEvent.getCurrentPlayerName());
        int initialHandSize = stub.lastEvent.getHand().size();
        assertEquals(7, initialHandSize); // Should start with 7 cards

        m.playAITurn();
        // Assert Logic
        // The AI logic will either:
        // Find a matching card and PLAY it (Hand size - 1)
        // Find no match and DRAW a card (Hand size + 1)

        int newHandSize = stub.lastEvent.getHand().size();
        boolean playedCard = (newHandSize == initialHandSize - 1);
        boolean drewCard = (newHandSize == initialHandSize + 1);

        assertTrue("AI should have either played a card (-1) or drew a card (+1). New Size: " + newHandSize, playedCard || drewCard);
        assertTrue("Model should wait for Next after AI move", stub.lastEvent.isMustPressNext());
    }
}