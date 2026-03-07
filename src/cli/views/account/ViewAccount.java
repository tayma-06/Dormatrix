package cli.views.account;

import controllers.account.ViewAccountController;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

public class ViewAccount {

    private final ViewAccountController controller;

    public ViewAccount(ViewAccountController controller) {
        this.controller = controller;
    }

    public void show() {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tSubDashboard("VIEW ACCOUNTS", new String[]{
            "[1] Student",
            "[2] Attendant",
            "[3] Maintenance Worker",
            "[4] Store-in-Charge",
            "[5] Hall Office",
            "[6] Admin",
            "[7] View All Accounts",
            "[0] Back"
        });

        int choice = FastInput.readInt();
        if (choice == 0) return;

        controller.handleViewChoice(choice);
    }
}
