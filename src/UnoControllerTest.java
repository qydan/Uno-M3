import org.junit.*;
import static org.junit.Assert.*;
import java.awt.event.ActionEvent;
import java.util.List;
/**
 * Test class for UnoController
 * @author Ajan Balaganesh Danilo Bukvic Aydan Eng Aws Ali
 * @version 3.0
 */
public class UnoControllerTest {
    private MockModel model;
    private UnoViewStub view;
    private UnoController controller;

    /**
     * Initializes the mock model, view stub, and controller before each test.
     */
    @Before
    public void setUp() {
        model = new MockModel();
        view = new UnoViewStub();
        controller = new UnoController(model, view);
    }

    /**
     * Tests that a "PLAY" command correctly triggers the model's play method.
     */
    @Test
    public void testPlayCard() {
        controller.actionPerformed(new ActionEvent(this, 0, "PLAY:0"));
        assertTrue("play() should be called", model.playCalled);
    }

    /**
     * Tests that playing a Wild card triggers the specific playWild method on the model.
     */
    @Test
    public void testPlayWild() {
        model.isWild = true;
        controller.actionPerformed(new ActionEvent(this, 0, "PLAY:0"));
        assertTrue("playWild() should be called", model.playWildCalled);
    }

    /**
     * tests that canceling the Wild card color selection aborts the move and updates the view.
     */
    @Test
    public void testPlayWildCancelled() {
        model.isWild = true;
        view.wildColorToReturn = UnoColor.NONE;
        controller.actionPerformed(new ActionEvent(this, 0, "PLAY:0"));
        assertFalse("playWild() should not be called when cancelled", model.playWildCalled);
        assertNotNull("showInfo() should be called", view.lastInfo);
    }

    /**
     * Tests that attempting to play an invalid or null card does not trigger model logic.
     */
    @Test
    public void testPlayInvalidCard() {
        model.returnNull = true;
        controller.actionPerformed(new ActionEvent(this, 0, "PLAY:99"));
        assertFalse("play() should not be called for invalid card", model.playCalled);
        assertNotNull("showInfo() should be called", view.lastInfo);
    }

    /**
     * Tests that a "DRAW" command correctly triggers the model's draw method.
     */
    @Test
    public void testDraw() {
        controller.actionPerformed(new ActionEvent(this, 0, "DRAW"));
        assertTrue("draw() should be called", model.drawCalled);
    }

    /**
     * Tests that a "NEXT" command correctly triggers the model's nextPlayer method.
     */
    @Test
    public void testNext() {
        controller.actionPerformed(new ActionEvent(this, 0, "NEXT"));
        assertTrue("nextPlayer() should be called", model.nextPlayerCalled);
    }

    /**
     * Tests that exceptions thrown by the model are caught and displayed to the user.
     */
    @Test
    public void testExceptionHandling() {
        model.throwException = true;
        controller.actionPerformed(new ActionEvent(this, 0, "DRAW"));
        assertNotNull("showInfo() should be called on exception", view.lastInfo);
    }

    // MOCK MODEL
    static class MockModel extends UnoModel {
        boolean playCalled = false;
        boolean playWildCalled = false;
        boolean drawCalled = false;
        boolean nextPlayerCalled = false;
        boolean playAICalled = false;

        boolean isWild = false;
        boolean returnNull = false;
        boolean throwException = false;

        public MockModel() {
            super(2, List.of("P1", "P2"), List.of(false, false));
        }

        @Override
        public void play(int index) {
            if (throwException) throw new IllegalStateException("Test exception");
            playCalled = true;
        }

        @Override
        public void playWild(int index, UnoColor color) {
            playWildCalled = true;
        }

        @Override
        public void playAITurn() {
            playAICalled = true;
        }

        @Override
        public void draw() {
            if (throwException) throw new IllegalStateException("Test exception");
            drawCalled = true;
        }

        @Override
        public void nextPlayer() {
            nextPlayerCalled = true;
        }

        @Override
        public boolean isCardWild(int index) {
            return isWild;
        }

        @Override
        public UnoCard peekCardInHand(int index) {
            if (returnNull) return null;
            return isWild
                    ? new UnoCard(UnoColor.WILD, UnoRank.WILD, UnoColor.WILD, UnoRank.WILD_DRAW_COLOR)
                    : new UnoCard(UnoColor.RED, UnoRank.FIVE, UnoColor.TEAL, UnoRank.FIVE);
        }
    }
}
