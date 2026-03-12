package controllers.dashboard.account;

import cli.views.account.SearchUser;
import cli.views.account.ViewAccount;

public class AccountDashboardController {

    private final ViewAccount viewAccount;
    private final SearchUser searchUser;

    public AccountDashboardController(ViewAccount viewAccount, SearchUser searchUser) {
        this.viewAccount = viewAccount;
        this.searchUser = searchUser;
    }

    public void handleInput(int choice) {
        switch (choice) {
            case 1 -> viewAccount.show();
            case 2 -> searchUser.show();
            default -> System.out.println("Invalid choice!");
        }
    }
}