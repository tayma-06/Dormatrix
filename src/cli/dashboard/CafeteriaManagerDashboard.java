package cli.dashboard;

import controllers.dashboard.CafeteriaDashboardController;
import utils.*;
import static utils.TerminalUI.*;

public class CafeteriaManagerDashboard implements Dashboard {

    private final CafeteriaDashboardController mainController = new CafeteriaDashboardController();
    private boolean firstShow = true;

    private static final String BOX = ConsoleColors.fgRGB(255, 210, 30);   // bright yellow box
    private static final String TEXT = ConsoleColors.ThemeText.CAFETERIA_TEXT;
    private static final String BG = ConsoleColors.bgRGB(35, 28, 0);
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
                CafeteriaAsciiUI.stopBarAnimation();   // stop before every redraw
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
                drawDashboard(
                        "CAFETERIA MANAGER DASHBOARD",
                        "Welcome, " + username,
                        MENU, TEXT, BOX,
                        extraHeader,
                        menuStartRow
                );

                // Row layout: top(3) title(4) sep(5) welcome(6) nowLine(7) bar(8)
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
                CafeteriaAsciiUI.stopBarAnimation();
                cleanup();
                System.err.println("[CafeteriaManagerDashboard] " + e.getMessage());
            }
        }
    }
}
