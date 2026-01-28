package cli.dashboard;

import controllers.dashboard.AdminDashboardController;
import java.util.Scanner;

public class AdminDashboard implements Dashboard {
    private final AdminDashboardController controller = new AdminDashboardController();
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
            System.out.println("| 3. View and Search Accounts                                         |");
            System.out.println("| 4. Manage Rooms                                                     |");
            System.out.println("| 0. Logout                                                           |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.print("Enter your choice: ");
            if (sc.hasNextInt()) {
                int choice = sc.nextInt();
                sc.nextLine();
                if (choice == 0) {
                    System.out.println("Logging Out....");
                }
                controller.handleInput(choice, username);
            } else {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine();
            }
        }
    }
}