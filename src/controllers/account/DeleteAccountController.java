package controllers.account;

import controllers.authentication.AccountManager;
import controllers.authentication.AuthController;
import libraries.collections.MyString;
import utils.RoleMapper;

public class DeleteAccountController {

    private final AccountManager manager;
    private final AuthController authController;

    public DeleteAccountController(AccountManager manager) {
        this.manager = manager;
        this.authController = new AuthController();
    }

    public String deleteUserWithAdminConfirmation(
            int roleChoice,
            String idToDelete,
            String adminUsername,
            String adminPassword
    ) {
        MyString role = RoleMapper.getRoleFromChoice(roleChoice);
        if (role.getValue().equals("UNKNOWN")) return "Invalid role choice!";

        boolean isAdmin = authController.authenticateUser(
                new MyString(adminUsername),
                new MyString(adminPassword),
                new MyString("ADMIN")
        );

        if (!isAdmin) {
            return "Authentication Failed! Deletion Cancelled.";
        }

        boolean deleted = manager.deleteUser(new MyString(idToDelete), role);
        return deleted ? "Account deleted successfully." : "Error: User ID not found in that role.";
    }
}
