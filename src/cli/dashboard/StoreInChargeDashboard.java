package cli.dashboard;

import cli.components.DormatrixBanner;
import controllers.dashboard.StoreInChargeDashboardController;
import java.util.Scanner;
public class StoreInChargeDashboard implements Dashboard{
    private final StoreInChargeDashboardController controller = new StoreInChargeDashboardController();
    private final DormatrixBanner banner = new DormatrixBanner();
    private final Scanner sc = new Scanner(System.in);

    @Override
    public void show(String username)
    {
        while(true){
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("|STORE-IN-CHARGE DASHBOARD                      |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("Welcome, "+username);
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("| 1. Inventory List                                                   |");
            System.out.println("| 2. Purchase Record                                                  |");
            System.out.println("| 3. Sale Summary                                                     |");
            System.out.println("| 0. Logout                                                           |");
            System.out.println("-----------------------------------------------------------------------");

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
