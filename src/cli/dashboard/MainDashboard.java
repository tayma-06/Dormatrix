package cli.dashboard;

import cli.components.DormatrixBanner;
import utils.InputHelper;
import utils.FastInput;
import utils.ConsoleUtil;
import controllers.dashboard.MainDashboardController;
import libraries.collections.MyString;

public class MainDashboard {
    private final DormatrixBanner banner = new DormatrixBanner();
    private final MainDashboardController controller = new MainDashboardController();

    public void show() {
        while (true) {
            ConsoleUtil.clearScreen();
            banner.printBanner();
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("|                   Welcome to IUT Female Dormitory                   |");
            System.out.println("-----------------------------------------------------------------------\n");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("|                           Select Role                               |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("| 1. Student                                                          |");
            System.out.println("| 2. Attendant                                                        |");
            System.out.println("| 3. Maintenance Worker                                               |");
            System.out.println("| 4. Store-in-Charge                                                  |");
            System.out.println("| 5. Hall Office                                                      |");
            System.out.println("| 6. Admin                                                            |");
            System.out.println("| 0. Exit                                                             |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.print("Enter your choice: ");
            int choice = FastInput.readInt();
            if (choice == 0) {
                System.out.println("-----------------------------------------------------------------------");
                System.out.println("| Exiting Dormatrix. Goodbye!                                         |");
                System.out.println("-----------------------------------------------------------------------");
                System.exit(0);
            }
            System.out.print("Enter userID: ");
            MyString username = new MyString(FastInput.readNonEmptyLine());
            System.out.print("Enter password: ");
            MyString password = InputHelper.readPassword();
            controller.handleRoleInput(choice, username, password);
        }
    }
}
