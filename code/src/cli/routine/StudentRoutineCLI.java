package cli.routine;

import controllers.routine.RoutineController;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;
import utils.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static utils.TerminalUI.*;
import static utils.TerminalUIExtras.*;

public class StudentRoutineCLI {

    private final RoutineController controller = new RoutineController();
    private static final String STUDENT_FILE = "data/users/students.txt";

    // ── Table layout constants (must match RoutineController) ─────
    // Table printed starting at row 3:
    //   row 3              = title line
    //   row 4              = top border  ┌──...──┐
    //   row 5              = header      │ TIME │ MON │...
    //   row 6              = divider     ├──...──┤
    //   row 7 + r*2        = slot r content row  (r = 0..11)
    //   row 8 + r*2        = slot r divider row
    //   row 30             = bottom border └──...──┘
    //   row 31             = note line
    private static final int SLOT_0_ROW      = 7;   // terminal row of first slot content
    private static final int CELL_W          = 12;  // matches RoutineController.CELL_WIDTH
    private static final int CELL_CONTENT_W  = 10;  // CELL_W - 2
    private static final int TIME_COL_W      = 10;  // matches RoutineController.TIME_COL_WIDTH
    // terminal col where day c content starts:
    //   1(border) + 10(TIME) + 1(border) = col 12 for border, col 13 for MON content
    //   each subsequent day: + 13 (1 border + 12 cell)
    private static final int DAY_0_COL       = 13;  // MON content start col (1-based)
    private static final int DAY_COL_STRIDE  = 13;

    private static final int HINT_ROW        = 33;

