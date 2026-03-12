package cli.dashboard;

import controllers.dashboard.CafeteriaDashboardController;
import utils.*;

import static utils.TerminalUI.*;

public class CafeteriaManagerDashboard implements Dashboard {

    private final CafeteriaDashboardController mainController = new CafeteriaDashboardController();
    private boolean firstShow = true;

    private static final BackgroundFiller.Theme THEME = BackgroundFiller.CAFETERIA;

    private static final MenuItem[] MENU = {
            new MenuItem(1, "Update Weekly Menu"),
            new MenuItem(2, "Schedule Special Event"),
            new MenuItem(3, "Verify Student Token"),
            new MenuItem(4, "Toggle Ramadan Mode"),
            new MenuItem(0, "Logout"),
    };

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
                CafeteriaAsciiUI.stopBarAnimation();
                BackgroundFiller.applyTheme(THEME);
                setActiveTheme(
                        THEME.box(),
                        THEME.text(),
                        THEME.canvasBg(),
                        THEME.panelBg(),
                        THEME.inputBg()
                );
                System.out.print(HIDE_CUR);

                String nowLine = "Now: " + TimeManager.nowDate() + " " + TimeManager.nowTime()
                        + " | Slot: " + TimeManager.getCurrentMealSlot()
                        + " | Ramadan: " + TimeManager.isRamadanMode();

                String[] extraHeader = {
                        nowLine,
                        CafeteriaAsciiUI.renderSlotProgress(TimeManager.getCurrentMealSlot())
                };

                int menuStartRow = 3;
                drawDashboard(
                        "CAFETERIA MANAGER DASHBOARD",
                        "Welcome, " + username,
                        MENU,
                        THEME.text(),
                        THEME.box(),
                        extraHeader,
                        menuStartRow
                );

                int nowRow = menuStartRow + 4;
                int barRow = menuStartRow + 5;
                CafeteriaAsciiUI.startBarAnimation(
                        barRow, nowRow, boxCol(), innerW(),
                        TimeManager.getCurrentMealSlot()
                );

                int choice = readChoiceArrow();
                CafeteriaAsciiUI.stopBarAnimation();
                System.out.print(RESET);

                if (choice == 0) {
                    BackgroundFiller.applyTheme(THEME);
                    showLogout();
                    BackgroundFiller.resetTheme();
                    return;
                }

                mainController.handleAction(choice);

                System.out.println();
                drawInputPrompt("Press Enter to continue...", THEME.text(), THEME.canvasBg());
                FastInput.readLine();

            } catch (Exception e) {
                CafeteriaAsciiUI.stopBarAnimation();
                cleanup();
                System.err.println("[CafeteriaManagerDashboard] " + e.getMessage());
            }
        }
    }
}