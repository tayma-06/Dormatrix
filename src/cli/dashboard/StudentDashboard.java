package cli.dashboard;

import cli.complaint.StudentComplaintCLI;
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
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("|                        STUDENT DASHBOARD                            |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("  Welcome, " + username);
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("| 1. View Room Info                                                   |");
            System.out.println("| 2. Facility Booking                                                 |");
            System.out.println("| 3. Meal Token Purchase                                              |");
            System.out.println("| 4. Store Purchases & Dues                                           |");
            System.out.println("| 5. Lost & Found                                                     |");
            System.out.println("| 6. Submit Complaint                                                 |");
            System.out.println("| 7. View Announcements                                               |");
            System.out.println("| 0. Logout                                                           |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.print("Enter your choice: ");

            int choice = FastInput.readInt();

            if (choice == 0) {
                System.out.println("-----------------------------------------------------------------------");
                System.out.println("| Logging Out....                                                     |");
                System.out.println("-----------------------------------------------------------------------");
                BackgroundFiller.resetTheme();
                return;
            }

            if (choice == 6) {
                new StudentComplaintCLI().start(username);
                continue;
            }

            controller.handleInput(choice, username);
        }
    }
}
