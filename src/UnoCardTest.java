import org.junit.*;
import static org.junit.Assert.*;

public class UnoCardTest {

    private UnoCard greenFive;
    private UnoCard redSkip;
    private UnoCard blueFive;
    private UnoCard wild;

    // set up some  cards for testing
    @Before
    public void setUp() {
        greenFive = new UnoCard(UnoColor.GREEN, UnoRank.FIVE);
        redSkip = new UnoCard(UnoColor.RED, UnoRank.SKIP);
        blueFive = new UnoCard(UnoColor.BLUE, UnoRank.FIVE);
        wild = new UnoCard(UnoColor.WILD, UnoRank.WILD);
    }

    @Test
    public void testWild() {
        assertFalse(greenFive.isWild());
        assertTrue(wild.isWild());

        assertFalse(blueFive.isWild());
    }

    // Making sure that we can play a card thats the same colour
    @Test
    public void testMatchSameColor() {
        UnoCard top = new  UnoCard(UnoColor.GREEN, UnoRank.EIGHT);


        UnoColor activeColor = UnoColor.GREEN;

        assertTrue(greenFive.matches(top, activeColor));
    }


    // Making sure we can play same rank
    @Test
    public void testMatchSameRank() {

        UnoCard top = greenFive;
        UnoColor activeColor = UnoColor.BLUE;
        assertTrue(blueFive.matches(top, activeColor));
    }

    //wild should be able to be played whenever
    @Test
    public void testMatchWildAlwaysMatches() {
        UnoCard top = greenFive;
        assertTrue(wild.matches(top, UnoColor.GREEN));
        assertTrue(wild.matches(top, UnoColor.NONE));
    }

    //shouldn't be able to play a card thats not the same colour or rank
    @Test
    public void testNoMatchDifferentColorAndRank() {

        UnoCard top = redSkip;

        UnoColor activeColor = UnoColor.GREEN;

        assertFalse(blueFive.matches(top, activeColor));
    }

    //Making sure there is proper string representation
    @Test
    public void testToText() {
        assertEquals("GREEN-FIVE", greenFive.toText());
        assertEquals("BLUE-FIVE", blueFive.toText());
    }



}

