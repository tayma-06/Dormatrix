package cli.routine;

import controllers.routine.RoutineController;
import utils.BackgroundFiller;
import utils.ConsoleUtil;

import java.util.Scanner;

public class StudentRoutineCLI {
    private final RoutineController controller = new RoutineController();
    private final Scanner sc = new Scanner(System.in);

    public void show(String studentId) {
        while (true) {
            ConsoleUtil.clearScreen();
            BackgroundFiller.applyStudentTheme();

            System.out.println();
            System.out.println("╔════════════════════════════════ WEEKLY ROUTINE ═══════════════════════════════╗");
            controller.printStudentRoutine(studentId);
            System.out.println("[1] Edit Slot");
            System.out.println("[2] Clear Slot");
            System.out.println("[3] View Exact Slot Text");
            System.out.println("[0] Back");
            System.out.print("Enter choice: ");

            int choice = readInt();
            if (choice == 0) return;

            switch (choice) {
                case 1:
                    handleEdit(studentId);
                    break;
                case 2:
                    handleClear(studentId);
                    break;
                case 3:
                    handleViewOne(studentId);
                    break;
                default:
                    System.out.println("Invalid choice.");
                    pause();
            }
        }
    }

    private void handleEdit(String studentId) {
        try {
            System.out.println("\nChoose day:");
            controller.printDayChoices();
            System.out.print("Day: ");
            int day = readInt();

            System.out.println("\nChoose slot:");
            controller.printSlotChoices();
            System.out.print("Slot: ");
            int slot = readInt();

            System.out.print("Enter routine text: ");
            String content = readText();
            if (content.isEmpty()) {
                System.out.println("Routine text cannot be empty.");
                pause();
                return;
            }

            controller.setSlot(studentId, day, slot, content);
            System.out.println("Routine updated successfully.");
            pause();
        } catch (Exception e) {
            System.out.println("Failed to update routine: " + e.getMessage());
            pause();
        }
    }

    private void handleClear(String studentId) {
        try {
            System.out.println("\nChoose day to clear:");
            controller.printDayChoices();
            System.out.print("Day: ");
            int day = readInt();

            System.out.println("\nChoose slot to clear:");
            controller.printSlotChoices();
            System.out.print("Slot: ");
            int slot = readInt();

            controller.clearSlot(studentId, day, slot);
            System.out.println("Slot cleared successfully.");
            pause();
        } catch (Exception e) {
            System.out.println("Failed to clear slot: " + e.getMessage());
            pause();
        }
    }

    private void handleViewOne(String studentId) {
        try {
            System.out.println("\nChoose day:");
            controller.printDayChoices();
            System.out.print("Day: ");
            int day = readInt();

            System.out.println("\nChoose slot:");
            controller.printSlotChoices();
            System.out.print("Slot: ");
            int slot = readInt();

            String content = controller.getSlotContent(studentId, day, slot);
            if (content == null || content.trim().isEmpty()) {
                System.out.println("No routine saved for that slot.");
            } else {
                System.out.println("Saved routine: " + content);
            }
            pause();
        } catch (Exception e) {
            System.out.println("Failed to read slot: " + e.getMessage());
            pause();
        }
    }

    private int readInt() {
        while (true) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue; // safely skips leftover newline from previous menu
            try {
                return Integer.parseInt(line);
            } catch (Exception e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }

    private String readText() {
        while (true) {
            String line = sc.nextLine();
            if (line != null) return line.trim();
        }
    }

    private void pause() {
        System.out.print("Press Enter to continue...");
        sc.nextLine();
    }
}
