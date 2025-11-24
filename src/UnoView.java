import java.awt.Color;
/**
 * View interface for Uno.
 * Defines how the model communicates visual/game state updates via UnoEvent.
 * @author Ajan Balaganesh Danilo Bukvic Aydan Eng Aws Ali
 * @version 3.0
 */
public interface UnoView {

    /**
     * Handles a game state update event from the model.
     * @param e The UnoEvent containing the current game state.
     */
    void handleUpdate(UnoEvent e);

    /**
     * Handles the end of the game.
     * @param message The winning message.
     */
    void handleEnd(String message);

    /**
     * Prompts the user to select a color for a Wild card.
     * @return The selected UnoColor.
     */
    UnoColor promptForWildColor();

    /**
     * Displays an informational message or error to the user.
     * @param message The message to display.
     */
    void showInfo(String message);

    /**
     * Maps an UnoColor enum to a Java AWT Color.
     * @param c The UnoColor.
     * @return The color.
     */
    Color mapCardColor(UnoColor c);
}