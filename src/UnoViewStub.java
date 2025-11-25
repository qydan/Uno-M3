import java.awt.Color;
/**
 * A stub implementation of UnoView for unit testing.
 */
public class UnoViewStub implements UnoView {
    public UnoEvent lastEvent;
    public String lastInfo;
    public UnoColor wildColorToReturn = UnoColor.RED; // Default to RED
    public boolean handleEndCalled = false;

    @Override
    public void handleUpdate(UnoEvent e) {
        this.lastEvent = e;
    }

    @Override
    public void handleEnd(String message) {
        this.handleEndCalled = true;
        this.lastInfo = message;
    }

    @Override
    public UnoColor promptForWildColor() {
        return wildColorToReturn;
    }

    @Override
    public void showInfo(String message) {
        this.lastInfo = message;
    }

    @Override
    public Color mapCardColor(UnoColor c) {
        return null; // Not needed for logic tests
    }
}