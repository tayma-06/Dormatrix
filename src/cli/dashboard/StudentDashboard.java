package cli.dashboard;

import controllers.dashboard.StudentDashboardController;
import cli.complaint.StudentComplaintCLI;

import java.util.Scanner;

public class StudentDashboard implements Dashboard {

    private final StudentDashboardController controller = new StudentDashboardController();
    private final Scanner sc = new Scanner(System.in);

    @Override
    public void show(String username) {
        while (true) {
            System.out.println("\n-----------------------------------------------------------------------");
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

            int choice = -1;
            try {
                choice = sc.nextInt();
                sc.nextLine();
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine();
                continue;
            }

            if (choice == 0) {
                System.out.println("-----------------------------------------------------------------------");
                System.out.println("| Logging Out....                                                     |");
                System.out.println("-----------------------------------------------------------------------");
                return;
            }

            if (choice == 6) {
                cli.Input.SC.nextLine();
                new cli.complaint.StudentComplaintCLI().start(username);
                continue;
            }


            controller.handleInput(choice, username);
        }
    }
}