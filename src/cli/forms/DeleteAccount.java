package cli.forms;

import utils.FastInput;
import utils.InputHelper;
import utils.ConsoleUtil;
import controllers.authentication.AccountManager;
import controllers.authentication.AuthController;
import libraries.collections.MyString;

public class DeleteAccount {
    private final AccountManager manager;
    private final AuthController authController;

    public DeleteAccount(AccountManager manager) {
        this.manager = manager;
        this.authController = new AuthController();
    }

    public void show() {
        ConsoleUtil.clearScreen();
        System.out.println();
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("|                           DELETE ACCOUNT                            |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("| 1. Student                                                          |");
        System.out.println("| 2. Attendant                                                        |");
        System.out.println("| 3. Maintenance Worker                                               |");
        System.out.println("| 4. Store-in-Charge                                                  |");
        System.out.println("| 5. Hall Office                                                      |");
        System.out.println("| 6. Admin                                                            |");
        System.out.println("| 0. Cancel                                                           |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.print("Enter role choice (1-6): ");

        int choice = FastInput.readInt();
        if (choice == 0) return;

        MyString role = CreateAccount.getRoleFromChoice(choice);

        System.out.print("Enter User ID to delete: ");
        MyString idToDelete = new MyString(FastInput.readNonEmptyLine());
        System.out.println();
        System.out.println("SECURITY WARNING: You are about to delete a user.");
        System.out.println("Please re-enter your ADMIN credentials to confirm.");
        System.out.print("Admin Username: ");
        MyString adminUser = new MyString(FastInput.readNonEmptyLine());

        System.out.print("Admin Password: ");
        MyString adminPass = InputHelper.readPassword();

        boolean isAdmin = authController.authenticateUser(adminUser, adminPass, new MyString("ADMIN"));
        if (!isAdmin) {
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("| Authentication Failed! Deletion Cancelled.                          |");
            System.out.println("-----------------------------------------------------------------------");
            return;
        }

        if (manager.deleteUser(idToDelete, role)) {
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("| Account deleted successfully.                                       |");
            System.out.println("-----------------------------------------------------------------------");
        } else {
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("| Error: User ID not found in that role.                              |");
            System.out.println("-----------------------------------------------------------------------");
        }
    }
}
