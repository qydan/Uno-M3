import java.util.EventObject;
import java.util.List;

/**
 * Represents a snapshot of the Uno game state to be sent to the View.
 * Follows the Java Event Model by extending EventObject.
 *
 * @author Ajan Balaganesh Danilo Bukvic Aydan Eng Aws Ali
 * @version 2.0
 */
public class UnoEvent extends EventObject {
    private final List<UnoCard> hand;
    private final String topCardText;
    private final String currentPlayerName;
    private final String info;
    private final boolean mustPressNext;
    private final UnoColor activeColor;
    private final boolean isDark;
    private final boolean isAIPlayer;

    /**
     * Constructs a new UnoEvent.
     *
     * @param source            The object on which the Event initially occurred.
     * @param hand              The current player's hand.
     * @param topCardText       The string representation of the top discard card.
     * @param currentPlayerName The name of the current player.
     * @param info              Status message to display.
     * @param mustPressNext     True if the player needs to end their turn.
     * @param activeColor       The current active color (useful for UI backgrounds).
     */
    public UnoEvent(Object source, List<UnoCard> hand, String topCardText,
                    String currentPlayerName, String info, boolean mustPressNext,
                    UnoColor activeColor, boolean isDark, boolean isAIPlayer) {
        super(source);
        this.hand = hand;
        this.topCardText = topCardText;
        this.currentPlayerName = currentPlayerName;
        this.info = info;
        this.mustPressNext = mustPressNext;
        this.activeColor = activeColor;
        this.isDark = isDark;
        this.isAIPlayer = isAIPlayer;
    }

    /**
     * Gets the current player's hand.
     * @return List of UnoCards.
     */
    public List<UnoCard> getHand() {
        return hand;
    }

    /**
     * Gets the text of the top card.
     * @return Top card string.
     */
    public String getTopCardText() {
        return topCardText;
    }

    /**
     * Gets the current player's name.
     * @return Player name string.
     */
    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    /**
     * Gets the info message.
     * @return Info string.
     */
    public String getInfo() {
        return info;
    }

    /**
     * Checks if the Next button should be active.
     * @return True if waiting for next player, False otherwise.
     */
    public boolean isMustPressNext() {
        return mustPressNext;
    }

    /**
     * Gets the active color (handles Wild color changes).
     * @return The current UnoColor.
     */
    public UnoColor getActiveColor() {
        return activeColor;
    }

    public boolean isDark() {
        return isDark;
    }

    public boolean isAIPlayer() {
        return isAIPlayer;
    }
}