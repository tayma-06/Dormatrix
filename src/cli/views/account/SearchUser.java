package cli.views.account;

import controllers.account.SearchUserController;
import utils.ConsoleUtil;
import utils.FastInput;

public class SearchUser {

    private final SearchUserController controller;

    public SearchUser(SearchUserController controller) {
        this.controller = controller;
    }

    public void show() {
        ConsoleUtil.clearScreen();
        System.out.println();
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("|                      Search Accounts                                |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.print("Enter User ID: ");

        String id = FastInput.readNonEmptyLine();
        String result = controller.searchById(id);

        if (result != null) {
            System.out.println(result);
        } else {
            System.out.println("User not found in any role.");
        }
    }
}
