package cli.routine;

import controllers.routine.RoutineController;
import utils.*;
import static utils.TerminalUI.*;

public class StudentRoutineCLI {

    private final RoutineController controller = new RoutineController();

    private static final MenuItem[] MENU = {
        new MenuItem(1, "Edit Slot"),
        new MenuItem(2, "Clear Slot"),
        new MenuItem(3, "View Exact Slot Text"),
        new MenuItem(0, "Back"),};

    public void show(String studentId) {
        while (true) {
            try {
                ConsoleUtil.clearScreen();
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                TerminalUI.at(2, 1);

                controller.printStudentRoutine(studentId);

                TerminalUI.tEmpty();
                TerminalUI.tSubDashboard("WEEKLY ROUTINE  —  Student: " + studentId, new String[]{
                    "[1] Edit Slot",
                    "[2] Clear Slot",
                    "[3] View Exact Slot Text",
                    "[0] Back"
                });

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

    private void handleEdit(String studentId) {
        try {
            System.out.println("\nChoose day:");
            controller.printDayChoices();
            tPrompt("Day: ");
            int day = FastInput.readInt();

            System.out.println("\nChoose slot:");
            controller.printSlotChoices();
            tPrompt("Slot: ");
            int slot = FastInput.readInt();

            tPrompt("Enter routine text: ");
            String content = FastInput.readNonEmptyLine();

            controller.setSlot(studentId, day, slot, content);
            tSuccess("Routine updated successfully.");
            tPause();
        } catch (Exception e) {
            tError("Failed to update routine: " + e.getMessage());
            tPause();
        }
    }

    private void handleClear(String studentId) {
        try {
            System.out.println("\nChoose day to clear:");
            controller.printDayChoices();
            tPrompt("Day: ");
            int day = FastInput.readInt();

            System.out.println("\nChoose slot to clear:");
            controller.printSlotChoices();
            tPrompt("Slot: ");
            int slot = FastInput.readInt();

            controller.clearSlot(studentId, day, slot);
            tSuccess("Slot cleared successfully.");
            tPause();
        } catch (Exception e) {
            tError("Failed to clear slot: " + e.getMessage());
            tPause();
        }
    }

    private void handleViewOne(String studentId) {
        try {
            System.out.println("\nChoose day:");
            controller.printDayChoices();
            tPrompt("Day: ");
            int day = FastInput.readInt();

            System.out.println("\nChoose slot:");
            controller.printSlotChoices();
            tPrompt("Slot: ");
            int slot = FastInput.readInt();

            String content = controller.getSlotContent(studentId, day, slot);
            if (content == null || content.trim().isEmpty()) {
                tError("No routine saved for that slot.");
            } else {
                tSuccess("Saved routine: " + content);
            }
            tPause();
        } catch (Exception e) {
            tError("Failed to read slot: " + e.getMessage());
            tPause();
        }
    }
}
