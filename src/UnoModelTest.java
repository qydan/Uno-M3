import org.junit.Test;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Basic unit tests for UnoModel core logic.
 */
public class UnoModelTest {

    @Test
    public void testModelInitialSetup() {
        List<String> names = Arrays.asList("A", "B");
        UnoModel model = new UnoModel(2, names);

        // both players should exist
        assertEquals(2, names.size());

        // each player should start with 7 cards
        // we check player 0 for example
        UnoCard c = model.peekCardInHand(0);
        assertNotNull(c);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPlayerCountTooLow() {
        new UnoModel(1, Arrays.asList("A"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPlayerCountTooHigh() {
        new UnoModel(5, Arrays.asList("A","B","C","D","E"));
    }

    @Test
    public void testPeekCardValidReturnsCard() {
        UnoModel model = new UnoModel(2, Arrays.asList("A","B"));
        UnoCard card = model.peekCardInHand(0);
        assertNotNull(card);
    }

    @Test
    public void testPeekCardInvalidReturnsNull() {
        UnoModel model = new UnoModel(2, Arrays.asList("A","B"));
        UnoCard card = model.peekCardInHand(999);
        assertNull(card);
    }
}
