import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
/**
 * Controller class for the Uno game that handles user interactions and updates the model.
 * This class implements the ActionListener interface to respond to button clicks and user actions
 * from the view. It acts as the intermediary between the UnoView and UnoModel, translating
 * user commands into model operations.
 * @author Danilo Bukvic Ajan Balaganesh Aydan Eng Aws Ali
 * @version 3.0
 */
public class UnoController implements ActionListener {

    private final UnoModel model;
    private final UnoView view;

    /**
     * Constructor for UnoController.
     * @param model The UnoModel instance representing the game state.
     * @param view The UnoView instance responsible for the user interface.
     */
    public UnoController(UnoModel model, UnoView view) {
        this.model = model;
        this.view = view;
    }

    /**
     * Handles user actions from the UI, such as playing or drawing a card.
     * @param e The ActionEvent triggered by th component.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        try {
            if (cmd.startsWith("PLAY:")) {
                int idx = Integer.parseInt(cmd.substring("PLAY:".length()));
                if (model.peekCardInHand(idx) == null) {
                    view.showInfo("Invalid card.");
                    return;
                }
                if (model.isCardWild(idx)) {
                    UnoColor c = view.promptForWildColor();
                    if (c == null || c == UnoColor.NONE) {
                        view.showInfo("Wild play cancelled.");
                        return;
                    }
                    model.playWild(idx, c);
                } else {
                    model.play(idx);
                }
            } else if (cmd.equals("DRAW")) {
                model.draw();
            } else if (cmd.equals("NEXT")) {
                try {
                    model.nextPlayer();
                } catch (IllegalStateException ex) {
                    model.playAITurn();
                }
            }
        } catch (Exception ex) {
            view.showInfo(ex.getMessage());
        }
    }
}
