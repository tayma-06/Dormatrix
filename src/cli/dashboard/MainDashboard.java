package cli.dashboard;

import cli.components.DormatrixBanner;
import utils.InputHelper;
import utils.FastInput;
import utils.ConsoleUtil;
import utils.ConsoleColors;
import controllers.dashboard.MainDashboardController;
import libraries.collections.MyString;

public class MainDashboard {
    private final DormatrixBanner banner = new DormatrixBanner();
    private final MainDashboardController controller = new MainDashboardController();

    private static final String BOX = ConsoleColors.LIGHT_PINK;
    private static final String EXIT_BOX = ConsoleColors.HOT_PINK;
    private static final String INPUT = ConsoleColors.BRIGHT_BLUE;
    private static final String RESET = ConsoleColors.RESET;

    public void show() {
        while (true) {
            ConsoleUtil.clearScreen();
            banner.printBanner();

            System.out.println(BOX + "-----------------------------------------------------------------------" + RESET);
            System.out.println(BOX + "|                   Welcome to IUT Female Dormitory                   |" + RESET);
            System.out.println(BOX + "-----------------------------------------------------------------------" + RESET + "\n");

            System.out.println(BOX + "-----------------------------------------------------------------------" + RESET);
            System.out.println(BOX + "|                           Select Role                               |" + RESET);
            System.out.println(BOX + "-----------------------------------------------------------------------" + RESET);

            System.out.println(BOX + "| 1. Student                                                          |" + RESET);
            System.out.println(BOX + "| 2. Attendant                                                        |" + RESET);
            System.out.println(BOX + "| 3. Maintenance Worker                                               |" + RESET);
            System.out.println(BOX + "| 4. Store-in-Charge                                                  |" + RESET);
            System.out.println(BOX + "| 5. Hall Office                                                      |" + RESET);
            System.out.println(BOX + "| 6. Admin                                                            |" + RESET);
            System.out.println(BOX + "| 0. Exit                                                             |" + RESET);
            System.out.println(BOX + "-----------------------------------------------------------------------" + RESET);

            System.out.print(INPUT + "Enter your choice: " + RESET);
            int choice = FastInput.readInt();

            if (choice == 0) {
                ConsoleUtil.clearScreen();

                System.out.println(EXIT_BOX + "-----------------------------------------------------------------------" + RESET);
                System.out.println(EXIT_BOX + "| Exiting Dormatrix. Goodbye!                                         |" + RESET);
                System.out.println(EXIT_BOX + "-----------------------------------------------------------------------" + RESET);

                System.out.print(INPUT + "Press Enter 0 to close... " + RESET);
                FastInput.readNonEmptyLine();

                System.exit(0);
            }

            System.out.print(INPUT + "Enter userID: " + RESET);
            MyString username = new MyString(FastInput.readNonEmptyLine());

            System.out.print(INPUT + "Enter password: " + RESET);
            MyString password = InputHelper.readPassword();

            controller.handleRoleInput(choice, username, password);
        }
    }
}
