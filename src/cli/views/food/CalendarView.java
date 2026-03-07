package cli.views.food;

import controllers.food.TokenPurchaseController;
import models.food.MealType;
import utils.FastInput;
import utils.TimeManager;
import utils.ConsoleUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class CalendarView {

    private final TokenPurchaseController controller = new TokenPurchaseController();

    public void showWeeklyMenuAndPurchaseTokens(String username, LocalDate today) {
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);

        while (true) {
            ConsoleUtil.clearScreen();
            renderWeeklyCalendar(today, startOfWeek);

            System.out.println("\n[Action Selection]");
            System.out.println(">> Enter 1-7 to select a specific day");
            System.out.println(">> Enter 8 to buy ALL meals for the rest of the week");
            System.out.println(">> Enter 0 to go back");

            System.out.print("\nChoice: ");
            int choice = FastInput.readInt();

            if (choice == 0) {
                break;
            }

            if (choice >= 1 && choice <= 7) {
                LocalDate selectedDay = startOfWeek.plusDays(choice - 1);

                if (selectedDay.isBefore(today)) {
                    System.out.println("\n[!] Error: Cannot select a past day (marked with X).");
                    System.out.print("Press Enter to continue...");
                    FastInput.readLine();
                } else {
                    handleSingleDayFlow(username, selectedDay);
                }
            } else if (choice == 8) {
                handleBulkPurchase(username, today, startOfWeek);
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public void renderWeeklyCalendar(LocalDate today, LocalDate startOfWeek) {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                             WEEKLY MEAL PLAN                               ║");
        System.out.println("╠══════════╦══════════╦══════════╦══════════╦══════════╦══════════╦══════════╣");
        System.out.println("║  [1]Mon  ║  [2]Tue  ║  [3]Wed  ║  [4]Thu  ║  [5]Fri  ║  [6]Sat  ║  [7]Sun  ║");
        System.out.println("╠══════════╬══════════╬══════════╬══════════╬══════════╬══════════╬══════════╣");

        System.out.print("║");
        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = startOfWeek.plusDays(i);
            String dayDisplay;

            if (currentDay.isBefore(today)) {
                dayDisplay = "    X     ";
            } else {
                dayDisplay = String.format("    %02d    ", currentDay.getDayOfMonth());
            }
            System.out.print(dayDisplay + "║");
        }
        System.out.println("\n╚══════════╩══════════╩══════════╩══════════╩══════════╩══════════╩══════════╝");
    }

    private void handleSingleDayFlow(String username, LocalDate day) {
        ConsoleUtil.clearScreen();
        System.out.println();
        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ SETTINGS FOR: " + String.format("%-54s", day.getDayOfWeek() + " (" + day + ")") + "║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");

        DayOfWeek dow = day.getDayOfWeek();

        if (TimeManager.isRamadanMode()) {
            printWrappedMenu("Suhoor", controller.getMenuForTime(day, dow.toString(), MealType.SUHOOR));
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            printWrappedMenu("Iftar", controller.getMenuForTime(day, dow.toString(), MealType.IFTAR));
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            printWrappedMenu("Dinner", controller.getMenuForTime(day, dow.toString(), MealType.DINNER));
        } else {
            printWrappedMenu("Breakfast", controller.getMenuForTime(day, dow.toString(), MealType.BREAKFAST));
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            printWrappedMenu("Lunch", controller.getMenuForTime(day, dow.toString(), MealType.LUNCH));
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            printWrappedMenu("Dinner", controller.getMenuForTime(day, dow.toString(), MealType.DINNER));
        }
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

        buyDayToken(username, day);

        System.out.println("\nDone! Press Enter to continue...");
        FastInput.readLine();
    }

    private void printWrappedMenu(String mealName, String menuItems) {
        String prefix = String.format("%-11s ", mealName + ":");
        int maxLineLength = 67 - prefix.length();

        if (menuItems == null || menuItems.isEmpty()) {
            System.out.printf("║ %-67s ║%n", prefix);
            return;
        }

        String[] words = menuItems.split(" ");
        StringBuilder currentLine = new StringBuilder();
        boolean firstLine = true;

        for (String word : words) {
            if (currentLine.length() + word.length() + (currentLine.length() > 0 ? 1 : 0) > maxLineLength) {

                String linePrefix = firstLine ? prefix : String.format("%" + prefix.length() + "s", "");
                System.out.printf("║ %-67s ║%n", linePrefix + currentLine.toString());
                currentLine = new StringBuilder(word);
                firstLine = false;
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        if (currentLine.length() > 0 || firstLine) {
            String linePrefix = firstLine ? prefix : String.format("%" + prefix.length() + "s", "");
            System.out.printf("║ %-67s ║%n", linePrefix + currentLine.toString());
        }
    }

    private void handleBulkPurchase(String username, LocalDate today, LocalDate startOfWeek) {
        System.out.println("\n[Bulk Purchase Mode]");
        System.out.print("Confirm buying all meals for all remaining days? (y/n): ");
        if (FastInput.readLine().trim().toLowerCase().equals("y")) {
            for (int i = 0; i < 7; i++) {
                LocalDate day = startOfWeek.plusDays(i);
                if (!day.isBefore(today)) {
                    System.out.println("\n>> " + day.getDayOfWeek() + " (" + day + "):");
                    autoBuyAllMeals(username, day);
                }
            }
            System.out.println("\nBulk purchase complete!");
            System.out.print("Press Enter...");
            FastInput.readLine();
        }
    }

    private void buyDayToken(String username, LocalDate day) {
        MealType[] meals = getAvailableMeals();
        for (MealType mt : meals) {
            System.out.print(">> Buy token for " + mt + "? (y/n): ");
            if (FastInput.readLine().trim().toLowerCase().startsWith("y")) {
                String result = controller.processTokenPurchaseForDay(username, day, mt);
                System.out.println("   Status: " + result);
            }
        }
    }

    private void autoBuyAllMeals(String username, LocalDate day) {
        for (MealType mt : getAvailableMeals()) {
            String res = controller.processTokenPurchaseForDay(username, day, mt);
            System.out.println("   - " + mt + ": " + res);
        }
    }

    private MealType[] getAvailableMeals() {
        if (TimeManager.isRamadanMode()) {
            return new MealType[]{MealType.SUHOOR, MealType.IFTAR, MealType.DINNER};
        }
        return new MealType[]{MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER};
    }
}
