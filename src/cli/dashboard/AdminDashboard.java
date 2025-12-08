package cli.dashboard;

import cli.components.DormatrixBanner;
import controllers.dashboard.AdminDashboardController;

import java.util.Scanner;

public class AdminDashboard implements Dashboard {
    private final AdminDashboardController controller = new AdminDashboardController();
    private final DormatrixBanner banner = new DormatrixBanner();
    private final Scanner sc = new Scanner(System.in);

    @Override
    public void show(String username) {
        while (true) {
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("|                           ADMIN DASHBOARD                           |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("  Welcome, " + username + "                                             ");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("| 1. Create Account                                                   |");
            System.out.println("| 2. Delete Account                                                   |");
            System.out.println("| 3. View Accounts by Role                                            |");
            System.out.println("| 0. Logout                                                           |");
            System.out.println("-----------------------------------------------------------------------");

            System.out.print("Enter your choice: ");
            String choice = sc.nextLine().trim();

            if (!controller.handleInput(choice, username)) {
                System.out.println("Logging out...");
                break;
            }
        }
    }
}
