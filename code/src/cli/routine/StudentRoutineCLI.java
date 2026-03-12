package cli.routine;

import controllers.routine.RoutineController;
import libraries.collections.MyOptional;
import models.users.StudentPublicInfo;
import utils.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static utils.TerminalUI.*;

public class StudentRoutineCLI {

    private final RoutineController controller = new RoutineController();
    private static final String STUDENT_FILE = "data/users/students.txt";

    private static final MenuItem[] MENU = {
        new MenuItem(1, "Edit Slot"),
        new MenuItem(2, "Clear Slot"),
        new MenuItem(3, "View Exact Slot Text"),
        new MenuItem(0, "Back"),};

    // Day and slot option arrays for tSubDashboard
    private static final String[] DAY_OPTIONS = {
        "[1] Monday", "[2] Tuesday", "[3] Wednesday", "[4] Thursday",
        "[5] Friday", "[6] Saturday", "[7] Sunday", "[0] Cancel"
    };
    private static final String[] SLOT_OPTIONS = {
        "[1] 00:00 - 02:00", "[2] 02:00 - 04:00", "[3] 04:00 - 06:00",
        "[4] 06:00 - 08:00", "[5] 08:00 - 10:00", "[6] 10:00 - 12:00",
            "[7] 12:00 - 14:00", "[8] 14:00 - 16:00", "[9] 16:00 - 18:00",
            "[10] 18:00 - 20:00", "[11] 20:00 - 22:00", "[12] 22:00 - 24:00", "[0] Cancel"
    };

    public void show(String username) {

        String studentId = resolveStudentId(username);

        while (true) {
            try {
                ConsoleUtil.clearScreen();
                BackgroundFiller.applyStudentTheme();
                System.out.print(HIDE_CUR);

                // Show weekly routine table below banner
                TerminalUI.at(3, 1);
                controller.printStudentRoutine(studentId);

                int menuStartRow = 17;
                int promptRow = drawDashboard(
                        "WEEKLY ROUTINE",
                        "Student: " + studentId,
                        MENU,
                        TerminalUI.getActiveTextColor(),
                        TerminalUI.getActiveBoxColor(),
                        null,
                        menuStartRow
                );

                System.out.print(SHOW_CUR);
                int choice = FastInput.readInt();

                if (choice == 0) {
                    return;
                }

                switch (choice) {
                    case 1 ->
                        handleEdit(studentId);
                    case 2 ->
                        handleClear(studentId);
                    case 3 ->
                        handleViewOne(studentId);
                    default -> {
                        tError("Invalid choice.");
                        tPause();
                    }
                }

            } catch (Exception e) {
                cleanup();
                System.err.println("[StudentRoutineCLI] " + e.getMessage());
            }
        }
    }

    // ── shared helpers ────────────────────────────────────────────
    /**
     * Show day picker, return 1-7 or 0 for cancel.
     */
    private int pickDay(String title) {
        clearAndRefresh();
        tSubDashboard(title, DAY_OPTIONS);
        int d = FastInput.readInt();
        return (d >= 0 && d <= 7) ? d : -1;
    }

    /**
     * Show slot picker, return 1-6 or 0 for cancel.
     */
    private int pickSlot(String title) {
        tEmpty();
        tSubDashboard(title, SLOT_OPTIONS);
        int s = FastInput.readInt();
        return (s >= 0 && s <= 12) ? s : -1;
    }

    /**
     * Clear + re-apply theme to get a fresh canvas.
     */
    private void clearAndRefresh() {
        ConsoleUtil.clearScreen();
        BackgroundFiller.applyStudentTheme();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
    }

    // ── action handlers ──────────────────────────────────────────
    private void handleEdit(String studentId) {
        try {
            int day = pickDay("SELECT DAY TO EDIT");
            if (day == 0) {
                return;
            }

            int slot = pickSlot("SELECT TIME SLOT");
            if (slot == 0) {
                return;
            }

            // Input box for routine text
            tEmpty();
            tBoxTop();
            tBoxTitle("ENTER ROUTINE TEXT");
            tBoxSep();
            tBoxLine("Day  : " + dayName(day));
            tBoxLine("Slot : " + slotLabel(slot));
            tBoxBottom();
            tPrompt("Content: ");
            String content = FastInput.readNonEmptyLine();

            controller.setSlot(studentId, day, slot, content);
            tEmpty();
            tSuccess("Routine updated successfully.");
            tPause();
        } catch (Exception e) {
            tError(e.getMessage());
            tPause();
        }
    }

    private void handleClear(String studentId) {
        try {
            int day = pickDay("SELECT DAY TO CLEAR");
            if (day == 0) {
                return;
            }

            int slot = pickSlot("SELECT TIME SLOT TO CLEAR");
            if (slot == 0) {
                return;
            }

            tEmpty();
            tBoxTop();
            tBoxTitle("CONFIRM CLEAR");
            tBoxSep();
            tBoxLine("Day  : " + dayName(day));
            tBoxLine("Slot : " + slotLabel(slot));
            tBoxBottom();
            tPrompt("Confirm? (y/n): ");
            String confirm = FastInput.readLine().trim();

            if (confirm.equalsIgnoreCase("y")) {
                controller.clearSlot(studentId, day, slot);
                tEmpty();
                tSuccess("Slot cleared successfully.");
            } else {
                tEmpty();
                tError("Cancelled.");
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
            if (day == 0) {
                return;
            }

            int slot = pickSlot("SELECT TIME SLOT TO VIEW");
            if (slot == 0) {
                return;
            }

            String content = controller.getSlotContent(studentId, day, slot);
            tEmpty();
            tBoxTop();
            tBoxTitle("SLOT CONTENT");
            tBoxSep();
            tBoxLine("Day  : " + dayName(day));
            tBoxLine("Slot : " + slotLabel(slot));
            tBoxSep();
            if (content == null || content.trim().isEmpty()) {
                tBoxLine("(No routine saved for this slot)");
            } else {
                tBoxLine("Content : " + content);
            }
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
        "14:00 - 16:00", "16:00 - 18:00", "18:00 - 20:00", "20:00 - 22:00", "22:00 - 00:00"
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
                if (parts.length < 2) {
                    continue;
                }

                String id = parts[0].trim().replace("\uFEFF", "");
                String name = parts[1].trim();

                boolean matchesId = id.equals(target.trim());
                boolean matchesName = name.equalsIgnoreCase(target.trim());

                if (matchesId || matchesName) {
                    return id;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}



