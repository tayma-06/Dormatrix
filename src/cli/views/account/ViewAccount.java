package cli.views.account;

import controllers.account.ViewAccountController;
import utils.ConsoleUtil;
import utils.FastInput;

public class ViewAccount {

    private final ViewAccountController controller;

    public ViewAccount(ViewAccountController controller) {
        this.controller = controller;
    }

    public void show() {
        ConsoleUtil.clearScreen();
        System.out.println();
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("|                          VIEW ACCOUNTS                              |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("| [1] Student                                                         |");
        System.out.println("| [2] Attendant                                                       |");
        System.out.println("| [3] Maintenance Worker                                              |");
        System.out.println("| [4] Store-in-Charge                                                 |");
        System.out.println("| [5] Hall Office                                                     |");
        System.out.println("| [6] Admin                                                           |");
        System.out.println("| [7] View All Accounts                                               |");
        System.out.println("| [0] Back                                                            |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println();
        System.out.print("Enter choice: ");

        int choice = FastInput.readInt();
        if (choice == 0) return;

        controller.handleViewChoice(choice);
    }
}
