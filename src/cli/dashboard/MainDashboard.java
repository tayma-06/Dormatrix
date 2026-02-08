package cli.dashboard;

import cli.components.DormatrixBanner;
import utils.InputHelper;
import utils.FastInput;
import utils.ConsoleUtil;
import utils.ConsoleColors;
import utils.BackgroundFiller;
import controllers.dashboard.MainDashboardController;
import libraries.collections.MyString;

public class MainDashboard {
    private final DormatrixBanner banner = new DormatrixBanner();
    private final MainDashboardController controller = new MainDashboardController();

    private static final String BOX = ConsoleColors.Accent.BOX;
    private static final String EXIT_BOX = ConsoleColors.Accent.EXIT;
    private static final String INPUT = ConsoleColors.Accent.INPUT;

    public void show() {
        while (true) {
            ConsoleUtil.clearScreen();
            BackgroundFiller.applyMainMenuTheme();
            System.out.println();
            banner.printBannerOnTheme();
            System.out.println(BOX + "-----------------------------------------------------------------------");
            System.out.println(BOX + "|                   Welcome to IUT Female Dormitory                   |");
            System.out.println(BOX + "-----------------------------------------------------------------------\n");

            System.out.println(BOX + "-----------------------------------------------------------------------");
            System.out.println(BOX + "|                           Select Role                               |");
            System.out.println(BOX + "-----------------------------------------------------------------------");

            System.out.println(BOX + "| 1. Student                                                          |");
            System.out.println(BOX + "| 2. Attendant                                                        |");
            System.out.println(BOX + "| 3. Maintenance Worker                                               |");
            System.out.println(BOX + "| 4. Store-in-Charge                                                  |");
            System.out.println(BOX + "| 5. Hall Office                                                      |");
            System.out.println(BOX + "| 6. Admin                                                            |");
            System.out.println(BOX + "| 7. Cafeteria Manager                                                           |");
            System.out.println(BOX + "| 0. Exit                                                             |");
            System.out.println(BOX + "-----------------------------------------------------------------------");

            System.out.print(INPUT + "Enter your choice: ");
            int choice = FastInput.readInt();

            if (choice == 0) {
                ConsoleUtil.clearScreen();
                BackgroundFiller.applyMainMenuTheme();

                System.out.println(EXIT_BOX + "-----------------------------------------------------------------------");
                System.out.println(EXIT_BOX + "| Exiting Dormatrix. Goodbye!                                         |");
                System.out.println(EXIT_BOX + "-----------------------------------------------------------------------");

                System.out.print(INPUT + "Press Enter 0 to close... ");
                FastInput.readNonEmptyLine();

                BackgroundFiller.resetTheme();
                System.exit(0);
            }

            System.out.print(INPUT + "Enter userID: ");
            MyString username = new MyString(FastInput.readNonEmptyLine());

            System.out.print(INPUT + "Enter password: ");
            MyString password = InputHelper.readPassword();

            controller.handleRoleInput(choice, username, password);
        }
    }
}
