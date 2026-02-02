package cli.dashboard;

import controllers.dashboard.HallOfficeDashboardController;
import utils.FastInput;
import utils.ConsoleUtil;

public class HallOfficeDashboard implements Dashboard {

    private final HallOfficeDashboardController controller = new HallOfficeDashboardController();

    @Override
    public void show(String username) {
        while (true) {
            ConsoleUtil.clearScreen();
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("|                        HALL OFFICE DASHBOARD                        |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("  Welcome, " + username);
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("| 1. Update Student Hall Room Info                                    |");
            System.out.println("| 2. View Student Complaints                                          |");
            System.out.println("| 3. View Worker Schedule                                             |");
            System.out.println("| 4. Handle Attendant Task                                            |");
            System.out.println("| 0. Logout                                                           |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.print("Enter your choice: ");

            int choice = FastInput.readInt();

            if (choice == 0) {
                System.out.println("-----------------------------------------------------------------------");
                System.out.println("| Logging Out....                                                     |");
                System.out.println("-----------------------------------------------------------------------");
                return;
            }

            controller.handleInput(choice, username);
        }
    }
}
