package cli.forms.food;

import cli.views.food.CalendarView;
import cli.views.food.TokenListView;
import controllers.food.TokenPurchaseController;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import models.food.MealType;
import models.store.StudentBalance;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TimeManager;

public class MealTokenPurchase {

    private final TokenPurchaseController purchaseController = new TokenPurchaseController();
    private final CalendarView calendarView = new CalendarView();

    public void show(String username) {
        while (true) {
            ConsoleUtil.clearScreen();
            LocalDate todayDate = TimeManager.nowDate();
            MealType currentSlot = TimeManager.getCurrentMealSlot();
            String dayOfWeek = TimeManager.nowDay().toString();
            String menuItems = purchaseController.getMenuForTime(todayDate, dayOfWeek, currentSlot);
            StudentBalance balance = purchaseController.getStudentBalance(username);
            System.out.println();
            System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                          MEAL TOKEN PURCHASE                        ║");
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");

            String dateTimeLine = "Date: " + todayDate
                    + " | Time: " + TimeManager.nowTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.out.printf("║ %-67s ║%n", dateTimeLine);

            String statusLine = "Current Status: "
                    + (currentSlot == MealType.NONE ? "CLOSED" : "ACTIVE - " + currentSlot);
            System.out.printf("║ %-67s ║%n", statusLine);

            String progressLine = utils.CafeteriaAsciiUI.renderSlotProgress(currentSlot);
            System.out.printf("║ %-67s ║%n", progressLine);

            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");

            if (currentSlot != MealType.NONE) {
                String menuLine = "Today's Menu: " + menuItems;
                System.out.printf("║ %-67s ║%n", menuLine);
            } else {
                System.out.printf("║ %-67s ║%n", "Cafeteria is currently closed.");
            }

            String balanceLine = "Your Balance: "
                    + (balance != null ? balance.getBalance() : "N/A") + " BDT";
            System.out.printf("║ %-67s ║%n", balanceLine);

            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ [1] Buy Current Meal Token                                          ║");
            System.out.println("║ [2] Buy Tokens for the Week (Monday-Sunday)                         ║");
            System.out.println("║ [3] View My Tokens                                                  ║");
            System.out.println("║ [0] Back to Dashboard                                               ║");
            System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

            System.out.println();
            System.out.print("Enter choice: ");

            int choice = FastInput.readInt();
            if (choice == 0) {
                break;
            }

            boolean skipOuterPause = false;

            System.out.println();
            switch (choice) {
                case 1 -> {
                    if (currentSlot == MealType.NONE) {
                        System.out.println(">> No active meal to buy right now!");
                    } else {
                        String result = purchaseController.processTokenPurchase(username);
                        System.out.println(">> " + result);
                    }
                }
                case 2 -> {
                    calendarView.showWeeklyMenuAndPurchaseTokens(username, todayDate);
                    skipOuterPause = true;
                }
                case 3 -> {
                    TokenListView tokenListView = new TokenListView();
                    tokenListView.show(username);
                    skipOuterPause = true;
                }
                default -> System.out.println("Invalid choice.");
            }

            if (!skipOuterPause) {
                System.out.println();
                System.out.print("Press Enter to continue...");
                FastInput.readLine();
            }
        }
    }
}