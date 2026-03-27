package cli.views.food;

import controllers.food.MenuManagementController;
import controllers.food.TokenPurchaseController;
import models.food.DailyMenu;
import models.food.MealType;
import utils.FastInput;
import utils.TimeManager;
import utils.ConsoleUtil;
import utils.TerminalUI;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class CalendarView {

    private final TokenPurchaseController controller = new TokenPurchaseController();
    private final MenuManagementController menuController = new MenuManagementController();
    private static final String[] DAYS = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};

    public void showWeeklyMenuAndPurchaseTokens(String username, LocalDate today) {
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);

        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);

            List<DailyMenu> weeklyMenu = menuController.getWeeklyMenuData();
            renderWeeklyMenuBoxes(weeklyMenu, startOfWeek);
            renderWeeklyCalendar(today, startOfWeek);

            TerminalUI.tEmpty();
            TerminalUI.tSubDashboard("ACTION SELECTION", new String[]{
                "[1-7] Select a specific day to buy tokens",
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
                    TerminalUI.tError("Cannot select a past day.");
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

    private void renderWeeklyMenuBoxes(List<DailyMenu> weeklyMenu, LocalDate startOfWeek) {
        boolean isRamadan = TimeManager.isRamadanMode();
        String box = TerminalUI.getActiveBoxColor();
        String txt = TerminalUI.getActiveTextColor();
        String dim = utils.ConsoleColors.Accent.MUTED;
        String BG = TerminalUI.getActiveBgColor();
        String RESET = utils.ConsoleColors.RESET;
        String BOLD = utils.ConsoleColors.BOLD;

        int dw = 12, mw = 26;
        // visual width (no ANSI): ║ dw ║ mw ║ mw ║ mw ║
        int visualW = 1 + dw + 1 + mw + 1 + mw + 1 + mw + 1;
        int col = TerminalUI.centerCol(visualW);

        String hLine = "╠" + "═".repeat(dw) + "╬" + "═".repeat(mw) + "╬" + "═".repeat(mw) + "╬" + "═".repeat(mw) + "╣";
        String botLine = "╚" + "═".repeat(dw) + "╩" + "═".repeat(mw) + "╩" + "═".repeat(mw) + "╩" + "═".repeat(mw) + "╝";
        int fullInner = dw + 1 + mw + 1 + mw + 1 + mw;
        String bannerTop = "╔" + "═".repeat(fullInner) + "╗";
        String bannerToGrid = "╠" + "═".repeat(dw) + "╦" + "═".repeat(mw) + "╦" + "═".repeat(mw) + "╦" + "═".repeat(mw) + "╣";

        String m1h = isRamadan ? "SUHOOR" : "BREAKFAST";
        String m2h = isRamadan ? "IFTAR" : "LUNCH";
        MealType mt1 = isRamadan ? MealType.SUHOOR : MealType.BREAKFAST;
        MealType mt2 = isRamadan ? MealType.IFTAR : MealType.LUNCH;
        MealType mt3 = MealType.DINNER;

        tRow(col, BG + box + bannerTop + RESET);
        tRow(col, BG + box + "║" + RESET + BG + txt + BOLD + padC("WEEKLY MEAL PLAN", fullInner) + RESET + BG + box + "║" + RESET);
        tRow(col, BG + box + bannerToGrid + RESET);
        tRow(col, BG + box + "║" + RESET + BG + txt + BOLD + padC("DAY", dw) + RESET
                + BG + box + "║" + RESET + BG + txt + BOLD + padC(m1h, mw) + RESET
                + BG + box + "║" + RESET + BG + txt + BOLD + padC(m2h, mw) + RESET
                + BG + box + "║" + RESET + BG + txt + BOLD + padC("DINNER", mw) + RESET
                + BG + box + "║" + RESET);
        tRow(col, BG + box + hLine + RESET);

        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            String dayLabel = DAYS[i];
            String shortDay = "[" + (i + 1) + "] " + dayLabel.substring(0, 3) + " " + String.format("%02d", day.getDayOfMonth());

            String[] c1 = wrapCells(extractMenuText(weeklyMenu, dayLabel, mt1), mw);
            String[] c2 = wrapCells(extractMenuText(weeklyMenu, dayLabel, mt2), mw);
            String[] c3 = wrapCells(extractMenuText(weeklyMenu, dayLabel, mt3), mw);
            int rowCount = Math.max(1, Math.max(c1.length, Math.max(c2.length, c3.length)));
            String cellColor = dim;
            String dayColor = txt;
            for (int r = 0; r < rowCount; r++) {
                String dc = r == 0 ? padR(shortDay, dw) : " ".repeat(dw);
                String a = r < c1.length ? c1[r] : " ".repeat(mw);
                String b = r < c2.length ? c2[r] : " ".repeat(mw);
                String c = r < c3.length ? c3[r] : " ".repeat(mw);
                tRow(col, BG + box + "║" + RESET + BG + dayColor + dc + RESET
                        + BG + box + "║" + RESET + BG + cellColor + a + RESET
                        + BG + box + "║" + RESET + BG + cellColor + b + RESET
                        + BG + box + "║" + RESET + BG + cellColor + c + RESET
                        + BG + box + "║" + RESET);
            }
            if (i < 6) {
                tRow(col, BG + box + hLine + RESET);
            }
        }
        tRow(col, BG + box + botLine + RESET);
    }

    private void tRow(int col, String content) {
        System.out.print("\u001B[" + col + "G" + content + "\n");
        System.out.flush();
    }

    private String padR(String s, int w) {
        if (s.length() >= w) {
            return s.substring(0, w);
        }
        return s + " ".repeat(w - s.length());
    }

    private String padC(String s, int w) {
        int total = w - s.length();
        int left = total / 2, right = total - left;
        return " ".repeat(Math.max(0, left)) + s + " ".repeat(Math.max(0, right));
    }

    private String[] wrapCells(String text, int w) {
        if (text == null || text.isEmpty() || text.equals("---")) {
            return new String[]{padC("—", w)};
        }
        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder cur = new StringBuilder();
        for (String word : words) {
            if (cur.length() + word.length() + (cur.length() > 0 ? 1 : 0) > w - 2) {
                lines.add(" " + cur + " ".repeat(Math.max(0, w - 1 - cur.length())));
                cur = new StringBuilder(word);
            } else {
                if (cur.length() > 0) {
                    cur.append(" ");
                }
                cur.append(word);
            }
        }
        if (cur.length() > 0) {
            lines.add(" " + cur + " ".repeat(Math.max(0, w - 1 - cur.length())));
        }
        return lines.toArray(new String[0]);
    }

    // kept for any other callers that still use the compact grid
    public void renderWeeklyCalendar(LocalDate today, LocalDate startOfWeek) {
        TerminalUI.tEmpty();
        TerminalUI.tPanelCenter("╔════════════════════════════════════════════════════════════════════════════╗", TerminalUI.getActiveBoxColor());
        TerminalUI.tPanelCenter("║                            WEEKLY CALENDAR                                 ║", TerminalUI.getActiveBoxColor());
        TerminalUI.tPanelCenter("╠══════════╦══════════╦══════════╦══════════╦══════════╦══════════╦══════════╣", TerminalUI.getActiveBoxColor());
        TerminalUI.tPanelCenter("║  [1]Mon  ║  [2]Tue  ║  [3]Wed  ║  [4]Thu  ║  [5]Fri  ║  [6]Sat  ║  [7]Sun  ║", TerminalUI.getActiveBoxColor());
        TerminalUI.tPanelCenter("╠══════════╬══════════╬══════════╬══════════╬══════════╬══════════╬══════════╣", TerminalUI.getActiveBoxColor());

        StringBuilder row = new StringBuilder("║");
        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = startOfWeek.plusDays(i);
            String dayDisplay = currentDay.isBefore(today)
                    ? "    X     "
                    : String.format("    %02d    ", currentDay.getDayOfMonth());
            row.append(dayDisplay).append("║");
        }
        TerminalUI.tPanelCenter(row.toString(), TerminalUI.getActiveBoxColor());
        TerminalUI.tPanelCenter("╚══════════╩══════════╩══════════╩══════════╩══════════╩══════════╩══════════╝", TerminalUI.getActiveBoxColor());
    }

    private void handleSingleDayFlow(String username, LocalDate day) {
        DayOfWeek dow = day.getDayOfWeek();

        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);
            TerminalUI.tBoxTop();
            TerminalUI.tBoxTitle("MENU FOR: " + day.getDayOfWeek() + " (" + day + ")");
            TerminalUI.tBoxSep();

            boolean isRamadan = TimeManager.isRamadanMode();
            if (isRamadan) {
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

            TerminalUI.tEmpty();
            String[] tokenOptions;
            if (isRamadan) {
                tokenOptions = new String[]{
                    "[1] Buy Suhoor Token", "[2] Buy Iftar Token", "[3] Buy Dinner Token",
                    "[4] Buy All Tokens", "[0] Back"
                };
            } else {
                tokenOptions = new String[]{
                    "[1] Buy Breakfast Token", "[2] Buy Lunch Token", "[3] Buy Dinner Token",
                    "[4] Buy All Tokens", "[0] Back"
                };
            }
            TerminalUI.tSubDashboard("TOKEN PURCHASE", tokenOptions);
            int choice = FastInput.readInt();

            if (choice == 0) {
                return;
            }

            MealType selected = null;
            if (isRamadan) {
                selected = switch (choice) {
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
                selected = switch (choice) {
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

            if (selected != null) {
                String result = controller.processTokenPurchaseForDay(username, day, selected);
                TerminalUI.tEmpty();
                TerminalUI.tSuccess(result);
                TerminalUI.tPause();
            } else if (choice == 4) {
                String[] results = autoBuyAllMeals(day, username);
                ConsoleUtil.clearScreen();
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                TerminalUI.at(2, 1);
                TerminalUI.tInfoBox("ALL TOKENS PURCHASED", results);
                TerminalUI.tPause();
            } else {
                TerminalUI.tError("Invalid selection.");
                TerminalUI.tPause();
            }
        }
    }

    private void printWrappedMenu(String mealName, String menuItems) {
        String prefix = String.format("%-11s ", mealName + ":");
        int maxLineLength = 67 - prefix.length();

        if (menuItems == null || menuItems.isEmpty() || menuItems.equals("---")) {
            TerminalUI.tBoxLine(prefix + "(Not set)");
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
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tInfoBox("BULK PURCHASE MODE",
                "This will try to buy every meal token for all remaining days of this week.",
                "Type y to confirm or n to cancel.");
        TerminalUI.tPrompt("Confirm buying all meals for all remaining days? (y/n): ");
        if (!FastInput.readLine().trim().equalsIgnoreCase("y")) {
            return;
        }

        java.util.List<String> results = new java.util.ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            if (!day.isBefore(today)) {
                results.add(day.getDayOfWeek() + " (" + day + ")");
                for (String line : autoBuyAllMeals(day, username)) {
                    results.add("  " + line);
                }
            }
        }

        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tInfoBox("BULK PURCHASE COMPLETE", results.toArray(new String[0]));
        TerminalUI.tPause();
    }

    private String[] autoBuyAllMeals(LocalDate day, String username) {
        MealType[] meals = getAvailableMeals();
        String[] results = new String[meals.length];
        for (int i = 0; i < meals.length; i++) {
            MealType mt = meals[i];
            String res = controller.processTokenPurchaseForDay(username, day, mt);
            results[i] = mt + ": " + res;
        }
        return results;
    }

    private MealType[] getAvailableMeals() {
        if (TimeManager.isRamadanMode()) {
            return new MealType[]{MealType.SUHOOR, MealType.IFTAR, MealType.DINNER};
        }
        return new MealType[]{MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER};
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
