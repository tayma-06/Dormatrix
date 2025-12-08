package cli.dashboard;

import cli.components.DormatrixBanner;
import controllers.dashboard.MainDashboardController;

import java.util.Scanner;

public class MainDashboard {
    private final DormatrixBanner banner = new DormatrixBanner();
    private final Scanner sc = new Scanner(System.in);
    private final MainDashboardController controller = new MainDashboardController();

    public void show() {
        banner.printBanner();
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("|                   Welcome to IUT Female Dormitory                   |");
        System.out.println("-----------------------------------------------------------------------\n");

        while (true) {
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("|                           Select Role                               |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("| 1. Student                                                          |");
            System.out.println("| 2. Attendant                                                        |");
            System.out.println("| 3. Maintenance Worker                                               |");
            System.out.println("| 4. Store-in-Charge                                                  |");
            System.out.println("| 5. Hall Office                                                      |");
            System.out.println("| 6. Admin                                                            |");
            System.out.println("| 0. Exit                                                             |");
            System.out.println("-----------------------------------------------------------------------");

            System.out.print("Enter your choice: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("0")) {
                System.out.println("-----------------------------------------------------------------------");
                System.out.println("| Exiting Dormatrix. Goodbye!                                         |");
                System.out.println("-----------------------------------------------------------------------");
                System.exit(0);
            }

            System.out.print("Enter username: ");
            String username = sc.nextLine().trim();
            System.out.print("Enter password: ");
            String password = sc.nextLine().trim();

            controller.handleRoleInput(choice, username, password);
        }
    }
}
