package cli.routine;

import controllers.routine.RoutineController;
import utils.*;
import static utils.TerminalUI.*;

public class AttendantRoutineCLI {

    private final RoutineController controller = new RoutineController();

    private static final MenuItem[] MENU = {
        new MenuItem(1, "View masked student routine"),
        new MenuItem(0, "Back"),};

    public void show() {
        while (true) {
            try {
                ConsoleUtil.clearScreen();
                BackgroundFiller.applyAttendantTheme();
                System.out.print(HIDE_CUR);

                int menuStartRow = 3;
                int promptRow = drawDashboard(
                        "STUDENT ROUTINE VIEW (MASKED)",
                        "Attendant Access",
                        MENU,
                        TerminalUI.getActiveTextColor(),
                        TerminalUI.getActiveBoxColor(),
                        null,
                        menuStartRow
                );

                System.out.print(SHOW_CUR);
                int choice = FastInput.readInt();
                System.out.print(RESET);

                if (choice == 0) {
                    return;
                }

                if (choice == 1) {
                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyAttendantTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);
                    TerminalUI.tBoxTop();
                    TerminalUI.tBoxTitle("VIEW STUDENT ROUTINE");
                    TerminalUI.tBoxBottom();
                    tPrompt("Enter student ID/username: ");
                    String studentId = FastInput.readNonEmptyLine();
                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyAttendantTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(3, 1);
                    controller.printMaskedRoutine(studentId);
                    tEmpty();
                    tPause();
                } else {
                    tError("Invalid choice.");
                    tPause();
                }

            } catch (Exception e) {
                cleanup();
                System.err.println("[AttendantRoutineCLI] " + e.getMessage());
            }
        }
    }
}
