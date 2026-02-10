package cli.forms.food;

import cli.views.food.TokenListView;
import controllers.food.CafeteriaController;
import models.food.MealType;
import models.store.StudentBalance;
import utils.FastInput;
import utils.TimeManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MealTokenPurchase {
    private final CafeteriaController controller = new CafeteriaController();

    public void show(String username) {
        while (true) {
            LocalDate todayDate = TimeManager.nowDate();
            MealType currentSlot = TimeManager.getCurrentMealSlot();
            String dayOfWeek = TimeManager.nowDay().toString();

            String menuItems = controller.getMenuForTime(todayDate, dayOfWeek, currentSlot);
            StudentBalance balance = controller.loadStudentBalance(username);

            System.out.println();
            System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                        MEAL TOKEN PURCHASE                          ║");
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");

            String dateTimeLine = "Date: " + todayDate +
                    " | Time: " + TimeManager.nowTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.out.println(String.format("║ %-67s ║", dateTimeLine));

            String statusLine = "Current Status: " +
                    (currentSlot == MealType.NONE ? "CLOSED" : "ACTIVE - " + currentSlot);
            System.out.println(String.format("║ %-67s ║", statusLine));

            String progressLine = utils.CafeteriaAsciiUI.renderSlotProgress(currentSlot);
            System.out.println(String.format("║ %-67s ║", progressLine));

            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");

            if (currentSlot != MealType.NONE) {
                String menuLine = "Today's Menu: " + menuItems;
                System.out.println(String.format("║ %-67s ║", menuLine));
            } else {
                System.out.println(String.format("║ %-67s ║",
                        "Cafeteria is currently closed. Check back during meal hours."
                ));
            }

            String balanceLine = "Your Balance: " +
                    (balance != null ? balance.getBalance() : "N/A") + " BDT";
            System.out.println(String.format("║ %-67s ║", balanceLine));

            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ [1] Buy Current Meal Token                                          ║");
            System.out.println("║ [2] Buy Tokens for the Week (Monday-Sunday)                         ║");
            System.out.println("║ [3] View My Tokens                                                  ║");
            System.out.println("║ [0] Back to Dashboard                                               ║");
            System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

            System.out.println();
            System.out.print("Enter choice: ");

            int choice = FastInput.readInt();
            if (choice == 0) break;

            boolean skipOuterPause = false;

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
                    buyWeeklyTokens(username);
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

    private void buyWeeklyTokens(String username) {
        LocalDate today = LocalDate.now();
        renderWeeklyCalendar(today);
        for (int i = 0; i < 7; i++) {
            LocalDate day = today.plusDays(i);
            DayOfWeek dayOfWeek = day.getDayOfWeek();
            System.out.println("\n>> " + dayOfWeek + " (" + day + ")");
            String dayMenu = controller.getMenuForTime(day, dayOfWeek.toString(), MealType.BREAKFAST);
            System.out.println("   Breakfast: " + dayMenu);
            dayMenu = controller.getMenuForTime(day, dayOfWeek.toString(), MealType.LUNCH);
            System.out.println("   Lunch: " + dayMenu);
            dayMenu = controller.getMenuForTime(day, dayOfWeek.toString(), MealType.DINNER);
            System.out.println("   Dinner: " + dayMenu);
            System.out.print("   Would you like to buy tokens for this day? (yes/no): ");
            String answer = FastInput.readLine().trim().toLowerCase();
            if (answer.equals("yes") || answer.equals("y")) {
                buyDayToken(username, day);
            }
        }
    }

    private void renderWeeklyCalendar(LocalDate today) {
        System.out.println();
        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                          WEEKLY MEAL PLAN                           ║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
        System.out.println("║             | Mon | Tue | Wed | Thu | Fri | Sat | Sun |             ║");

        System.out.print("║             | ");
        for (int i = 0; i < 7; i++) {
            LocalDate day = today.plusDays(i);
            String status = (day.isBefore(LocalDate.now())) ? " X " : String.format(" %s ", day.getDayOfMonth());
            System.out.print(status + "| ");
        }
        System.out.println();
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
    }

    private void buyDayToken(String username, LocalDate day) {
        System.out.println("Buying tokens for: " + day);
        for (MealType mealType : MealType.values()) {
            if (mealType == MealType.NONE) continue;

            System.out.print("   Would you like to buy " + mealType + " token? (yes/no): ");
            String answer = FastInput.readLine().trim().toLowerCase();
            if (answer.equals("yes") || answer.equals("y")) {
                String result = controller.purchaseTokenForDay(username, day, mealType);
                System.out.println(">> " + result);
            }
        }
    }
}
