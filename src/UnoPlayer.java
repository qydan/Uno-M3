import java.util.ArrayList;
import java.util.List;
/**
 * Represents a single Uno player and stores that player's hand of cards.
 * @author Danilo Bukvic Ajan Balaganesh Aydan Eng Aws Ali
 * @version 3.0
 */
public class UnoPlayer {
    public final String name;
    public final boolean isAI;
    public final List<UnoCard> hand = new ArrayList<>();

    /**
     * Constructor for UnoPlayer.
     * @param name Name of the player.
     * @param isAI If the player is an AI or not.
     */
    public UnoPlayer(String name, boolean isAI) {
        this.name = name;
        this.isAI = isAI;
    }
}
