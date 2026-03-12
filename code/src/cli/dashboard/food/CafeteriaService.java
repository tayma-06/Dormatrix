package cli.dashboard.food;

import controllers.food.MenuManagementController;
import java.time.LocalDate;
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
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);

        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);

            List<DailyMenu> currentMenu = menuController.getWeeklyMenuData();
            renderWeeklyBoxes(currentMenu);
            renderWeeklyCalendar(today, startOfWeek);

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

    private void renderWeeklyBoxes(List<DailyMenu> currentMenu) {
        boolean isRamadan = TimeManager.isRamadanMode();
        String box = TerminalUI.getActiveBoxColor();
        String txt = TerminalUI.getActiveTextColor();
        String dim = utils.ConsoleColors.Accent.MUTED;
        String BG = TerminalUI.getActiveBgColor();
        String RESET = utils.ConsoleColors.RESET;
        String BOLD = utils.ConsoleColors.BOLD;

        int dw = 9, mw = 26;
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
        tRow(col, BG + box + "║" + RESET + BG + txt + BOLD + padC("WEEKLY MENU PREVIEW", fullInner) + RESET + BG + box + "║" + RESET);
        tRow(col, BG + box + bannerToGrid + RESET);
        tRow(col, BG + box + "║" + RESET + BG + txt + BOLD + padC("DAY", dw) + RESET
                + BG + box + "║" + RESET + BG + txt + BOLD + padC(m1h, mw) + RESET
                + BG + box + "║" + RESET + BG + txt + BOLD + padC(m2h, mw) + RESET
                + BG + box + "║" + RESET + BG + txt + BOLD + padC("DINNER", mw) + RESET
                + BG + box + "║" + RESET);
        tRow(col, BG + box + hLine + RESET);

        for (int i = 0; i < days.length; i++) {
            String day = days[i];
            String shortDay = "[" + (i + 1) + "] " + day.substring(0, 3);
            String[] c1 = wrapCells(extractMenuText(currentMenu, day, mt1), mw);
            String[] c2 = wrapCells(extractMenuText(currentMenu, day, mt2), mw);
            String[] c3 = wrapCells(extractMenuText(currentMenu, day, mt3), mw);
            int rows = Math.max(1, Math.max(c1.length, Math.max(c2.length, c3.length)));
            for (int r = 0; r < rows; r++) {
                String dc = r == 0 ? padR(shortDay, dw) : " ".repeat(dw);
                String a = r < c1.length ? c1[r] : " ".repeat(mw);
                String b = r < c2.length ? c2[r] : " ".repeat(mw);
                String c = r < c3.length ? c3[r] : " ".repeat(mw);
                tRow(col, BG + box + "║" + RESET + BG + txt + dc + RESET
                        + BG + box + "║" + RESET + BG + dim + a + RESET
                        + BG + box + "║" + RESET + BG + dim + b + RESET
                        + BG + box + "║" + RESET + BG + dim + c + RESET
                        + BG + box + "║" + RESET);
            }
            if (i < days.length - 1) {
                tRow(col, BG + box + hLine + RESET);
            }
        }
        tRow(col, BG + box + botLine + RESET);
    }

    private void tRow(int col, String content) {
        System.out.print("\u001B[" + col + "G" + content + "\n");
        System.out.flush();
    }

    private String pad(String s, int w) {
        if (s.length() >= w) {
            return s.substring(0, w);
        }
        return s + " ".repeat(w - s.length());
    }

    private String padR(String s, int w) {
        return pad(s, w);
    }

    private String padC(String s, int w) {
        int total = w - s.length();
        int left = total / 2, right = total - left;
        return " ".repeat(Math.max(0, left)) + s + " ".repeat(Math.max(0, right));
    }

    private String[] wrapCells(String text, int w) {
        if (text == null || text.isEmpty() || text.equals("---")) {
            return new String[]{padC("(not set)", w)};
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
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
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
