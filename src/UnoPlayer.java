import java.util.ArrayList;
import java.util.List;
/**
 * Represents a single Uno player and stores that player's hand of cards.
 * @author Danilo Bukvic Ajan Balaganesh Aydan Eng Aws Ali
 * @version 1.0
 */
public class UnoPlayer {
    public final String name;

    public List<UnoCard> hand = new ArrayList<>();


    public UnoPlayer(String name) {
        this.name = name;
    }
}
