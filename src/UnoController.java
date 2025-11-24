import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controller class for the Uno game that handles user interactions and updates the model.
 * This class implements the ActionListener interface to respond to button clicks and user actions
 * from the view. It acts as the intermediary between the UnoView and UnoModel, translating
 * user commands into model operations.
 * @author Danilo Bukvic Ajan Balaganesh Aydan Eng Aws Ali
 * @version 1.0
 */
public class UnoController implements ActionListener {

    private final UnoModel model;
    private final UnoView view;

    public UnoController(UnoModel model, UnoView view) {
        this.model = model;
        this.view = view;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        try {
            //playing card
            if (cmd.startsWith("PLAY:")) {
                handPlayCommand(cmd);

                //Handle drawing a card
            } else if (cmd.equals("DRAW")) {
                model.draw();

                //Move to the next player
            } else if (cmd.equals("NEXT")) {
                model.nextPlayer();

            } else {
                System.err.println("Unknown command: " + cmd);
            }

        } catch (IllegalStateException ex) {
            view.showInfo(ex.getMessage());
        }
    }

    private void handPlayCommand(String command) {
        int idx = Integer.parseInt(command.substring("PLAY:".length()));

        // Check validity via Model helper first
        if (model.peekCardInHand(idx) == null) {
            view.showInfo("Invalid card.");
            return;
        }

        // Ask Model if this card is wild
        if (model.isCardWild(idx)) {
            UnoColor chosen = view.promptForWildColor();
            if (chosen == null || chosen == UnoColor.NONE) {
                view.showInfo("Wild play cancelled.");
                return;
            }
            model.playWild(idx, chosen);
        } else {
            model.play(idx);
        }
    }
}
