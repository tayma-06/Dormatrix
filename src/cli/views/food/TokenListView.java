package cli.views.food;

import controllers.food.CafeteriaController;
import models.food.MealToken;

import java.util.List;

public class TokenListView {
    private final CafeteriaController controller = new CafeteriaController();

    public void show(String username) {
        List<MealToken> tokens = controller.getStudentTokens(username);

        System.out.println("========== YOUR MEAL TOKENS ==========");

        if (tokens.isEmpty()) {
            System.out.println(" No tokens found. Go buy some delicious food!");
        } else {
            System.out.printf("%-18s | %-10s | %-10s | %-8s\n", "TOKEN ID", "MEAL", "DATE", "STATUS");
            System.out.println("----------------------------------------------------------");

            for (MealToken t : tokens) {
                String status = t.isUsed() ? "USED": "ACTIVE";

                System.out.printf("%-18s | %-10s | %-10s | %-8s\n",
                        t.getTokenId(), t.getType(), t.getDate(), status);
            }
        }
        System.out.println("======================================");
        System.out.println("Press Enter to return...");
        utils.FastInput.readLine();
    }
}