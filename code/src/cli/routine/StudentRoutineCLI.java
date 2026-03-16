package cli.routine;

import controllers.routine.RoutineController;
import utils.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static utils.TerminalUI.*;
import static utils.TerminalUIExtras.*;

public class StudentRoutineCLI {

    private final RoutineController controller = new RoutineController();
    private static final String STUDENT_FILE = "data/users/students.txt";

    private static final MenuItem[] MENU = {
            new MenuItem(1, "Edit Slot"),
            new MenuItem(2, "Clear Slot"),
            new MenuItem(3, "View Exact Slot Text"),
            new MenuItem(0, "Back"),
    };

    private static final String[] DAY_OPTIONS = {
            "Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday", "Sunday"
    };

    private static final String[] SLOT_OPTIONS = {
            "00:00 - 02:00", "02:00 - 04:00", "04:00 - 06:00",
            "06:00 - 08:00", "08:00 - 10:00", "10:00 - 12:00",
            "12:00 - 14:00", "14:00 - 16:00", "16:00 - 18:00",
            "18:00 - 20:00", "20:00 - 22:00", "22:00 - 24:00"
    };

    public void show(String username) {
        String studentId = resolveStudentId(username);

        while (true) {
            try {
                // ── Screen 1: show the routine table ─────────────────
                clearAndRefresh();
                System.out.print(HIDE_CUR);
                TerminalUI.at(3, 1);
                controller.printStudentRoutine(studentId);
                System.out.print(SHOW_CUR);
                tEmpty();
                tPrompt("Press Enter to open menu...");
                FastInput.readLine();

                // ── Screen 2: show the action menu ────────────────────
                clearAndRefresh();

                drawDashboard(
                        "WEEKLY ROUTINE",
                        "Student: " + studentId,
                        MENU,
                        TerminalUI.getActiveTextColor(),
                        TerminalUI.getActiveBoxColor(),
                        null,
                        3
                );

                int choice = readChoiceArrow();

                if (choice == 0) return;

                switch (choice) {
                    case 1 -> handleEdit(studentId);
                    case 2 -> handleClear(studentId);
                    case 3 -> handleViewOne(studentId);
                    default -> { tError("Invalid choice."); tPause(); }
                }

            } catch (Exception e) {
                cleanup();
                System.err.println("[StudentRoutineCLI] " + e.getMessage());
            }
        }
    }

    // ── helpers ───────────────────────────────────────────────────

    private int pickDay(String title) throws InterruptedException {
        clearAndRefresh();
        int idx = tArrowSelect(title, DAY_OPTIONS);
        return idx < 0 ? 0 : idx + 1;  // 1-7 or 0 for cancel
    }

    private int pickSlot(String title) throws InterruptedException {
        clearAndRefresh();
        int idx = tArrowSelect(title, SLOT_OPTIONS);
        return idx < 0 ? 0 : idx + 1;  // 1-12 or 0 for cancel
    }

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

    // ── action handlers ──────────────────────────────────────────

    private void handleEdit(String studentId) {
        try {
            int day = pickDay("SELECT DAY TO EDIT");
            if (day == 0) return;

            int slot = pickSlot("SELECT TIME SLOT");
            if (slot == 0) return;

            clearAndRefresh();
            tBoxTop();
            tBoxTitle("ENTER ROUTINE TEXT");
            tBoxSep();
            tBoxLine("Day  : " + dayName(day));
            tBoxLine("Slot : " + slotLabel(slot));
            tBoxSep();
            tBoxLine("  [ESC] Cancel and go back", ConsoleColors.fgRGB(160, 150, 60));
            tBoxSep();
            tCustomInputRow("Content : ");
            String content = readLineOrEsc();
            if (content == null || content.trim().isEmpty()) return;

            controller.setSlot(studentId, day, slot, content.trim());
            tBoxTop();
            tBoxLine("Routine updated successfully.");
            tBoxBottom();
            tPause();
        } catch (Exception e) {
            tError(e.getMessage());
            tPause();
        }
    }

    private void handleClear(String studentId) {
        try {
            int day = pickDay("SELECT DAY TO CLEAR");
            if (day == 0) return;

            int slot = pickSlot("SELECT TIME SLOT TO CLEAR");
            if (slot == 0) return;

            clearAndRefresh();
            tBoxTop();
            tBoxTitle("CONFIRM CLEAR");
            tBoxSep();
            tBoxLine("Day  : " + dayName(day));
            tBoxLine("Slot : " + slotLabel(slot));
            tBoxSep();

            String[] confirmOptions = {"Yes, clear it", "Cancel"};
            int cidx = tArrowSelect("CONFIRM", confirmOptions);

            if (cidx == 0) {
                controller.clearSlot(studentId, day, slot);
                clearAndRefresh();
                tBoxTop();
                tBoxLine("Slot cleared successfully.");
                tBoxBottom();
            } else {
                tBoxTop();
                tBoxLine("Cancelled.");
                tBoxBottom();
            }
            tPause();
        } catch (Exception e) {
            tError(e.getMessage());
            tPause();
        }
    }

    private void handleViewOne(String studentId) {
        try {
            int day = pickDay("SELECT DAY TO VIEW");
            if (day == 0) return;

            int slot = pickSlot("SELECT TIME SLOT TO VIEW");
            if (slot == 0) return;

            String content = controller.getSlotContent(studentId, day, slot);
            clearAndRefresh();
            tBoxTop();
            tBoxTitle("SLOT CONTENT");
            tBoxSep();
            tBoxLine("Day  : " + dayName(day));
            tBoxLine("Slot : " + slotLabel(slot));
            tBoxSep();
            tBoxLine(content == null || content.trim().isEmpty()
                    ? "(No routine saved for this slot)"
                    : "Content : " + content);
            tBoxBottom();
            tPause();
        } catch (Exception e) {
            tError(e.getMessage());
            tPause();
        }
    }

    // ── label helpers ─────────────────────────────────────────────

    private static final String[] DAY_NAMES = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };
    private static final String[] SLOT_NAMES = {
            "00:00 - 02:00", "02:00 - 04:00", "04:00 - 06:00", "06:00 - 08:00",
            "08:00 - 10:00", "10:00 - 12:00", "12:00 - 14:00",
            "14:00 - 16:00", "16:00 - 18:00", "18:00 - 20:00",
            "20:00 - 22:00", "22:00 - 00:00"
    };

    private String dayName(int d) {
        return d >= 1 && d <= 7 ? DAY_NAMES[d - 1] : "?";
    }

    private String slotLabel(int s) {
        return s >= 1 && s <= 12 ? SLOT_NAMES[s - 1] : "?";
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
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}
