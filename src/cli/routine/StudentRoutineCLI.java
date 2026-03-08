package cli.routine;

import cli.Input;
import controllers.routine.RoutineController;

import java.time.DayOfWeek;
import java.util.Scanner;

public class StudentRoutineCLI {

    private final RoutineController controller = new RoutineController();
    private final Scanner sc = Input.SC;

    public void show(String dashboardToken) {
        while (true) {
            System.out.println();
            System.out.println(controller.renderStudentRoutine(dashboardToken));
            System.out.println("1. Add/Update slot");
            System.out.println("2. Clear slot");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");

            int ch = readInt();
            if (ch == 0) return;

            if (ch == 1) {
                DayOfWeek day = readDay();
                int slot = readFullSlot();
                String content = readNonEmpty("Write schedule content: ");
                boolean ok = controller.putSlot(dashboardToken, day, slot, content);
                System.out.println(ok ? "Routine updated." : "Could not update routine.");
            } else if (ch == 2) {
                DayOfWeek day = readDay();
                int slot = readFullSlot();
                boolean ok = controller.clearSlot(dashboardToken, day, slot);
                System.out.println(ok ? "Slot cleared." : "Could not clear slot.");
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private int readInt() {
        while (true) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;
            try {
                return Integer.parseInt(line);
            } catch (Exception e) {
                System.out.print("Invalid number. Enter again: ");
            }
        }
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (!line.isEmpty()) return line;
            System.out.println("Input can not be empty.");
        }
    }

    private DayOfWeek readDay() {
        while (true) {
            System.out.println("Choose day:");
            System.out.println("[1] Monday  [2] Tuesday  [3] Wednesday  [4] Thursday");
            System.out.println("[5] Friday  [6] Saturday [7] Sunday");
            System.out.print("Day: ");
            int x = readInt();

            switch (x) {
                case 1: return DayOfWeek.MONDAY;
                case 2: return DayOfWeek.TUESDAY;
                case 3: return DayOfWeek.WEDNESDAY;
                case 4: return DayOfWeek.THURSDAY;
                case 5: return DayOfWeek.FRIDAY;
                case 6: return DayOfWeek.SATURDAY;
                case 7: return DayOfWeek.SUNDAY;
                default: System.out.println("Invalid day.");
            }
        }
    }

    private int readFullSlot() {
        while (true) {
            System.out.println("Choose 24-hour slot:");
            for (int i = 0; i < RoutineController.FULL_SLOT_LABELS.length; i++) {
                System.out.println("[" + (i + 1) + "] " + RoutineController.FULL_SLOT_LABELS[i]);
            }
            System.out.print("Slot: ");
            int x = readInt();
            if (x >= 1 && x <= RoutineController.FULL_SLOT_LABELS.length) {
                return x - 1;
            }
            System.out.println("Invalid slot.");
        }
    }
}