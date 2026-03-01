package cli.dashboard.food;

import controllers.food.MenuManagementController;
import models.food.DailyMenu;
import models.food.MealType;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TimeManager;

import java.util.List;

public class CafeteriaService {
    private final MenuManagementController menuController = new MenuManagementController();
    private final String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};

    public void showWeeklyMenuUI() {
        while (true) {
            ConsoleUtil.clearScreen();
            List<DailyMenu> currentMenu = menuController.getWeeklyMenuData();
            renderWeeklyBoxes(currentMenu);

            System.out.println();
            System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                          SELECT DAY TO EDIT                         ║");
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ [1] Monday                                                          ║");
            System.out.println("║ [2] Tuesday                                                         ║");
            System.out.println("║ [3] Wednesday                                                       ║");
            System.out.println("║ [4] Thursday                                                        ║");
            System.out.println("║ [5] Friday                                                          ║");
            System.out.println("║ [6] Saturday                                                        ║");
            System.out.println("║ [7] Sunday                                                          ║");
            System.out.println("║ [0] Back                                                            ║");
            System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
            System.out.print("Choice: ");
            int choice = FastInput.readInt();

            if (choice == 0) return;

            if (choice >= 1 && choice <= 7) {
                promptDayEdit(days[choice - 1], currentMenu);
            }
        }
    }

    private void renderWeeklyBoxes(List<DailyMenu> currentMenu) {
        boolean isRamadan = TimeManager.isRamadanMode();
        System.out.println();
        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         WEEKLY MENU PREVIEW                         ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

        for (int i = 0; i < days.length; i++) {
            String day = days[i];

            System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
            System.out.println("║ [" + (i + 1) + "] MENU FOR: " + String.format("%-54s", day) + "║");
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");

            if (isRamadan) {
                printWrappedMenu("Suhoor", extractMenuText(currentMenu, day, MealType.SUHOOR));
                System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
                printWrappedMenu("Iftar", extractMenuText(currentMenu, day, MealType.IFTAR));
                System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
                printWrappedMenu("Dinner", extractMenuText(currentMenu, day, MealType.DINNER));
            } else {
                printWrappedMenu("Breakfast", extractMenuText(currentMenu, day, MealType.BREAKFAST));
                System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
                printWrappedMenu("Lunch", extractMenuText(currentMenu, day, MealType.LUNCH));
                System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
                printWrappedMenu("Dinner", extractMenuText(currentMenu, day, MealType.DINNER));
            }
            System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
        }
    }

    private void promptDayEdit(String day, List<DailyMenu> currentMenu) {
        ConsoleUtil.clearScreen();
        System.out.println();

        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ SETTINGS FOR: " + String.format("%-54s", day) + "║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");

        boolean isRamadan = TimeManager.isRamadanMode();

        if (isRamadan) {
            printWrappedMenu("Suhoor", extractMenuText(currentMenu, day, MealType.SUHOOR));
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            printWrappedMenu("Iftar", extractMenuText(currentMenu, day, MealType.IFTAR));
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            printWrappedMenu("Dinner", extractMenuText(currentMenu, day, MealType.DINNER));
        } else {
            printWrappedMenu("Breakfast", extractMenuText(currentMenu, day, MealType.BREAKFAST));
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            printWrappedMenu("Lunch", extractMenuText(currentMenu, day, MealType.LUNCH));
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            printWrappedMenu("Dinner", extractMenuText(currentMenu, day, MealType.DINNER));
        }
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

        System.out.println();
        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         SELECT MEAL TO EDIT                         ║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
        if (isRamadan) {
            System.out.println("║ [1] Suhoor                                                          ║");
            System.out.println("║ [2] Iftar                                                           ║");
            System.out.println("║ [3] Dinner                                                          ║");
            System.out.println("║ [0] Cancel                                                          ║");
        } else {
            System.out.println("║ [1] Breakfast                                                       ║");
            System.out.println("║ [2] Lunch                                                           ║");
            System.out.println("║ [3] Dinner                                                          ║");
            System.out.println("║ [0] Cancel                                                          ║");
        }
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.print("Choice: ");
        int mealChoice = FastInput.readInt();

        if (mealChoice == 0) return;

        MealType type = null;
        if (isRamadan) {
            type = switch (mealChoice) {
                case 1 -> MealType.SUHOOR;
                case 2 -> MealType.IFTAR;
                case 3 -> MealType.DINNER;
                default -> null;
            };
        } else {
            type = switch (mealChoice) {
                case 1 -> MealType.BREAKFAST;
                case 2 -> MealType.LUNCH;
                case 3 -> MealType.DINNER;
                default -> null;
            };
        }

        if (type != null) {
            System.out.print("Enter new items: ");
            String items = FastInput.readNonEmptyLine();

            String result = menuController.processSingleMealUpdate(day, type, items);
            System.out.println("\n" + result);

            System.out.print("Press Enter to continue...");
            FastInput.readLine();
        } else {
            System.out.println("Invalid selection.");
            System.out.print("Press Enter to continue...");
            FastInput.readLine();
        }
    }

    private void printWrappedMenu(String mealName, String menuItems) {
        String prefix = String.format("%-11s ", mealName + ":");
        int maxLineLength = 67 - prefix.length();

        if (menuItems == null || menuItems.isEmpty() || menuItems.equals("---")) {
            System.out.printf("║ %-67s ║%n", prefix + "(Not set)");
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
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            }
        }
        if (currentLine.length() > 0 || firstLine) {
            String linePrefix = firstLine ? prefix : String.format("%" + prefix.length() + "s", "");
            System.out.printf("║ %-67s ║%n", linePrefix + currentLine.toString());
        }
    }

    public void showSpecialEventUI() {
        System.out.println();
        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                       SCHEDULE SPECIAL EVENT                        ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
        System.out.print(" Enter Date (YYYY-MM-DD): ");
        String dateStr = FastInput.readNonEmptyLine();

        System.out.println();
        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         SELECT MEAL TYPE                            ║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
        System.out.println("║ [1] Lunch                                                           ║");
        System.out.println("║ [2] Dinner                                                          ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
        System.out.print(" Choice: ");
        int typeChoice = FastInput.readInt();

        System.out.println();
        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      ENTER SPECIAL MENU ITEMS                       ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
        System.out.print(" Items: ");
        String items = FastInput.readNonEmptyLine();

        System.out.println();
        System.out.println("=======================================================================");
        String result = menuController.processSpecialEvent(dateStr, typeChoice, items);
        System.out.println(" " + result);
        System.out.println("=======================================================================");
        FastInput.readLine();
    }

    private String extractMenuText(List<DailyMenu> menu, String day, MealType type) {
        if (menu == null) return "---";
        for (DailyMenu m : menu) {
            if (m.getDay().equalsIgnoreCase(day) && m.getType() == type) return m.getItems();
        }
        return "---";
    }
}