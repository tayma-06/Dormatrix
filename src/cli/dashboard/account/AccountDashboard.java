package cli.dashboard.account;

import cli.dashboard.Dashboard;
import controllers.dashboard.account.AccountDashboardController;
import utils.*;
import static utils.TerminalUI.*;

public class AccountDashboard implements Dashboard {

    private final AccountDashboardController controller;

    private static final MenuItem[] MENU = {
        new MenuItem(1, "View Accounts (By Role or All)"),
        new MenuItem(2, "Search User by ID"),
        new MenuItem(0, "Back"),};

    public AccountDashboard(AccountDashboardController controller) {
        this.controller = controller;
    }

    @Override
    public void show(String username) {
        while (true) {
            try {
                ConsoleUtil.clearScreen();
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                System.out.print(HIDE_CUR);

                int menuStartRow = 3;
                int promptRow = drawDashboard(
                        "VIEW & SEARCH ACCOUNTS",
                        "Account Management",
                        MENU,
                        TerminalUI.getActiveTextColor(),
                        TerminalUI.getActiveBoxColor(),
                        null,
                        menuStartRow
                );

                System.out.print(SHOW_CUR);
                int choice = FastInput.readInt();
                System.out.print(RESET);

                if (choice == 0) {
                    ConsoleUtil.clearScreen();
                    return;
                }

                controller.handleInput(choice);
                TerminalUI.tPause();

            } catch (Exception e) {
                cleanup();
                System.err.println("[AccountDashboard] " + e.getMessage());
            }
        }
    }
}
