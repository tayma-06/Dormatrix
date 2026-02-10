package cli.dashboard.account;

import cli.dashboard.Dashboard;
import controllers.dashboard.AccountDashboardController;
import utils.ConsoleUtil;
import utils.FastInput;

public class AccountDashboard implements Dashboard {

    private final AccountDashboardController controller;
    public AccountDashboard(AccountDashboardController controller) {
        this.controller = controller;
    }
    @Override
    public void show(String username) {
        while (true) {
            ConsoleUtil.clearScreen();
            System.out.println("═══════════════════════════════════════════════════════════════════════");
            System.out.println("|                 VIEW & SEARCH ACCOUNTS DASHBOARD                     |");
            System.out.println("═══════════════════════════════════════════════════════════════════════");
            System.out.println("| [1] View Accounts (By Role or All)                                  |");
            System.out.println("| [2] Search User by ID                                               |");
            System.out.println("| [0] Back                                                            |");
            System.out.println("═══════════════════════════════════════════════════════════════════════");
            System.out.println();
            System.out.print("Enter choice: ");
            int choice = FastInput.readInt();
            if (choice == 0) return;
            controller.handleInput(choice);
            ConsoleUtil.pause();
        }
    }
}
