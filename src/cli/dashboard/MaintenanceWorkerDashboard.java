package cli.dashboard;

import cli.components.DormatrixBanner;
import controllers.dashboard.MaintenanceWorkerDashboardController;
import java.util.Scanner;

public class MaintenanceWorkerDashboard implements Dashboard{
    private final MaintenanceWorkerDashboardController controller= new MaintenanceWorkerDashboardController();
    private final DormatrixBanner banner = new DormatrixBanner();

    private final Scanner sc = new Scanner(System.in);

    @Override
    public void show(String username)
    {
        while (true) { 
            System.out.println("-----------------------------------------------------------------");
            System.out.println("|                 MAINTENANCE WORKER DASHBOARD                  |");
            System.out.println("-----------------------------------------------------------------");
            System.out.println("  Welcome, "+username);
            System.out.println("-----------------------------------------------------------------");
            System.out.println("| 1. Work Field                                                 |");
            System.out.println("| 2. View Task Queue                                            |");
            System.out.println("| 3. Updated Student Complain Status                            |");
            System.out.println("| 0. Logout                                                     |");
            System.out.println("-----------------------------------------------------------------");

            System.out.println("Enter your choice: ");
            int choice = sc.nextInt();

            if(!controller.handleInput(choice, username))
            {
                System.out.println("Logging out...");
                break;
            }
             
        }
    }
}
