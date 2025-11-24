import java.awt.Color;
import java.util.List;
/**
 * View interface for Uno; defines how the model communicates visual/game state updates to any UI.
 * @author Danilo Bukvic Ajan Balaganesh Aydan Eng Aws Ali
 * @version 1.0
 */
public interface UnoView {
    // Push state to the view directly
    void handleUpdate(
            List<UnoCard> currentHand,
            String topCardText,
            String currentPlayerName,
            String info,
            boolean mustPressNext
    );

    void handleEnd(String message);
    
    UnoColor promptForWildColor();
    
    void showInfo(String message);

    default Color mapCardColor(UnoColor c) {
        return switch (c) {
            case RED -> Color.RED;
            case GREEN -> Color.GREEN;
            case BLUE -> Color.BLUE;
            case YELLOW -> Color.YELLOW;
            default -> Color.GRAY; // Our default color
        };
    }
}
