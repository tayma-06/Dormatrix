package cli.dashboard;

import controllers.dashboard.AttendantDashboardController;
import java.util.Scanner;

public class AttendantDashboard implements Dashboard {

    private final AttendantDashboardController controller = new AttendantDashboardController();
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
            int choice = sc.nextInt();

            if (choice == 0) {
                System.out.println("-----------------------------------------------------------------------");
                System.out.println("| Logging Out....                                                     |");
                System.out.println("-----------------------------------------------------------------------");
            }
            controller.handleInput(choice, username);
        }

    }
}