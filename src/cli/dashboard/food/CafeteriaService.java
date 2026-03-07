package cli.dashboard.food;

import controllers.food.MenuManagementController;
import java.util.List;
import models.food.DailyMenu;
import models.food.MealType;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;
import utils.TimeManager;

public class CafeteriaService {

    private final MenuManagementController menuController = new MenuManagementController();
    private final String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};

    public void showWeeklyMenuUI() {
        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);

            List<DailyMenu> currentMenu = menuController.getWeeklyMenuData();
            renderWeeklyBoxes(currentMenu);

            TerminalUI.tEmpty();
            TerminalUI.tSubDashboard("SELECT DAY TO EDIT", new String[]{
                "[1] Monday", "[2] Tuesday", "[3] Wednesday", "[4] Thursday",
                "[5] Friday", "[6] Saturday", "[7] Sunday", "[0] Back"
            });
            int choice = FastInput.readInt();

            if (choice == 0) {
                ConsoleUtil.clearScreen();
                return;
            }

            if (choice >= 1 && choice <= 7) {
                promptDayEdit(days[choice - 1], currentMenu);
            }
        }
    }

    private void renderWeeklyBoxes(List<DailyMenu> currentMenu) {
        boolean isRamadan = TimeManager.isRamadanMode();

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("WEEKLY MENU PREVIEW");
        TerminalUI.tBoxBottom();

        for (int i = 0; i < days.length; i++) {
            String day = days[i];

            TerminalUI.tBoxTop();
            TerminalUI.tBoxLine("[" + (i + 1) + "] MENU FOR: " + day);
            TerminalUI.tBoxSep();

            if (isRamadan) {
                printWrappedMenu("Suhoor", extractMenuText(currentMenu, day, MealType.SUHOOR));
                TerminalUI.tBoxSep();
                printWrappedMenu("Iftar", extractMenuText(currentMenu, day, MealType.IFTAR));
                TerminalUI.tBoxSep();
                printWrappedMenu("Dinner", extractMenuText(currentMenu, day, MealType.DINNER));
            } else {
                printWrappedMenu("Breakfast", extractMenuText(currentMenu, day, MealType.BREAKFAST));
                TerminalUI.tBoxSep();
                printWrappedMenu("Lunch", extractMenuText(currentMenu, day, MealType.LUNCH));
                TerminalUI.tBoxSep();
                printWrappedMenu("Dinner", extractMenuText(currentMenu, day, MealType.DINNER));
            }
            TerminalUI.tBoxBottom();
        }
    }

    private void promptDayEdit(String day, List<DailyMenu> currentMenu) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);

        boolean isRamadan = TimeManager.isRamadanMode();

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("SETTINGS FOR: " + day);
        TerminalUI.tBoxSep();

        if (isRamadan) {
            printWrappedMenu("Suhoor", extractMenuText(currentMenu, day, MealType.SUHOOR));
            TerminalUI.tBoxSep();
            printWrappedMenu("Iftar", extractMenuText(currentMenu, day, MealType.IFTAR));
            TerminalUI.tBoxSep();
            printWrappedMenu("Dinner", extractMenuText(currentMenu, day, MealType.DINNER));
        } else {
            printWrappedMenu("Breakfast", extractMenuText(currentMenu, day, MealType.BREAKFAST));
            TerminalUI.tBoxSep();
            printWrappedMenu("Lunch", extractMenuText(currentMenu, day, MealType.LUNCH));
            TerminalUI.tBoxSep();
            printWrappedMenu("Dinner", extractMenuText(currentMenu, day, MealType.DINNER));
        }
        TerminalUI.tBoxBottom();

        TerminalUI.tEmpty();
        String[] mealItems;
        if (isRamadan) {
            mealItems = new String[]{"[1] Suhoor", "[2] Iftar", "[3] Dinner", "[0] Cancel"};
        } else {
            mealItems = new String[]{"[1] Breakfast", "[2] Lunch", "[3] Dinner", "[0] Cancel"};
        }
        TerminalUI.tSubDashboard("SELECT MEAL TO EDIT", mealItems);
        int mealChoice = FastInput.readInt();

        if (mealChoice == 0) {
            return;
        }

        MealType type = null;
        if (isRamadan) {
            type = switch (mealChoice) {
                case 1 ->
                    MealType.SUHOOR;
                case 2 ->
                    MealType.IFTAR;
                case 3 ->
                    MealType.DINNER;
                default ->
                    null;
            };
        } else {
            type = switch (mealChoice) {
                case 1 ->
                    MealType.BREAKFAST;
                case 2 ->
                    MealType.LUNCH;
                case 3 ->
                    MealType.DINNER;
                default ->
                    null;
            };
        }

        if (type != null) {
            TerminalUI.tPrompt("Enter new items: ");
            String items = FastInput.readNonEmptyLine();

            String result = menuController.processSingleMealUpdate(day, type, items);
            TerminalUI.tEmpty();
            TerminalUI.tSuccess(result);
            TerminalUI.tPause();
        } else {
            TerminalUI.tError("Invalid selection.");
            TerminalUI.tPause();
        }
    }

    private void printWrappedMenu(String mealName, String menuItems) {
        String prefix = mealName + ": ";
        int maxLen = 55;

        if (menuItems == null || menuItems.isEmpty() || menuItems.equals("---")) {
            TerminalUI.tBoxLine(prefix + "(Not set)");
            return;
        }

        String[] words = menuItems.split(" ");
        StringBuilder currentLine = new StringBuilder();
        boolean firstLine = true;

        for (String word : words) {
            if (currentLine.length() + word.length() + (currentLine.length() > 0 ? 1 : 0) > maxLen) {
                String linePrefix = firstLine ? prefix : " ".repeat(prefix.length());
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
            String linePrefix = firstLine ? prefix : " ".repeat(prefix.length());
            TerminalUI.tBoxLine(linePrefix + currentLine.toString());
        }
    }

    public void showSpecialEventUI() {
        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("SCHEDULE SPECIAL EVENT");
        TerminalUI.tBoxBottom();
        TerminalUI.tPrompt("Enter Date (YYYY-MM-DD): ");
        String dateStr = FastInput.readNonEmptyLine();

        TerminalUI.tEmpty();
        TerminalUI.tSubDashboard("SELECT MEAL TYPE", new String[]{
            "[1] Lunch", "[2] Dinner"
        });
        int typeChoice = FastInput.readInt();

        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("ENTER SPECIAL MENU ITEMS");
        TerminalUI.tBoxBottom();
        TerminalUI.tPrompt("Items: ");
        String items = FastInput.readNonEmptyLine();

        TerminalUI.tEmpty();
        String result = menuController.processSpecialEvent(dateStr, typeChoice, items);
        TerminalUI.tSuccess(result);
        TerminalUI.tPause();
    }

    private String extractMenuText(List<DailyMenu> menu, String day, MealType type) {
        if (menu == null) {
            return "---";
        }
        for (DailyMenu m : menu) {
            if (m.getDay().equalsIgnoreCase(day) && m.getType() == type) {
                return m.getItems();
            }
        }
        return "---";
    }
}
