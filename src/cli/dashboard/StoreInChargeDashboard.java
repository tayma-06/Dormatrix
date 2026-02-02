package cli.dashboard;

import controllers.dashboard.StoreInChargeDashboardController;
import utils.BackgroundFiller;
import utils.FastInput;
import utils.ConsoleUtil;

public class StoreInChargeDashboard implements Dashboard {

    private final StoreInChargeDashboardController controller =
            new StoreInChargeDashboardController();

    @Override
    public void show(String username) {
        while (true) {
            ConsoleUtil.clearScreen();
            BackgroundFiller.applyStoreInChargeTheme();
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("|                      STORE-IN-CHARGE DASHBOARD                      |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("  Welcome, " + username);
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("| 1. Inventory List                                                   |");
            System.out.println("| 2. Purchase Record                                                  |");
            System.out.println("| 3. Sale Summary                                                     |");
            System.out.println("| 0. Logout                                                           |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.print("Enter your choice: ");

            int choice = FastInput.readInt();

            if (choice == 0) {
                System.out.println("-----------------------------------------------------------------------");
                System.out.println("| Logging Out....                                                     |");
                System.out.println("-----------------------------------------------------------------------");
                BackgroundFiller.resetTheme();
                return;
            }

            controller.handleInput(choice, username);
        }
    }
}
