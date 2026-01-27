package cli.forms;

import controllers.food.MealTokenController;
import exceptions.InvalidTokenException;
import models.food.MealToken;

import java.util.Scanner;

public class MealTokenPurchase {

    private final MealTokenController mealTokenController;
    private final Scanner scanner;

    public MealTokenPurchase() {
        this.mealTokenController = new MealTokenController();
        this.scanner = new Scanner(System.in);
    }

    public void show(String username) {
        while (true) {
            System.out.println("------------------------------------------------");
            System.out.println("|              Meal Token System               |");
            System.out.println("------------------------------------------------");
            System.out.println("| 1. Buy Meal Token                            |");
            System.out.println("| 2. Validate Meal Token                       |");
            System.out.println("| 0. Back                                      |");
            System.out.println("------------------------------------------------");
            System.out.print("Enter choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    buyToken(username);
                    break;

                case 2:
                    validateToken();
                    break;

                case 0:
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private void buyToken(String username) {
        MealToken token = mealTokenController.buyToken(username);
        System.out.println("Token purchased successfully!");
        System.out.println("Token ID: " + token.getTokenId());
    }

    private void validateToken() {
        System.out.print("Enter Token ID: ");
        String tokenId = scanner.nextLine();

        try {
            mealTokenController.validate(tokenId);
            System.out.println("Token is valid. Enjoy your meal!");
        } catch (InvalidTokenException e) {
            System.out.println("Validation failed: " + e.getMessage());
        }
    }
}
