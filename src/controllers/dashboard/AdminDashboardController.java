package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.forms.*;
import cli.views.*;
import controllers.authentication.AccountManager;
import java.util.Scanner;

public class AdminDashboardController {
    private final MainDashboard mainDashboard;
    private final AccountManager accountManager;
    private final Scanner scanner;
    private final CreateAccount createAccountForm;
    private final DeleteAccount deleteAccountForm;
    private final ViewAccount viewAccountForm;
    private final SearchUser searchUserForm;

    public AdminDashboardController() {
        this.mainDashboard = new MainDashboard();
        this.accountManager = new AccountManager();
        this.scanner = new Scanner(System.in);
        this.createAccountForm = new CreateAccount(accountManager, scanner);
        this.deleteAccountForm = new DeleteAccount(accountManager, scanner);
        this.viewAccountForm = new ViewAccount(accountManager, scanner);
        this.searchUserForm = new SearchUser(accountManager, scanner);
    }

    public void handleInput(int choice, String username) {
        switch (choice) {
            case 1:
                createAccountForm.show();
                break;
            case 2:
                deleteAccountForm.show();
                break;
            case 3:
                handleViewMenu();
                break;
            case 0:
                mainDashboard.show();
                break;
            default:
                System.out.println("Invalid choice. Please try again...");
        }
    }
    private void handleViewMenu() {
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("|                      View and Search Accounts                       |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("| 1. View Accounts (By Role or All)                                   |");
        System.out.println("| 2. Search User by ID                                                |");
        System.out.println("| 0. Exit                                                             |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.print("Enter choice: ");
        int viewChoice = scanner.nextInt();
        scanner.nextLine();
        switch (viewChoice) {
            case 1:
                viewAccountForm.show();
                break;
            case 2:
                searchUserForm.show();
                break;
            case 0:
                mainDashboard.show();
            default:
                System.out.println("Invalid choice!");
        }
    }
}