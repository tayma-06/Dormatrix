package cli.forms.account;

import controllers.account.DeleteAccountController;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.InputHelper;

public class DeleteAccount {

    private final DeleteAccountController controller;

    public DeleteAccount(DeleteAccountController controller) {
        this.controller = controller;
    }

    public void show() {
        ConsoleUtil.clearScreen();

        System.out.println();
        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                           DELETE ACCOUNT                            ║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
        System.out.println("║ [1] Student                                                         ║");
        System.out.println("║ [2] Attendant                                                       ║");
        System.out.println("║ [3] Maintenance Worker                                              ║");
        System.out.println("║ [4] Store-in-Charge                                                 ║");
        System.out.println("║ [5] Hall Office                                                     ║");
        System.out.println("║ [6] Admin                                                           ║");
        System.out.println("║ [7] Cafeteria Manager                                               ║");
        System.out.println("║ [0] Back                                                            ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

        System.out.println();
        System.out.print("Enter role choice (1-6): ");

        int roleChoice = FastInput.readInt();
        if (roleChoice == 0) return;

        System.out.print("Enter User ID to delete: ");
        String idToDelete = FastInput.readNonEmptyLine();

        System.out.println();
        System.out.println("SECURITY WARNING: You are about to delete a user.");
        System.out.println("Please re-enter your ADMIN credentials to confirm.");

        System.out.print("Admin Username: ");
        String adminUser = FastInput.readNonEmptyLine();

        System.out.print("Admin Password: ");
        String adminPass = InputHelper.readPassword().getValue();

        String result = controller.deleteUserWithAdminConfirmation(
                roleChoice,
                idToDelete,
                adminUser,
                adminPass
        );

        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("║ " + result);
        System.out.println("═══════════════════════════════════════════════════════════════════════");
    }
}
