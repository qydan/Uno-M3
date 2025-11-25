import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test class for UnoCard
 * @author Ajan Balaganesh Danilo Bukvic Aydan Eng Aws Ali
 * @version 3.0
 */
public class UnoCardTest {

    private UnoCard greenFive;
    private UnoCard redSkip;
    private UnoCard blueFive;
    private UnoCard wild;

    /**
     * Initializes sample cards for testing.
     */
    @Before
    public void setUp() {
        greenFive = new UnoCard(UnoColor.GREEN, UnoRank.FIVE, UnoColor.TEAL, UnoRank.FIVE);
        redSkip = new UnoCard(UnoColor.RED, UnoRank.SKIP, UnoColor.ORANGE, UnoRank.SKIP_EVERYONE);
        blueFive = new UnoCard(UnoColor.BLUE, UnoRank.FIVE, UnoColor.PINK, UnoRank.FIVE);
        wild = new UnoCard(UnoColor.WILD, UnoRank.WILD, UnoColor.WILD, UnoRank.WILD_DRAW_COLOR);
    }

    /**
     * Tests correct identification of Wild cards on both Light and Dark sides.
     */
    @Test
    public void testWild() {
        // Check Light side wild status
        assertFalse(greenFive.isWild(false));
        assertTrue(wild.isWild(false));
        assertFalse(blueFive.isWild(false));

        // Check Dark side wild status (Wild Draw Color is wild)
        assertTrue(wild.isWild(true));
    }

    /**
     * Tests that a card matches another if they share the same rank.
     */
    @Test
    public void testMatchSameColor() {
        UnoCard top = new UnoCard(UnoColor.GREEN, UnoRank.EIGHT, UnoColor.TEAL, UnoRank.EIGHT);
        UnoColor activeColor = UnoColor.GREEN;

        // Match on Light side
        assertTrue(greenFive.matches(top, activeColor, false));
    }


    /**
     * Tests that a card matches another if they share the same color.
     */
    @Test
    public void testMatchSameRank() {
        UnoCard top = greenFive;
        UnoColor activeColor = UnoColor.BLUE;

        // Blue Five matches Green Five (Same Rank)
        assertTrue(blueFive.matches(top, activeColor, false));
    }

    /**
     * Tests that a wild card matches any card.
     */    @Test
    public void testMatchWildAlwaysMatches() {
        UnoCard top = greenFive;
        // Wild matches anything
        assertTrue(wild.matches(top, UnoColor.GREEN, false));
    }

    /**
     * tests a card does not match if both color and rank are different.
     */    @Test
    public void testNoMatchDifferentColorAndRank() {
        UnoCard top = redSkip;
        UnoColor activeColor = UnoColor.GREEN;

        // Blue Five does not match Red Skip
        assertFalse(blueFive.matches(top, activeColor, false));
    }

    /**
     * Tests the string representation of cards for both Light and Dark sides.
     * */    @Test
    public void testToText() {
        // Test Light Side Text
        assertEquals("GREEN-FIVE", greenFive.toText(false));
        assertEquals("BLUE-FIVE", blueFive.toText(false));

        // Test Dark Side Text
        assertEquals("TEAL-FIVE", greenFive.toText(true));
        assertEquals("PINK-FIVE", blueFive.toText(true));
    }
}

