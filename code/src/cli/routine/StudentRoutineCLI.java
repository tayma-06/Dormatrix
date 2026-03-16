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

    private static final String[] DAY_NAMES = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };
    private static final String[] SLOT_NAMES = {
            "00:00-02:00", "02:00-04:00", "04:00-06:00", "06:00-08:00",
            "08:00-10:00", "10:00-12:00", "12:00-14:00", "14:00-16:00",
            "16:00-18:00", "18:00-20:00", "20:00-22:00", "22:00-24:00"
    };

    private static final String HINT =
            "  [←↑↓→] Navigate    [Enter] View/Edit/Clear    [ESC/Q] Back";

    // ─────────────────────────────────────────────────────────────
    //  Entry point
    // ─────────────────────────────────────────────────────────────

    public void show(String username) {
        String studentId = resolveStudentId(username);

        while (true) {
            try {
                boolean exit = navigateTable(studentId);
                if (exit) return;
            } catch (Exception e) {
                cleanup();
                System.err.println("[StudentRoutineCLI] " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Draw table with current highlight at (hlRow, hlCol)
    // ─────────────────────────────────────────────────────────────

    private void drawTable(String studentId, int hlRow, int hlCol) {
        clearAndRefresh();
        System.out.print(HIDE_CUR);
        TerminalUI.at(3, 1);
        // Use the new highlight-aware render
        System.out.print(controller.renderStudentRoutineWithHighlight(studentId, hlRow, hlCol));

        // Draw hint bar below table
        System.out.println();
        System.out.print(ConsoleColors.fgRGB(160, 150, 60) + HINT + RESET);
        System.out.flush();
    }

    // ─────────────────────────────────────────────────────────────
    //  Interactive navigator
    // ─────────────────────────────────────────────────────────────

    private boolean navigateTable(String studentId) throws Exception {
        int selRow = 0, selCol = 0;

        drawTable(studentId, selRow, selCol);

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
                            selRow = newRow;
                            selCol = newCol;
                            // Redraw whole table with new highlight
                            term.setAttributes(saved);
                            drawTable(studentId, selRow, selCol);
                            saved = term.enterRawMode();
                        }
                    } else {
                        return true; // bare ESC → exit
                    }
                    continue;
                }

                if (c == 13 || c == 10) {       // Enter → cell action
                    term.setAttributes(saved);
                    System.out.print(SHOW_CUR);

                    handleCellAction(studentId, selRow, selCol);

                    // Redraw after possible change
                    drawTable(studentId, selRow, selCol);
                    saved = term.enterRawMode();
                    continue;
                }

                if (c == 3 || c == 'q' || c == 'Q') return true; // Ctrl+C or Q
            }
        } finally {
            term.setAttributes(saved);
            System.out.print(SHOW_CUR);
            System.out.flush();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Cell action popup
    // ─────────────────────────────────────────────────────────────

    private void handleCellAction(String studentId, int row, int col)
            throws InterruptedException {
        String dayName  = DAY_NAMES[col];
        String slotName = SLOT_NAMES[row];
        String content  = controller.getSlotContent(studentId, col + 1, row + 1);

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

        String[] options = {"Edit this slot", "Clear this slot", "Cancel"};
        int idx;
        try { idx = tArrowSelect("SLOT OPTIONS", options); }
        catch (InterruptedException e) { return; }
        if (idx < 0 || idx == 2) return;

        if (idx == 0) {                         // EDIT
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
            if (newContent == null || newContent.trim().isEmpty()) return;

            controller.setSlot(studentId, col + 1, row + 1, newContent.trim());
            clearAndRefresh();
            tBoxTop();
            tBoxLine("Slot updated: " + dayName + " " + slotName);
            tBoxBottom();
            tPause();

        } else {                                // CLEAR
            if (content == null || content.trim().isEmpty()) {
                clearAndRefresh();
                tBoxTop();
                tBoxLine("This slot is already empty.");
                tBoxBottom();
                tPause();
                return;
            }

            clearAndRefresh();
            String[] confirm = {"Yes, clear it", "Cancel"};
            int cidx;
            try { cidx = tArrowSelect("CONFIRM CLEAR", confirm); }
            catch (InterruptedException e) { return; }

            if (cidx == 0) {
                controller.clearSlot(studentId, col + 1, row + 1);
                clearAndRefresh();
                tBoxTop();
                tBoxLine("Slot cleared: " + dayName + " " + slotName);
                tBoxBottom();
                tPause();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Fallback menu (no JLine)
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
        int col = pickFallback("SELECT DAY",  DAY_NAMES);   if (col < 0) return;
        int row = pickFallback("SELECT SLOT", SLOT_NAMES);  if (row < 0) return;
        clearAndRefresh();
        tBoxTop(); tBoxTitle("EDIT SLOT"); tBoxSep();
        tBoxLine("Day  : " + DAY_NAMES[col]);
        tBoxLine("Slot : " + SLOT_NAMES[row]); tBoxSep();
        tBoxLine("  [ESC] Cancel", ConsoleColors.fgRGB(160, 150, 60)); tBoxSep();
        tCustomInputRow("Content : ");
        String content = readLineOrEsc();
        if (content == null || content.trim().isEmpty()) return;
        controller.setSlot(studentId, col + 1, row + 1, content.trim());
        tBoxTop(); tBoxLine("Slot updated."); tBoxBottom(); tPause();
    }

    private void handleClearFallback(String studentId) throws InterruptedException {
        int col = pickFallback("SELECT DAY",  DAY_NAMES);  if (col < 0) return;
        int row = pickFallback("SELECT SLOT", SLOT_NAMES); if (row < 0) return;
        clearAndRefresh();
        String[] confirm = {"Yes, clear it", "Cancel"};
        int cidx;
        try { cidx = tArrowSelect("CONFIRM CLEAR", confirm); }
        catch (InterruptedException e) { return; }
        if (cidx == 0) {
            controller.clearSlot(studentId, col + 1, row + 1);
            tBoxTop(); tBoxLine("Cleared."); tBoxBottom(); tPause();
        }
    }

    private void handleViewFallback(String studentId) throws InterruptedException {
        int col = pickFallback("SELECT DAY",  DAY_NAMES);  if (col < 0) return;
        int row = pickFallback("SELECT SLOT", SLOT_NAMES); if (row < 0) return;
        String content = controller.getSlotContent(studentId, col + 1, row + 1);
        clearAndRefresh();
        tBoxTop(); tBoxTitle("SLOT CONTENT"); tBoxSep();
        tBoxLine("Day  : " + DAY_NAMES[col]);
        tBoxLine("Slot : " + SLOT_NAMES[row]); tBoxSep();
        tBoxLine(content == null || content.trim().isEmpty()
                ? "(empty)" : "Content : " + content);
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
