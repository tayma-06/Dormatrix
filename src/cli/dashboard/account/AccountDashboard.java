package cli.dashboard.account;

import cli.dashboard.Dashboard;
import controllers.dashboard.account.AccountDashboardController;
import utils.ConsoleUtil;
import utils.TerminalUI;

import static utils.TerminalUI.*;

public class AccountDashboard implements Dashboard {

    private final AccountDashboardController controller;

    private static final MenuItem[] MENU = {
            new MenuItem(1, "View Accounts"),
            new MenuItem(2, "Search User by ID"),
            new MenuItem(0, "Back")
    };

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

                drawDashboard(
                        "VIEW & SEARCH ACCOUNTS",
                        "Choose View to browse users or Search to open one directly",
                        MENU,
                        TerminalUI.getActiveTextColor(),
                        TerminalUI.getActiveBoxColor(),
                        new String[]{
                                "Press Enter to open"
                        },
                        3
                );

                int choice = readChoiceArrow();
                System.out.print(RESET);

                if (choice == 0) {
                    return;
                }

                controller.handleInput(choice);

            } catch (Exception e) {
                cleanup();
                System.err.println("[AccountDashboard] " + e.getMessage());
                return;
            }
        }
    }
}