/**
 * Represents a single Uno card with a color and rank.
 * @author Ajan Balaganesh Danilo Bukvic Aydan Eng Aws Ali
 * @version 1.0
 */
public class UnoCard {
    public final UnoColor color;
    public final UnoRank rank;

    public UnoCard(UnoColor color, UnoRank rank) {
        this.color = color;
        this.rank = rank;
    }

    public boolean isWild() {
        return rank == UnoRank.WILD;
    }

    public boolean matches(UnoCard top, UnoColor activeColor) {
        if (isWild()) {
            return true;
        }
        return color == activeColor | rank == top.rank | color == top.color;
    }

    public String toText() {
        if (color == UnoColor.WILD){
            return "WILD";
        }
        return color + "-" + rank;
    }


    @Override
    public String toString() {
        return toText();
    }


}

