package cli.dashboard;

import cli.components.DormatrixBanner;
import controllers.dashboard.HallOfficeDashboardController;
import java.util.Scanner;
public class HallOfficeDashboard implements Dashboard{
    private final HallOfficeDashboardController controller = new HallOfficeDashboardController();
    private final DormatrixBanner banner = new DormatrixBanner();
    private final Scanner sc = new Scanner(System.in);

    @Override
    public void show(String username)
    {
       while (true)
        {
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("|                        HALL OFFICE DASHBOARD                        |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("  Welcome, "+username);
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("| 1. Update Student Hall Room Info                                    |");
            System.out.println("| 2. View Student Complaints                                          |");
            System.out.println("| 3. View Worker Schedule                                             |");
            System.out.println("| 4. Handle Attendant Task                                            |");
            System.out.println("| 0. Logout                                                           |");
            System.out.println("-----------------------------------------------------------------------");

            System.out.println("Enter your choice: ");

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
