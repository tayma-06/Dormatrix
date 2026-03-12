package cli.views.food;

import controllers.food.CafeteriaController;
import models.food.MealToken;
import utils.FastInput;
import utils.TerminalUI;

import java.util.List;

public class TokenListView {

    private final CafeteriaController controller = new CafeteriaController();

    public void show(String username) {
        List<MealToken> tokens = controller.getStudentTokens(username);

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("YOUR MEAL TOKENS");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Total Tokens: " + tokens.size());
        TerminalUI.tBoxSep();

        if (tokens.isEmpty()) {
            TerminalUI.tBoxLine("No tokens found. Go buy some delicious food!");
        } else {
            TerminalUI.tBoxLine("TOKEN ID           | MEAL       | DATE       | STATUS");
            TerminalUI.tBoxSep();

            for (MealToken t : tokens) {
                TerminalUI.tBoxLine(String.format(
                        "%-18s | %-10s | %-10s | %-20s",
                        t.getTokenId(),
                        t.getType(),
                        t.getDate(),
                        t.getStatus().name()
                ));
            }
        }

        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Press Enter to continue...");
        TerminalUI.tBoxBottom();
        FastInput.readLine();
    }
}
