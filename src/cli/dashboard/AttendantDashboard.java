package cli.dashboard;

import controllers.dashboard.AttendantDashboardController;
import cli.complaint.AttendantComplaintCLI;
import utils.BackgroundFiller;
import utils.FastInput;
import utils.ConsoleUtil;

public class AttendantDashboard implements Dashboard {

    private final AttendantDashboardController controller = new AttendantDashboardController();

    @Override
    public void show(String username) {
        while (true) {
            ConsoleUtil.clearScreen();
            BackgroundFiller.applyAttendantTheme();
            System.out.println();
            System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                        ATTENDANT DASHBOARD                          ║");
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");

            String welcomeMessage = "Welcome, " + username;
            int totalWidth = 69;
            int paddingLeft = (totalWidth - welcomeMessage.length()) / 2;
            int paddingRight = totalWidth - welcomeMessage.length() - paddingLeft;
            String formattedWelcome =
                    String.format("║%" + paddingLeft + "s%s%" + paddingRight + "s║", "", welcomeMessage, "");
            System.out.println(formattedWelcome);

            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ [1] Handle Student Complaints                                       ║");
            System.out.println("║ [2] Handle Worker Schedule                                          ║");
            System.out.println("║ [3] Add Found Items                                                 ║");
            System.out.println("║ [0] Logout                                                          ║");
            System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

            System.out.println();
            System.out.print("Enter your choice: ");
            int choice = FastInput.readInt();

            if (choice == 0) {
                System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
                System.out.println("║                         Logging Out....                             ║");
                System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
                BackgroundFiller.resetTheme();
                return;
            }

            if (choice == 1) {
                new AttendantComplaintCLI().start();
                continue;
            }

            controller.handleInput(choice, username);
        }
    }
}
