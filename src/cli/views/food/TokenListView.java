package cli.views.food;

import controllers.food.CafeteriaController;
import models.food.MealToken;
import utils.FastInput;

import java.util.List;

public class TokenListView {
    private final CafeteriaController controller = new CafeteriaController();

    public void show(String username) {
        List<MealToken> tokens = controller.getStudentTokens(username);

        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         YOUR MEAL TOKENS                            ║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");

        System.out.println(String.format("║ %-67s ║", "Total Tokens: " + tokens.size()));
        System.out.println("╠════════════════════╦════════════╦════════════╦══════════════════════╣");

        if (tokens.isEmpty()) {
            System.out.println(String.format("║ %-67s ║", "No tokens found. Go buy some delicious food!   "));
        } else {
            System.out.println("║ TOKEN ID           ║ MEAL       ║ DATE       ║ STATUS               ║");
            System.out.println("╠════════════════════╬════════════╬════════════╬══════════════════════╣");

            for (MealToken t : tokens) {
                System.out.println(String.format(
                        "║ %-18s ║ %-10s ║ %-10s ║ %-20s ║",
                        t.getTokenId(),
                        t.getType(),
                        t.getDate(),
                        t.getStatus().name()
                ));
            }
        }

        System.out.println("╠════════════════════╩════════════╩════════════╩══════════════════════╣");
        System.out.println("║ Press Enter to return...                                            ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

        FastInput.readLine();
    }
}
