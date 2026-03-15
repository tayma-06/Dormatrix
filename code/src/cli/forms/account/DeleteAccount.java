package cli.forms.account;

import controllers.account.DeleteAccountController;
import controllers.authentication.AuthController;
import utils.FastInput;

import static utils.TerminalUI.*;

public class DeleteAccount {

    private final DeleteAccountController controller;

    public DeleteAccount(DeleteAccountController controller) {
        this.controller = controller;
    }

    public void show() {
        fillBackground(getActiveBgColor());
        at(2, 1);

        tSubDashboard("DELETE ACCOUNT", new String[]{
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
        if (roleChoice == 0) {
            return;
        }

        fillBackground(getActiveBgColor());
        at(2, 1);

        tBoxTop();
        tBoxTitle("DELETE ACCOUNT");
        tBoxSep();
        tBoxLine("Enter the target user ID and confirm admin credentials.");
        tBoxBottom();
        tEmpty();

        tPrompt("Enter User ID to delete: ");
        String idToDelete = FastInput.readNonEmptyLine();

        tEmpty();
        tBoxTop();
        tBoxTitle("SECURITY WARNING");
        tBoxSep();
        tBoxLine("You are about to permanently delete a user account.");
        tBoxLine("Please re-enter ADMIN credentials to continue.");
        tBoxBottom();
        tEmpty();

        tPrompt("Admin Username: ");
        String adminUser = FastInput.readNonEmptyLine();

        String adminPass = AuthController.readPassword("Admin Password: ").getValue();

        String result = controller.deleteUserWithAdminConfirmation(
                roleChoice,
                idToDelete,
                adminUser,
                adminPass
        );

        fillBackground(getActiveBgColor());
        at(2, 1);

        if (result.toLowerCase().contains("success") || result.toLowerCase().contains("deleted")) {
            tSuccess(result);
        } else {
            tError(result);
        }
        tPause();
    }
}