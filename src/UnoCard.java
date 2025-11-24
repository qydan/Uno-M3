/**
 * Represents a single Uno card with a color and rank.
 * @author Ajan Balaganesh Danilo Bukvic Aydan Eng Aws Ali
 * @version 3.0
 */
public class UnoCard {
    private final UnoColor lightColor;
    private final UnoRank lightRank;

    private final UnoColor darkColor;
    private final UnoRank darkRank;

    /**
     * Constructs a dual-sided Uno Card.
     * @param lightColor Color on the light side.
     * @param lightRank Rank on the light side.
     * @param darkColor Color on the dark side.
     * @param darkRank Rank on the dark side.
     */
    public UnoCard(UnoColor lightColor, UnoRank lightRank, UnoColor darkColor, UnoRank darkRank) {
        this.lightColor = lightColor;
        this.lightRank = lightRank;
        this.darkColor = darkColor;
        this.darkRank = darkRank;
    }

    /**
     * Gets the color based on the current side.
     * @param isDark True for dark side, false for light.
     * @return The active UnoColor.
     */
    public UnoColor getColor(boolean isDark) {
        return isDark ? darkColor : lightColor;
    }

    /**
     * Gets the rank based on the current side.
     * @param isDark True for dark side, false for light.
     * @return The active UnoRank.
     */
    public UnoRank getRank(boolean isDark) {
        return isDark ? darkRank : lightRank;
    }

    public boolean isWild(boolean isDark) {
        UnoRank r = getRank(isDark);
        return r == UnoRank.WILD || r == UnoRank.WILD_DRAW_TWO || r == UnoRank.WILD_DRAW_COLOR;
    }

    /**
     * Checks if the card matches the top card.
     * @param top The top card on the discard pile.
     * @param activeColor The currently active color (handles Wild overrides).
     * @param isDark Whether the game is on the dark side.
     * @return True if playable.
     */
    public boolean matches(UnoCard top, UnoColor activeColor, boolean isDark) {
        UnoColor myColor = getColor(isDark);
        UnoRank myRank = getRank(isDark);

        if (isWild(isDark)) return true;

        // Match color, rank, or if the top card matches my color
        return myColor == activeColor || myRank == top.getRank(isDark);
    }

    /**
     * Returns text representation based on side.
     * @param isDark True for dark side.
     * @return String description.
     */
    public String toText(boolean isDark) {
        return getColor(isDark) + "-" + getRank(isDark);
    }

    @Override
    public String toString() {
        return toText(false) + " / " + toText(true);
    }

    /**
     * Returns text representation based on side.
     * @return card's light rank.
     */
    public UnoRank getLightRank() {
        return lightRank;
    }

    /**
     * Returns text representation based on side.
     * @return card's dark rank.
     */
    public UnoRank getDarkRank() {
        return darkRank;
    }

}

