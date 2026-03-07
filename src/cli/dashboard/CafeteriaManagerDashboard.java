package cli.dashboard;

import controllers.dashboard.CafeteriaDashboardController;
import utils.*;

public class CafeteriaManagerDashboard implements Dashboard {

    private final CafeteriaDashboardController mainController = new CafeteriaDashboardController();

    @Override
    public void show(String username) {
        while (true) {
            ConsoleUtil.clearScreen();
            BackgroundFiller.applyCafeteriaManagerTheme();
            renderHeader(username);
            renderMenuOptions();

            System.out.print("\n" + "Enter your choice: ");
            int choice = FastInput.readInt();

            if (choice == 0) {
                ConsoleUtil.clearScreen();
                renderLogoutBox();
                return;
            }
            mainController.handleAction(choice);

            System.out.print("\nPress Enter to continue...");
            FastInput.readLine();
        }
    }

    private void renderHeader(String username) {
        String nowLine = "Now: " + TimeManager.nowDate() + " " + TimeManager.nowTime()
                + " | Slot: " + TimeManager.getCurrentMealSlot()
                + " | Ramadan: " + TimeManager.isRamadanMode();
        System.out.println();
        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                     CAFETERIA MANAGER DASHBOARD                     ║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");

        String welcomeMessage = "Welcome, " + username;
        int totalWidth = 69;
        int paddingLeft = (totalWidth - welcomeMessage.length()) / 2;
        int paddingRight = totalWidth - welcomeMessage.length() - paddingLeft;

        System.out.printf("║%" + paddingLeft + "s%s%" + paddingRight + "s║%n", "", welcomeMessage, "");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-67s ║%n", nowLine);
        System.out.printf("║ %-67s ║%n", CafeteriaAsciiUI.renderSlotProgress(TimeManager.getCurrentMealSlot()));
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
    }

    private void renderMenuOptions() {
        System.out.println("║ [1] Update Weekly Menu                                              ║");
        System.out.println("║ [2] Schedule Special Event                                          ║");
        System.out.println("║ [3] Verify Student Token                                            ║");
        System.out.println("║ [4] Toggle Ramadan Mode                                             ║");
        System.out.println("║ [0] Logout                                                          ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
    }

    private void renderLogoutBox() {
        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         Logging Out....                             ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
    }
}
