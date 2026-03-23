package controllers.dashboard;

import cli.dashboard.account.AccountDashboard;
import cli.forms.account.CreateAccount;
import cli.forms.account.DeleteAccount;
import cli.profile.EditProfileCLI;
import cli.views.account.SearchUser;
import cli.views.account.ViewAccount;
import controllers.account.CreateAccountController;
import controllers.account.DeleteAccountController;
import controllers.account.SearchUserController;
import controllers.account.ViewAccountController;
import controllers.authentication.AccountManager;
import controllers.dashboard.account.AccountDashboardController;

public class AdminDashboardController {

    private final AccountManager accountManager;
    private final CreateAccount createAccountForm;
    private final DeleteAccount deleteAccountForm;
    private final AccountDashboard accountDashboard;

    public AdminDashboardController() {
        this.accountManager = new AccountManager();

        CreateAccountController createAccountController = new CreateAccountController(accountManager);
        DeleteAccountController deleteAccountController = new DeleteAccountController(accountManager);
        ViewAccountController viewAccountController = new ViewAccountController(accountManager);
        SearchUserController searchUserController = new SearchUserController(accountManager);

        this.createAccountForm = new CreateAccount(createAccountController);
        this.deleteAccountForm = new DeleteAccount(deleteAccountController);

        ViewAccount viewAccountView = new ViewAccount(viewAccountController);
        SearchUser searchUserView = new SearchUser(searchUserController);

        AccountDashboardController accountDashboardController =
                new AccountDashboardController(viewAccountView, searchUserView);

        this.accountDashboard = new AccountDashboard(accountDashboardController);
    }

    public void handleInput(int choice, String username) {
        switch (choice) {
            case 1 -> createAccountForm.show();
            case 2 -> deleteAccountForm.show();
            case 3 -> accountDashboard.show(username);
            case 4 -> new EditProfileCLI(username, "ADMIN").start();
            default -> {
            }
        }
    }
}