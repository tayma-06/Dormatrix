package cli.forms;

import controllers.food.CafeteriaController;
import models.food.MealType;
import models.store.StudentBalance;
import utils.FastInput;
import utils.TimeManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MealTokenPurchase {
    private final CafeteriaController controller = new CafeteriaController();

    public void show(String username) {
        while (true) {
            // 1. Get Live Data
            LocalDate todayDate = LocalDate.now();
            MealType currentSlot = TimeManager.getCurrentMealSlot();
            String dayOfWeek = todayDate.getDayOfWeek().toString();

            // UPDATED: Now checks Calendar first, then Weekly Menu
            String menuItems = controller.getMenuForTime(todayDate, dayOfWeek, currentSlot);
            StudentBalance balance = controller.loadStudentBalance(username);

            // 2. Display Header
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════════════");
            System.out.println("|                      Meal Token Purchase                            |");
            System.out.println("═══════════════════════════════════════════════════════════════════════");
            System.out.println(" Date: " + todayDate + " | Time: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            System.out.println(" Current Status: " + (currentSlot == MealType.NONE ? "CLOSED" : "ACTIVE - " + currentSlot));
            System.out.println("═══════════════════════════════════════════════════════════════════════");
            System.out.println();

            if (currentSlot != MealType.NONE) {
                System.out.println(" TODAY'S MENU: " + menuItems );
            } else {
                System.out.println(" Cafeteria is currently closed. Check back during meal hours.");
            }

            System.out.println(" Your Balance: " + (balance != null ? balance.getBalance() : "N/A") + " BDT");
            System.out.println("═══════════════════════════════════════════════════════════════════════");
            System.out.println("║ [1] Buy Current Meal Token                                          ║");
            System.out.println("║ [2] View My Tokens                                                  ║");
            System.out.println("║ [0] Back to Dashboard                                               ║");
            System.out.println("═══════════════════════════════════════════════════════════════════════");
            System.out.println();
            System.out.print("Enter choice: ");

            int choice = FastInput.readInt();
            if (choice == 0) break;
            System.out.println();
            switch (choice) {
                case 1 -> {
                    if (currentSlot == MealType.NONE) {
                        System.out.println(">> No active meal to buy right now!");
                    } else {
                        String result = controller.purchaseToken(username);
                        System.out.println(">> " + result);
                    }
                }
                case 2 -> {
                    var tokens = controller.getStudentTokens(username);
                    System.out.println("═══════════════════════════════════════════════════════════════════════");
                    System.out.println("║                       YOUR PURCHASED TOKENS                         ║");
                    System.out.println("═══════════════════════════════════════════════════════════════════════");
                    if(tokens.isEmpty()) System.out.println("No tokens found.");
                    else tokens.forEach(t -> System.out.println(t.getTokenId() + " | " + t.getType() + " | " + t.getStatus()));
                }
                default -> System.out.println("Invalid choice.");
            }
            System.out.print("\nPress Enter to continue...");
            FastInput.readLine();
        }
    }
}