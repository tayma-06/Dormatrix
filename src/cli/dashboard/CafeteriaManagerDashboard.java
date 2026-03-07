package cli.dashboard;

import controllers.dashboard.CafeteriaDashboardController;
import utils.*;
import static utils.TerminalUI.*;

public class CafeteriaManagerDashboard implements Dashboard {

    private final CafeteriaDashboardController mainController = new CafeteriaDashboardController();
    private boolean firstShow = true;

    private static final String BOX = ConsoleColors.Accent.BOX;
    private static final String TEXT = ConsoleColors.ThemeText.CAFETERIA_TEXT;
    private static final String BG = ConsoleColors.bgRGB(45, 25, 10);
    private static final String MUTED = ConsoleColors.Accent.MUTED;

    private static final MenuItem[] MENU = {
        new MenuItem(1, "Update Weekly Menu"),
        new MenuItem(2, "Schedule Special Event"),
        new MenuItem(3, "Verify Student Token"),
        new MenuItem(4, "Toggle Ramadan Mode"),
        new MenuItem(0, "Logout"),};

    @Override
    public void show(String username) {
        if (firstShow) {
            try {
                quickMatrixRain();
            } catch (InterruptedException ignored) {
            }
            firstShow = false;
        }

        while (true) {
            try {
                BackgroundFiller.applyCafeteriaManagerTheme();
                setActiveTheme(BOX, TEXT, BG);
                System.out.print(HIDE_CUR);

                String nowLine = "Now: " + TimeManager.nowDate() + " " + TimeManager.nowTime()
                        + " | Slot: " + TimeManager.getCurrentMealSlot()
                        + " | Ramadan: " + TimeManager.isRamadanMode();
                String[] extraHeader = {
                    nowLine,
                    CafeteriaAsciiUI.renderSlotProgress(TimeManager.getCurrentMealSlot())
                };

                int menuStartRow = 3;
                int promptRow = drawDashboard(
                        "CAFETERIA MANAGER DASHBOARD",
                        "Welcome, " + username,
                        MENU, TEXT, BOX,
                        extraHeader,
                        menuStartRow
                );

                System.out.print(SHOW_CUR);
                int choice = FastInput.readInt();
                System.out.print(RESET);

                if (choice == 0) {
                    BackgroundFiller.applyCafeteriaManagerTheme();
                    showLogout();
                    BackgroundFiller.resetTheme();
                    return;
                }

                mainController.handleAction(choice);

                System.out.println();
                drawInputPrompt("Press Enter to continue...", TEXT, BG);
                FastInput.readLine();

            } catch (Exception e) {
                cleanup();
                System.err.println("[CafeteriaManagerDashboard] " + e.getMessage());
            }
        }
    }
}