    private static final String[] DAY_NAMES = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };
    private static final String[] SLOT_NAMES = {
            "00:00-02:00", "02:00-04:00", "04:00-06:00", "06:00-08:00",
            "08:00-10:00", "10:00-12:00", "12:00-14:00", "14:00-16:00",
            "16:00-18:00", "18:00-20:00", "20:00-22:00", "22:00-24:00"
    };

    // ─────────────────────────────────────────────────────────────
    //  Entry point
    // ─────────────────────────────────────────────────────────────

    public void show(String username) {
        String studentId = resolveStudentId(username);

        while (true) {
            try {
                clearAndRefresh();
                System.out.print(HIDE_CUR);

                TerminalUI.at(3, 1);
                controller.printStudentRoutine(studentId);
                drawHint();

                String[][] cells = loadCells(studentId);
                boolean exit = navigateTable(studentId, cells);
                if (exit) return;

            } catch (Exception e) {
                cleanup();
                System.err.println("[StudentRoutineCLI] " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Interactive navigator
    // ─────────────────────────────────────────────────────────────

    private boolean navigateTable(String studentId, String[][] cells) throws Exception {
        int selRow = 0, selCol = 0;

        highlightCell(selRow, selCol, cells[selRow][selCol], true);
        System.out.flush();

        Terminal term = TerminalUI.getJLineTerminal();
        if (term == null) {
            System.out.print(SHOW_CUR);
            return showFallbackMenu(studentId);
        }

        Attributes saved = term.enterRawMode();
        NonBlockingReader reader = term.reader();

        try {
            while (true) {
                int c = reader.read();
                if (c == -1) continue;

                if (c == 27) {
                    int n1 = reader.read(100);
                    if (n1 == '[' || n1 == 'O') {
                        int n2 = reader.read(100);
                        int newRow = selRow, newCol = selCol;
                        switch (n2) {
                            case 'A': newRow = (selRow - 1 + 12) % 12; break; // UP
                            case 'B': newRow = (selRow + 1)      % 12; break; // DOWN
                            case 'C': newCol = (selCol + 1)      %  7; break; // RIGHT
                            case 'D': newCol = (selCol - 1 +  7) %  7; break; // LEFT
                        }
                        if (newRow != selRow || newCol != selCol) {
                            highlightCell(selRow, selCol, cells[selRow][selCol], false);
                            selRow = newRow;
                            selCol = newCol;
                            highlightCell(selRow, selCol, cells[selRow][selCol], true);
                            System.out.flush();
                        }
                    } else {
                        return true;  // bare ESC → exit
                    }
                    continue;
                }

                if (c == 13 || c == 10) {   // Enter → cell action
                    term.setAttributes(saved);
                    System.out.print(SHOW_CUR);

                    boolean changed = handleCellAction(studentId, selRow, selCol, cells);

                    // Redraw table
                    clearAndRefresh();
                    System.out.print(HIDE_CUR);
                    TerminalUI.at(3, 1);
                    controller.printStudentRoutine(studentId);
                    drawHint();

                    if (changed) cells = loadCells(studentId);

                    saved = term.enterRawMode();
                    highlightCell(selRow, selCol, cells[selRow][selCol], true);
                    System.out.flush();
                    continue;
                }

                if (c == 3 || c == 'q' || c == 'Q') return true;  // Ctrl+C or Q
            }
        } finally {
            term.setAttributes(saved);
            System.out.print(SHOW_CUR);
            System.out.flush();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Highlight a cell on the rendered table
    // ─────────────────────────────────────────────────────────────

    private void highlightCell(int row, int col, String content, boolean on) {
        int termRow = SLOT_0_ROW + row * 2;
        int termCol = DAY_0_COL + col * DAY_COL_STRIDE;

        String bg   = on ? ConsoleColors.bgRGB(160, 130, 0)   : getActiveBgColor();
        String fg   = on ? ConsoleColors.fgRGB(255, 255, 120) : getActiveTextColor();
        String bold = on ? BOLD : "";

        String cell = (content == null) ? "" : content;
        if (cell.length() > CELL_CONTENT_W) {
            cell = cell.substring(0, CELL_CONTENT_W - 1) + "…";
        }
        // pad to exactly CELL_W
        String padded = cell + " ".repeat(Math.max(0, CELL_W - cell.length()));

        TerminalUI.at(termRow, termCol);
        System.out.print(bg + fg + bold + padded + RESET);
    }

    // ─────────────────────────────────────────────────────────────
    //  Cell action popup
    // ─────────────────────────────────────────────────────────────

    private boolean handleCellAction(String studentId, int row, int col,
                                     String[][] cells) throws InterruptedException {
        String dayName  = DAY_NAMES[col];
        String slotName = SLOT_NAMES[row];
        String content  = cells[row][col];

        // ── Show full content ─────────────────────────────────────
        clearAndRefresh();
        tBoxTop();
        tBoxTitle(dayName + "  |  " + slotName);
        tBoxSep();

        if (content == null || content.trim().isEmpty()) {
            tBoxLine("Content : (empty)");
        } else {
            int wrap = 55;
            String full = content;
            if (full.length() <= wrap) {
                tBoxLine("Content : " + full);
            } else {
                tBoxLine("Content : " + full.substring(0, wrap));
                for (int i = wrap; i < full.length(); i += wrap) {
                    tBoxLine("          " + full.substring(i, Math.min(i + wrap, full.length())));
                }
            }
        }
        tBoxBottom();

        // ── Action picker ─────────────────────────────────────────
        String[] options = {"Edit this slot", "Clear this slot", "Cancel"};
        int idx;
        try { idx = tArrowSelect("SLOT OPTIONS", options); }
        catch (InterruptedException e) { return false; }
        if (idx < 0 || idx == 2) return false;

        if (idx == 0) {                                 // EDIT
            clearAndRefresh();
            tBoxTop();
            tBoxTitle("EDIT SLOT");
            tBoxSep();
            tBoxLine("Day  : " + dayName);
            tBoxLine("Slot : " + slotName);
            if (content != null && !content.trim().isEmpty()) {
                tBoxLine("Old  : " + content);
            }
            tBoxSep();
            tBoxLine("  [ESC] Cancel and go back", ConsoleColors.fgRGB(160, 150, 60));
            tBoxSep();
            tCustomInputRow("Content : ");
            String newContent = readLineOrEsc();
            if (newContent == null || newContent.trim().isEmpty()) return false;

            controller.setSlot(studentId, col + 1, row + 1, newContent.trim());
            clearAndRefresh();
            tBoxTop();
            tBoxLine("Slot updated: " + dayName + " " + slotName);
            tBoxBottom();
            tPause();
            return true;

        } else {                                        // CLEAR
            if (content == null || content.trim().isEmpty()) {
                clearAndRefresh();
                tBoxTop();
                tBoxLine("This slot is already empty.");
                tBoxBottom();
                tPause();
                return false;
            }

            clearAndRefresh();
            String[] confirm = {"Yes, clear it", "Cancel"};
            int cidx;
            try { cidx = tArrowSelect("CONFIRM CLEAR", confirm); }
            catch (InterruptedException e) { return false; }

            if (cidx == 0) {
                controller.clearSlot(studentId, col + 1, row + 1);
                clearAndRefresh();
                tBoxTop();
                tBoxLine("Slot cleared: " + dayName + " " + slotName);
                tBoxBottom();
                tPause();
                return true;
            }
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Load all cell contents into 12×7 array
    // ─────────────────────────────────────────────────────────────

    private String[][] loadCells(String studentId) {
        String[][] cells = new String[12][7];
        for (int r = 0; r < 12; r++) {
            for (int c = 0; c < 7; c++) {
                String content = controller.getSlotContent(studentId, c + 1, r + 1);
                cells[r][c] = (content == null) ? "" : content;
            }
        }
        return cells;
    }

    // ─────────────────────────────────────────────────────────────
    //  Navigation hint bar below the table
    // ─────────────────────────────────────────────────────────────

    private void drawHint() {
        TerminalUI.at(HINT_ROW, TerminalUI.boxCol());
        System.out.print(
                ConsoleColors.fgRGB(160, 150, 60)
                        + "  [←↑↓→] Navigate    [Enter] View / Edit / Clear    [ESC] Back    [Q] Back"
                        + RESET
        );
        System.out.flush();
    }

    // ─────────────────────────────────────────────────────────────
    //  Fallback menu if JLine terminal unavailable
    // ─────────────────────────────────────────────────────────────

    private boolean showFallbackMenu(String studentId) throws Exception {
        MenuItem[] MENU = {
                new MenuItem(1, "Edit Slot"),
                new MenuItem(2, "Clear Slot"),
                new MenuItem(3, "View Slot Text"),
                new MenuItem(0, "Back"),
        };
        clearAndRefresh();
        drawDashboard("WEEKLY ROUTINE", "Student: " + studentId,
                MENU, getActiveTextColor(), getActiveBoxColor(), null, 3);
        int choice = readChoiceArrow();
        if (choice == 0) return true;

        switch (choice) {
            case 1 -> handleEditFallback(studentId);
            case 2 -> handleClearFallback(studentId);
            case 3 -> handleViewFallback(studentId);
        }
        return false;
    }

    private void handleEditFallback(String studentId) throws InterruptedException {
        int day  = pickFallback("SELECT DAY",  DAY_NAMES);  if (day  < 0) return;
        int slot = pickFallback("SELECT SLOT", SLOT_NAMES); if (slot < 0) return;
        clearAndRefresh();
        tBoxTop(); tBoxTitle("EDIT SLOT"); tBoxSep();
        tBoxLine("Day  : " + DAY_NAMES[day]);
        tBoxLine("Slot : " + SLOT_NAMES[slot]); tBoxSep();
        tBoxLine("  [ESC] Cancel", ConsoleColors.fgRGB(160, 150, 60)); tBoxSep();
        tCustomInputRow("Content : ");
        String content = readLineOrEsc();
        if (content == null || content.trim().isEmpty()) return;
        controller.setSlot(studentId, day + 1, slot + 1, content.trim());
        tBoxTop(); tBoxLine("Slot updated."); tBoxBottom(); tPause();
    }

    private void handleClearFallback(String studentId) throws InterruptedException {
        int day  = pickFallback("SELECT DAY",  DAY_NAMES);  if (day  < 0) return;
        int slot = pickFallback("SELECT SLOT", SLOT_NAMES); if (slot < 0) return;
        clearAndRefresh();
        String[] confirm = {"Yes, clear it", "Cancel"};
        int cidx;
        try { cidx = tArrowSelect("CONFIRM CLEAR", confirm); }
        catch (InterruptedException e) { return; }
        if (cidx == 0) { controller.clearSlot(studentId, day + 1, slot + 1); tBoxTop(); tBoxLine("Cleared."); tBoxBottom(); tPause(); }
    }

    private void handleViewFallback(String studentId) throws InterruptedException {
        int day  = pickFallback("SELECT DAY",  DAY_NAMES);  if (day  < 0) return;
        int slot = pickFallback("SELECT SLOT", SLOT_NAMES); if (slot < 0) return;
        String content = controller.getSlotContent(studentId, day + 1, slot + 1);
        clearAndRefresh();
        tBoxTop(); tBoxTitle("SLOT CONTENT"); tBoxSep();
        tBoxLine("Day  : " + DAY_NAMES[day]);
        tBoxLine("Slot : " + SLOT_NAMES[slot]); tBoxSep();
        tBoxLine(content == null || content.trim().isEmpty() ? "(empty)" : "Content : " + content);
        tBoxBottom(); tPause();
    }

    private int pickFallback(String title, String[] options) throws InterruptedException {
        clearAndRefresh();
        String[] numbered = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            numbered[i] = String.format("%-5s%s", "[" + (i + 1) + "]", options[i]);
        }
        return tArrowSelect(title, numbered);
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────

    private void clearAndRefresh() {
        ConsoleUtil.clearScreen();
        BackgroundFiller.applyStudentTheme();
        TerminalUI.setActiveTheme(
                ConsoleColors.fgRGB(60, 140, 255),
                ConsoleColors.ThemeText.STUDENT_TEXT,
                ConsoleColors.bgRGB(0, 6, 45)
        );
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
    }

    private String resolveStudentId(String target) {
        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length < 2) continue;
                String id   = parts[0].trim().replace("\uFEFF", "");
                String name = parts[1].trim();
                if (id.equals(target.trim()) || name.equalsIgnoreCase(target.trim())) return id;
            }
        } catch (IOException e) { return null; }
        return null;
    }
}




//package cli.routine;
//
//import controllers.routine.RoutineController;
//import utils.*;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//
//import static utils.TerminalUI.*;
//import static utils.TerminalUIExtras.*;
//
//public class StudentRoutineCLI {
//
//    private final RoutineController controller = new RoutineController();
//    private static final String STUDENT_FILE = "data/users/students.txt";
//
//    private static final MenuItem[] MENU = {
//            new MenuItem(1, "Edit Slot"),
//            new MenuItem(2, "Clear Slot"),
//            new MenuItem(3, "View Exact Slot Text"),
//            new MenuItem(0, "Back"),
//    };
//
//    private static final String[] DAY_OPTIONS = {
//            "[1] Monday", "[2] Tuesday", "[3] Wednesday", "[4] Thursday",
//            "[5] Friday", "[6] Saturday", "[7] Sunday"
//    };
//
//    private static final String[] SLOT_OPTIONS = {
//            "[1]  00:00 - 02:00", "[2]  02:00 - 04:00", "[3]  04:00 - 06:00",
//            "[4]  06:00 - 08:00", "[5]  08:00 - 10:00", "[6]  10:00 - 12:00",
//            "[7]  12:00 - 14:00", "[8]  14:00 - 16:00", "[9]  16:00 - 18:00",
//            "[10] 18:00 - 20:00", "[11] 20:00 - 22:00", "[12] 22:00 - 24:00"
//    };
//
//    public void show(String username) {
//        String studentId = resolveStudentId(username);
//
//        while (true) {
//            try {
//                // ── Screen 1: show the routine table ─────────────────
//                clearAndRefresh();
//                System.out.print(HIDE_CUR);
//                TerminalUI.at(3, 1);
//                controller.printStudentRoutine(studentId);
//                System.out.print(SHOW_CUR);
//                tEmpty();
//                tPrompt("Press Enter to open menu...");
//                FastInput.readLine();
//
//                // ── Screen 2: show the action menu ────────────────────
//                clearAndRefresh();
//
//                drawDashboard(
//                        "WEEKLY ROUTINE",
//                        "Student: " + studentId,
//                        MENU,
//                        TerminalUI.getActiveTextColor(),
//                        TerminalUI.getActiveBoxColor(),
//                        null,
//                        3
//                );
//
//                int choice = readChoiceArrow();
//
//                if (choice == 0) return;
//
//                switch (choice) {
//                    case 1 -> handleEdit(studentId);
//                    case 2 -> handleClear(studentId);
//                    case 3 -> handleViewOne(studentId);
//                    default -> { tError("Invalid choice."); tPause(); }
//                }
//
//            } catch (Exception e) {
//                cleanup();
//                System.err.println("[StudentRoutineCLI] " + e.getMessage());
//            }
//        }
//    }
//
//    // ── helpers ───────────────────────────────────────────────────
//
//    private int pickDay(String title) throws InterruptedException {
//        clearAndRefresh();
//        int idx = tArrowSelect(title, DAY_OPTIONS);
//        return idx < 0 ? 0 : idx + 1;  // 1-7 or 0 for cancel
//    }
//
//    private int pickSlot(String title) throws InterruptedException {
//        clearAndRefresh();
//        int idx = tArrowSelect(title, SLOT_OPTIONS);
//        return idx < 0 ? 0 : idx + 1;  // 1-12 or 0 for cancel
//    }
//
//    private void clearAndRefresh() {
//        ConsoleUtil.clearScreen();
//        BackgroundFiller.applyStudentTheme();
//        TerminalUI.setActiveTheme(
//                ConsoleColors.fgRGB(60, 140, 255),
//                ConsoleColors.ThemeText.STUDENT_TEXT,
//                ConsoleColors.bgRGB(0, 6, 45)
//        );
//        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
//        TerminalUI.at(2, 1);
//    }
//
//    // ── action handlers ──────────────────────────────────────────
//
//    private void handleEdit(String studentId) {
//        try {
//            int day = pickDay("SELECT DAY TO EDIT");
//            if (day == 0) return;
//
//            int slot = pickSlot("SELECT TIME SLOT");
//            if (slot == 0) return;
//
//            clearAndRefresh();
//            tBoxTop();
//            tBoxTitle("ENTER ROUTINE TEXT");
//            tBoxSep();
//            tBoxLine("Day  : " + dayName(day));
//            tBoxLine("Slot : " + slotLabel(slot));
//            tBoxSep();
//            tBoxLine("  [ESC] Cancel and go back", ConsoleColors.fgRGB(160, 150, 60));
//            tBoxSep();
//            tCustomInputRow("Content : ");
//            String content = readLineOrEsc();
//            if (content == null || content.trim().isEmpty()) return;
//
//            controller.setSlot(studentId, day, slot, content.trim());
//            tBoxTop();
//            tBoxLine("Routine updated successfully.");
//            tBoxBottom();
//            tPause();
//        } catch (Exception e) {
//            tError(e.getMessage());
//            tPause();
//        }
//    }
//
//    private void handleClear(String studentId) {
//        try {
//            int day = pickDay("SELECT DAY TO CLEAR");
//            if (day == 0) return;
//
//            int slot = pickSlot("SELECT TIME SLOT TO CLEAR");
//            if (slot == 0) return;
//
//            clearAndRefresh();
//            tBoxTop();
//            tBoxTitle("CONFIRM CLEAR");
//            tBoxSep();
//            tBoxLine("Day  : " + dayName(day));
//            tBoxLine("Slot : " + slotLabel(slot));
//            tBoxSep();
//
//            String[] confirmOptions = {"Yes, clear it", "Cancel"};
//            int cidx = tArrowSelect("CONFIRM", confirmOptions);
//
//            if (cidx == 0) {
//                controller.clearSlot(studentId, day, slot);
//                clearAndRefresh();
//                tBoxTop();
//                tBoxLine("Slot cleared successfully.");
//                tBoxBottom();
//            } else {
//                tBoxTop();
//                tBoxLine("Cancelled.");
//                tBoxBottom();
//            }
//            tPause();
//        } catch (Exception e) {
//            tError(e.getMessage());
//            tPause();
//        }
//    }
//
//    private void handleViewOne(String studentId) {
//        try {
//            int day = pickDay("SELECT DAY TO VIEW");
//            if (day == 0) return;
//
//            int slot = pickSlot("SELECT TIME SLOT TO VIEW");
//            if (slot == 0) return;
//
//            String content = controller.getSlotContent(studentId, day, slot);
//            clearAndRefresh();
//            tBoxTop();
//            tBoxTitle("SLOT CONTENT");
//            tBoxSep();
//            tBoxLine("Day  : " + dayName(day));
//            tBoxLine("Slot : " + slotLabel(slot));
//            tBoxSep();
//            tBoxLine(content == null || content.trim().isEmpty()
//                    ? "(No routine saved for this slot)"
//                    : "Content : " + content);
//            tBoxBottom();
//            tPause();
//        } catch (Exception e) {
//            tError(e.getMessage());
//            tPause();
//        }
//    }
//
//    // ── label helpers ─────────────────────────────────────────────
//
//    private static final String[] DAY_NAMES = {
//            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
//    };
//    private static final String[] SLOT_NAMES = {
//            "00:00 - 02:00", "02:00 - 04:00", "04:00 - 06:00", "06:00 - 08:00",
//            "08:00 - 10:00", "10:00 - 12:00", "12:00 - 14:00",
//            "14:00 - 16:00", "16:00 - 18:00", "18:00 - 20:00",
//            "20:00 - 22:00", "22:00 - 00:00"
//    };
//
//    private String dayName(int d) {
//        return d >= 1 && d <= 7 ? DAY_NAMES[d - 1] : "?";
//    }
//
//    private String slotLabel(int s) {
//        return s >= 1 && s <= 12 ? SLOT_NAMES[s - 1] : "?";
//    }
//
//    private String resolveStudentId(String target) {
//        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] parts = line.split("\\|", -1);
//                if (parts.length < 2) continue;
//                String id   = parts[0].trim().replace("\uFEFF", "");
//                String name = parts[1].trim();
//                if (id.equals(target.trim()) || name.equalsIgnoreCase(target.trim())) return id;
//            }
//        } catch (IOException e) {
//            return null;
//        }
//        return null;
//    }
//}
