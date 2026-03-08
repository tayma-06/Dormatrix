package cli.dashboard;

import controllers.dashboard.StudentDashboardController;
import utils.BackgroundFiller;
import utils.ConsoleUtil;
import utils.FastInput;

public class StudentDashboard implements Dashboard {

    private final StudentDashboardController controller
            = new StudentDashboardController();

    @Override
    public void show(String username) {
        while (true) {
            ConsoleUtil.clearScreen();
            BackgroundFiller.applyStudentTheme();
            System.out.println();
            System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                        STUDENT DASHBOARD                            ║");
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");

            String welcomeMessage = "Welcome, " + username;
            int totalWidth = 69;
            int paddingLeft = (totalWidth - welcomeMessage.length()) / 2;
            int paddingRight = totalWidth - welcomeMessage.length() - paddingLeft;
            String formattedWelcome
                    = String.format("║%" + paddingLeft + "s%s%" + paddingRight + "s║", "", welcomeMessage, "");
            System.out.println(formattedWelcome);

            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ [1]  View Room Info                                                 ║");
            System.out.println("║ [2]  Facility Booking                                               ║");
            System.out.println("║ [3]  Meal Token Purchase                                            ║");
            System.out.println("║ [4]  Store Account & Dues                                           ║");
            System.out.println("║ [5]  Lost & Found                                                   ║");
            System.out.println("║ [6]  Complaint Menu                                                 ║");
            System.out.println("║ [7]  Weekly Routine                                                 ║");
            System.out.println("║ [8]  View Announcements                                             ║");
            System.out.println("║ [9]  Store Shopping Cart                                            ║");
            System.out.println("║ [10] Emergency Contacts                                             ║");
            System.out.println("║ [0]  Logout                                                         ║");
            System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

            System.out.println();
            System.out.print("Enter your choice: ");

            int choice = FastInput.readInt();

            if (choice == 0) {
                ConsoleUtil.clearScreen();
                System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
                System.out.println("║                         Logging Out....                             ║");
                System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
                BackgroundFiller.resetTheme();
                return;
            }

            controller.handleInput(choice, username);
        }
    }
}
