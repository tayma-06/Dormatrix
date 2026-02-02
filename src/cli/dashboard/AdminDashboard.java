package cli.dashboard;

import controllers.dashboard.AdminDashboardController;
import utils.FastInput;
import utils.ConsoleUtil;

public class AdminDashboard implements Dashboard {
    private final AdminDashboardController controller = new AdminDashboardController();

    @Override
    public void show(String username) {
        while (true) {
            ConsoleUtil.clearScreen();
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("|                           ADMIN DASHBOARD                           |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("  Welcome, " + username + "                                             ");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("| 1. Create Account                                                   |");
            System.out.println("| 2. Delete Account                                                   |");
            System.out.println("| 3. View and Search Accounts                                         |");
            System.out.println("| 4. Manage Rooms                                                     |");
            System.out.println("| 0. Logout                                                           |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.print("Enter your choice: ");

            int choice = FastInput.readInt();

            if (choice == 0) {
                System.out.println("Logging Out....");
                return;
            }

            controller.handleInput(choice, username);
        }
    }
}
