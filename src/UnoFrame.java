import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
/**
 * Main GUI frame for the Uno game. Handles all visual display and updates from the model.
 * @author Ajan Balaganesh Danilo Bukvic Aydan Eng Aws Ali
 * @version 1.0
 */
public class UnoFrame extends JFrame implements UnoView {
    // GUI components
    private final JLabel labelTopCard = new JLabel("Top: -", SwingConstants.CENTER);
    private final JLabel labelPlayer = new JLabel("Player: -", SwingConstants.CENTER);
    private final JLabel labelInfo = new JLabel(" ", SwingConstants.CENTER);

    private final JPanel handPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
    private final JButton buttonDraw = new JButton("Draw");
    private final JButton buttonNext = new JButton("Next Player");

    // Controller
    private final UnoController controller;

    public UnoFrame(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);

        // Ask how many players will play
        int players = askPlayerCount();
        List<String> names = new ArrayList<>();

        // Prompt players for their name
        for (int i = 1; i <= players; i++) {
            String name = JOptionPane.showInputDialog(this, "Enter name for Player " + i, "Player " + i);
            if (name == null || name.isBlank()) name = "Player " + i;
            names.add(name);
        }

        // Init model and controller
        UnoModel model = new UnoModel(players, names);
        controller = new UnoController(model, this);
        model.addView(this);

        // North Panel: Game state
        JPanel north = new JPanel(new GridLayout(1, 3));
        labelTopCard.setFont(labelTopCard.getFont().deriveFont(Font.BOLD, 18f));
        labelPlayer.setFont(labelPlayer.getFont().deriveFont(Font.BOLD, 18f));
        north.add(labelTopCard);
        north.add(labelPlayer);
        north.add(labelInfo);

        // South Panel: Control buttons
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10));
        buttonDraw.setActionCommand("DRAW");
        buttonDraw.addActionListener(controller);
        buttonNext.setActionCommand("NEXT");
        buttonNext.addActionListener(controller);
        south.add(buttonDraw);
        south.add(buttonNext);

        JPanel root = new JPanel(new BorderLayout());
        root.add(north, BorderLayout.NORTH);
        root.add(new JScrollPane(handPanel), BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);

        setLocationRelativeTo(null);

        setVisible(true);
    }

    private int askPlayerCount() {
        Object[] opts = {"2", "3", "4"};
        Object result = JOptionPane.showInputDialog(this, "Select number of players:", "Players", JOptionPane.QUESTION_MESSAGE, null, opts, "2");
        if (result == null) return 2; // Default to 2 players
        return Integer.parseInt(result.toString());
    }

    @Override
    public void handleUpdate(java.util.List<UnoCard> currentHand, String topCardText, String currentPlayerName, String info, boolean mustPressNext) {
        // Update labels
        labelTopCard.setText("Top: " + topCardText);
        labelPlayer.setText("Turn: " + currentPlayerName + (mustPressNext ? " (press Next)" : ""));
        labelInfo.setText(info);

        // Refresh hand panel
        handPanel.removeAll();
        for (int i = 0; i < currentHand.size(); i++) {
            UnoCard c = currentHand.get(i);
            JButton b = new JButton(c.toText());
            b.setOpaque(true);
            b.setBackground(mapCardColor(c.color));
            b.setActionCommand("PLAY:" + i);
            b.addActionListener(controller);
            handPanel.add(b);
        }

        // Repaint and revalidate hand
        handPanel.revalidate();
        handPanel.repaint();

        // Toggle button states
        buttonDraw.setEnabled(!mustPressNext);
        buttonNext.setEnabled(mustPressNext);
    }

    @Override
    public void handleEnd(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE
        );
        dispose();
        System.exit(0);
    }

    @Override
    public UnoColor promptForWildColor() {
        Object[] opts = {"RED", "GREEN", "BLUE", "YELLOW"};
        Object result = JOptionPane.showInputDialog(
                this,
                "Choose a color for the Wild:",
                "Wild Color",
                JOptionPane.QUESTION_MESSAGE,
                null, 
                opts,
                "RED"
        );
        if (result == null) return UnoColor.NONE;
        return UnoColor.valueOf(result.toString());
    }

    @Override
    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
