package cli.views.food;

import controllers.food.TokenPurchaseController;
import models.food.MealType;
import utils.FastInput;
import utils.TimeManager;
import utils.ConsoleUtil;
import utils.TerminalUI;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class CalendarView {

    private final TokenPurchaseController controller = new TokenPurchaseController();

    public void showWeeklyMenuAndPurchaseTokens(String username, LocalDate today) {
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);

        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);
            renderWeeklyCalendar(today, startOfWeek);

            TerminalUI.tEmpty();
            TerminalUI.tSubDashboard("ACTION SELECTION", new String[]{
                "[1-7] Select a specific day",
                "[8] Buy ALL meals for the rest of the week",
                "[0] Go back"
            });
            int choice = FastInput.readInt();

            if (choice == 0) {
                break;
            }

            if (choice >= 1 && choice <= 7) {
                LocalDate selectedDay = startOfWeek.plusDays(choice - 1);

                if (selectedDay.isBefore(today)) {
                    TerminalUI.tError("Cannot select a past day (marked with X).");
                    TerminalUI.tPause();
                } else {
                    handleSingleDayFlow(username, selectedDay);
                }
            } else if (choice == 8) {
                handleBulkPurchase(username, today, startOfWeek);
            } else {
                TerminalUI.tError("Invalid choice. Try again.");
            }
        }
    }

    public void renderWeeklyCalendar(LocalDate today, LocalDate startOfWeek) {
        TerminalUI.tEmpty();
        TerminalUI.tPanelCenter("╔════════════════════════════════════════════════════════════════════════════╗", TerminalUI.getActiveBoxColor());
        TerminalUI.tPanelCenter("║                             WEEKLY MEAL PLAN                               ║", TerminalUI.getActiveBoxColor());
        TerminalUI.tPanelCenter("╠══════════╦══════════╦══════════╦══════════╦══════════╦══════════╦══════════╣", TerminalUI.getActiveBoxColor());
        TerminalUI.tPanelCenter("║  [1]Mon  ║  [2]Tue  ║  [3]Wed  ║  [4]Thu  ║  [5]Fri  ║  [6]Sat  ║  [7]Sun  ║", TerminalUI.getActiveBoxColor());
        TerminalUI.tPanelCenter("╠══════════╬══════════╬══════════╬══════════╬══════════╬══════════╬══════════╣", TerminalUI.getActiveBoxColor());

        StringBuilder row = new StringBuilder("║");
        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = startOfWeek.plusDays(i);
            String dayDisplay;

            if (currentDay.isBefore(today)) {
                dayDisplay = "    X     ";
            } else {
                dayDisplay = String.format("    %02d    ", currentDay.getDayOfMonth());
            }
            row.append(dayDisplay).append("║");
        }
        TerminalUI.tPanelCenter(row.toString(), TerminalUI.getActiveBoxColor());
        TerminalUI.tPanelCenter("╚══════════╩══════════╩══════════╩══════════╩══════════╩══════════╩══════════╝", TerminalUI.getActiveBoxColor());
    }

    private void handleSingleDayFlow(String username, LocalDate day) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("SETTINGS FOR: " + day.getDayOfWeek() + " (" + day + ")");
        TerminalUI.tBoxSep();

        DayOfWeek dow = day.getDayOfWeek();

        if (TimeManager.isRamadanMode()) {
            printWrappedMenu("Suhoor", controller.getMenuForTime(day, dow.toString(), MealType.SUHOOR));
            TerminalUI.tBoxSep();
            printWrappedMenu("Iftar", controller.getMenuForTime(day, dow.toString(), MealType.IFTAR));
            TerminalUI.tBoxSep();
            printWrappedMenu("Dinner", controller.getMenuForTime(day, dow.toString(), MealType.DINNER));
        } else {
            printWrappedMenu("Breakfast", controller.getMenuForTime(day, dow.toString(), MealType.BREAKFAST));
            TerminalUI.tBoxSep();
            printWrappedMenu("Lunch", controller.getMenuForTime(day, dow.toString(), MealType.LUNCH));
            TerminalUI.tBoxSep();
            printWrappedMenu("Dinner", controller.getMenuForTime(day, dow.toString(), MealType.DINNER));
        }
        TerminalUI.tBoxBottom();

        buyDayToken(username, day);

        TerminalUI.tPause();
    }

    private void printWrappedMenu(String mealName, String menuItems) {
        String prefix = String.format("%-11s ", mealName + ":");
        int maxLineLength = 67 - prefix.length();

        if (menuItems == null || menuItems.isEmpty()) {
            TerminalUI.tBoxLine(prefix);
            return;
        }

        String[] words = menuItems.split(" ");
        StringBuilder currentLine = new StringBuilder();
        boolean firstLine = true;

        for (String word : words) {
            if (currentLine.length() + word.length() + (currentLine.length() > 0 ? 1 : 0) > maxLineLength) {

                String linePrefix = firstLine ? prefix : String.format("%" + prefix.length() + "s", "");
                TerminalUI.tBoxLine(linePrefix + currentLine.toString());
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
            TerminalUI.tBoxLine(linePrefix + currentLine.toString());
        }
    }

    private void handleBulkPurchase(String username, LocalDate today, LocalDate startOfWeek) {
        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("BULK PURCHASE MODE");
        TerminalUI.tBoxBottom();
        TerminalUI.tPrompt("Confirm buying all meals for all remaining days? (y/n): ");
        if (FastInput.readLine().trim().toLowerCase().equals("y")) {
            for (int i = 0; i < 7; i++) {
                LocalDate day = startOfWeek.plusDays(i);
                if (!day.isBefore(today)) {
                    TerminalUI.tPrint(day.getDayOfWeek() + " (" + day + "):");
                    autoBuyAllMeals(username, day);
                }
            }
            TerminalUI.tSuccess("Bulk purchase complete!");
            TerminalUI.tPause();
        }
    }

    private void buyDayToken(String username, LocalDate day) {
        MealType[] meals = getAvailableMeals();
        for (MealType mt : meals) {
            TerminalUI.tPrompt("Buy token for " + mt + "? (y/n): ");
            if (FastInput.readLine().trim().toLowerCase().startsWith("y")) {
                String result = controller.processTokenPurchaseForDay(username, day, mt);
                TerminalUI.tPrint("Status: " + result);
            }
        }
    }

    private void autoBuyAllMeals(String username, LocalDate day) {
        for (MealType mt : getAvailableMeals()) {
            String res = controller.processTokenPurchaseForDay(username, day, mt);
            TerminalUI.tPrint(mt + ": " + res);
        }
    }

    private MealType[] getAvailableMeals() {
        if (TimeManager.isRamadanMode()) {
            return new MealType[]{MealType.SUHOOR, MealType.IFTAR, MealType.DINNER};
        }
        return new MealType[]{MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER};
    }
}
