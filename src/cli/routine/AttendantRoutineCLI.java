package cli.routine;

import controllers.routine.RoutineController;
import utils.BackgroundFiller;
import utils.ConsoleUtil;

import java.util.Scanner;

public class AttendantRoutineCLI {
    private final RoutineController controller = new RoutineController();
    private final Scanner sc = new Scanner(System.in);

    public void show() {
        while (true) {
            ConsoleUtil.clearScreen();
            BackgroundFiller.applyAttendantTheme();

            System.out.println();
            System.out.println("╔═════════════════════ STUDENT ROUTINE VIEW (MASKED) ═════════════════════╗");
            System.out.println("[1] View masked student routine");
            System.out.println("[0] Back");
            System.out.print("Enter choice: ");

            int choice = readInt();
            if (choice == 0) return;

            if (choice == 1) {
                System.out.print("Enter student ID/username: ");
                String studentId = readText();
                System.out.println();
                controller.printMaskedRoutine(studentId);
                pause();
            } else {
                System.out.println("Invalid choice.");
                pause();
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
