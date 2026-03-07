package cli.forms.account;

import controllers.account.DeleteAccountController;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.InputHelper;
import utils.TerminalUI;

public class DeleteAccount {

    private final DeleteAccountController controller;

    public DeleteAccount(DeleteAccountController controller) {
        this.controller = controller;
    }

    public void show() {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tSubDashboard("DELETE ACCOUNT", new String[]{
            "[1] Student",
            "[2] Attendant",
            "[3] Maintenance Worker",
            "[4] Store-in-Charge",
            "[5] Hall Office",
            "[6] Admin",
            "[7] Cafeteria Manager",
            "[0] Back"
        });

        int roleChoice = FastInput.readInt();
        if (roleChoice == 0) return;

        TerminalUI.tPrompt("Enter User ID to delete: ");
        String idToDelete = FastInput.readNonEmptyLine();

        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("SECURITY WARNING");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("You are about to delete a user.");
        TerminalUI.tBoxLine("Please re-enter your ADMIN credentials to confirm.");
        TerminalUI.tBoxBottom();

        TerminalUI.tPrompt("Admin Username: ");
        String adminUser = FastInput.readNonEmptyLine();

        TerminalUI.tPrompt("Admin Password: ");
        String adminPass = InputHelper.readPassword().getValue();

        String result = controller.deleteUserWithAdminConfirmation(
                roleChoice,
                idToDelete,
                adminUser,
                adminPass
        );

        TerminalUI.tEmpty();
        if (result.toLowerCase().contains("success") || result.toLowerCase().contains("deleted")) {
            TerminalUI.tSuccess(result);
        } else {
            TerminalUI.tError(result);
        }
    }
}
