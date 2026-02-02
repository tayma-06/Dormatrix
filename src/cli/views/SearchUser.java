package cli.views;

import controllers.authentication.AccountManager;
import libraries.collections.MyString;
import utils.FastInput;
import utils.ConsoleUtil;

public class SearchUser {
    private final AccountManager manager;

    public SearchUser(AccountManager manager) {
        this.manager = manager;
    }

    public void show() {
        ConsoleUtil.clearScreen();
        System.out.println();
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("|                      Search Accounts                                |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.print("Enter User ID: ");
        MyString id = new MyString(FastInput.readNonEmptyLine());
        String result = manager.findUserDetails(id);
        if (result != null) {
            System.out.println(result);
        } else {
            System.out.println("User not found in any role.");
        }
    }
}
