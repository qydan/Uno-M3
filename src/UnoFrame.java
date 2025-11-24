import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
/**
 * Main GUI frame for the Uno game. Handles all visual display and updates from the model.
 * @author Ajan Balaganesh Danilo Bukvic Aydan Eng Aws Ali
 * @version 3.0
 */
public class UnoFrame extends JFrame implements UnoView {
    // GUI components
    private final JLabel labelTopCard = new JLabel("Top: -", SwingConstants.CENTER);
    private final JLabel labelPlayer = new JLabel("Player: -", SwingConstants.CENTER);
    private final JLabel labelInfo = new JLabel(" ", SwingConstants.CENTER);
    private final JPanel handPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
    private final JButton buttonDraw = new JButton("Draw");
    private final JButton buttonNext = new JButton("Next Player");
    private final JPanel contentPane;

    // Controller
    private final UnoController controller;
    private boolean isDark = false;

    /**
     * Constructor for UnoFrame.
     * @param title title of UnoFrame instance.
     */
    public UnoFrame(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);

        // Ask how many players will play
        int num = askPlayerCount();
        List<String> names = new ArrayList<>();
        List<Boolean> isAI = new ArrayList<>();

        for (int i = 1; i <= num; i++) {
            JTextField nameField = new JTextField("Player " + i);
            JCheckBox aiCheck = new JCheckBox("Is AI?");
            Object[] msg = {"Enter name:", nameField, aiCheck};
            JOptionPane.showMessageDialog(this, msg, "Player " + i + " Setup", JOptionPane.QUESTION_MESSAGE);

            names.add(nameField.getText());
            isAI.add(aiCheck.isSelected());
        }

        // Init model and controller
        UnoModel model = new UnoModel(num, names, isAI);
        controller = new UnoController(model, this);

        // North Panel: Game state
        JPanel north = new JPanel(new GridLayout(2, 1));
        JPanel stats = new JPanel(new GridLayout(1, 3));
        stats.add(labelTopCard);
        stats.add(labelPlayer);
        stats.add(labelInfo);
        north.add(stats);

        // South Panel: Control buttons
        JPanel south = new JPanel();
        buttonDraw.setActionCommand("DRAW");
        buttonDraw.addActionListener(controller);
        buttonNext.setActionCommand("NEXT");
        buttonNext.addActionListener(controller);
        south.add(buttonDraw);
        south.add(buttonNext);

        contentPane = new JPanel(new BorderLayout());
        contentPane.add(north, BorderLayout.NORTH);
        contentPane.add(new JScrollPane(handPanel), BorderLayout.CENTER);
        contentPane.add(south, BorderLayout.SOUTH);

        setContentPane(contentPane);
        setLocationRelativeTo(null);

        model.addView(this);

        setVisible(true);
    }

    // Moved asking player count to private helper method
    private int askPlayerCount() {
        String s = JOptionPane.showInputDialog(this, "Number of Players (2-4):", "2");
        try { return Integer.parseInt(s); } catch(Exception e) { return 2; }
    }

    /**
     * Handles the updates to the game state and refreshes the UI when needed.
     * @param e The UnoEvent containing the current game state.
     */
    @Override
    public void handleUpdate(UnoEvent e) {
        this.isDark = e.isDark();
        Color bgColor = isDark ? new Color(50, 0, 50) : new Color(240, 240, 240); // Dark purple vs White
        handPanel.setBackground(bgColor);
        if (contentPane != null) contentPane.setBackground(bgColor);

        // Update labels
        labelTopCard.setText("Top: " + e.getTopCardText());
        labelPlayer.setText("Turn: " + e.getCurrentPlayerName() + (e.isAIPlayer() ? " (AI)" : ""));
        labelInfo.setText(e.getInfo());

        // Refresh hand panel
        handPanel.removeAll();
        List<UnoCard> currentHand = e.getHand();
        for (int i = 0; i < currentHand.size(); i++) {
            UnoCard c = currentHand.get(i);
            JButton b = new JButton(c.toText(isDark));
            b.setBackground(mapCardColor(c.getColor(isDark)));
            b.setForeground(isDark ? Color.WHITE : Color.BLACK);
            b.setActionCommand("PLAY:" + i);
            b.addActionListener(controller);
            b.setEnabled(!e.isMustPressNext() && !e.isAIPlayer()); // Disable hand if AI turn
            handPanel.add(b);
        }

        // Button Logic
        if (e.isAIPlayer()) {
            buttonDraw.setEnabled(false);
            if (e.isMustPressNext()) {
                buttonNext.setText("Next Player");
            } else {
                buttonNext.setText("Run AI Turn");
            }
            buttonNext.setEnabled(true);

        } else {
            buttonDraw.setEnabled(!e.isMustPressNext());
            buttonNext.setText("Next Player");
            buttonNext.setEnabled(e.isMustPressNext());
        }

        // Repaint and revalidate hand
        handPanel.revalidate();
        handPanel.repaint();
    }

    /**
     * Handles the end of the game, shows end of game message and exits program.
     * @param message The message to display when the game ends.
     */
    @Override
    public void handleEnd(String message) {
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        System.exit(0);
    }

    /**
     * Prompt the user to choose a color for a Wild card.
     * @return The UnoColor selected by the player.
     */
    @Override
    public UnoColor promptForWildColor() {
        UnoColor[] opts = isDark ?
                new UnoColor[]{UnoColor.TEAL, UnoColor.PINK, UnoColor.PURPLE, UnoColor.ORANGE} :
                new UnoColor[]{UnoColor.RED, UnoColor.GREEN, UnoColor.BLUE, UnoColor.YELLOW};

        int n = JOptionPane.showOptionDialog(this, "Choose Color:", "Wild", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
        return (n >= 0) ? opts[n] : opts[0];
    }

    /**
     * Displays a message to the user.
     * @param message The message to show.
     */
    @Override
    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Maps an UnoColor to a corresponding AWT Java Color for UI.
     * @param c The UnoColor to map.
     * @return The Java Color corresponding to the given UnoColor.
     */
    @Override
    public Color mapCardColor(UnoColor c) {
        return switch(c) {
            case RED -> new Color(255, 80, 80);
            case GREEN -> new Color(80, 200, 80);
            case BLUE -> new Color(80, 80, 255);
            case YELLOW -> new Color(255, 220, 0);
            case TEAL -> new Color(0, 128, 128);
            case PINK -> new Color(255, 105, 180);
            case PURPLE -> new Color(128, 0, 128);
            case ORANGE -> new Color(255, 165, 0);
            default -> Color.GRAY;
        };
    }
}
