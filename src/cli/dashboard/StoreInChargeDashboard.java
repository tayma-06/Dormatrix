package cli.dashboard;

import controllers.dashboard.StoreInChargeDashboardController;
import utils.*;

import static utils.TerminalUI.*;

public class StoreInChargeDashboard implements Dashboard {

    private final StoreInChargeDashboardController controller
            = new StoreInChargeDashboardController();
    private boolean firstShow = true;

    private static final BackgroundFiller.Theme THEME = BackgroundFiller.STORE;

    private static final MenuItem[] MENU = {
            new MenuItem(1, "Inventory Management"),
            new MenuItem(2, "Process Purchase"),
            new MenuItem(3, "Sales Summary & Reports"),
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
                BackgroundFiller.applyTheme(THEME);
                setActiveTheme(
                        THEME.box(),
                        THEME.text(),
                        THEME.canvasBg(),
                        THEME.panelBg(),
                        THEME.inputBg()
                );
                System.out.print(HIDE_CUR);

                int menuStartRow = 3;
                drawDashboard(
                        "STORE-IN-CHARGE DASHBOARD",
                        "Welcome, " + username,
                        MENU,
                        THEME.text(),
                        THEME.box(),
                        null,
                        menuStartRow
                );

                int choice = readChoiceArrow();
                System.out.print(RESET);

                if (choice == 0) {
                    BackgroundFiller.applyTheme(THEME);
                    showLogout();
                    BackgroundFiller.resetTheme();
                    return;
                }

                controller.handleInput(choice, username);

            } catch (Exception e) {
                cleanup();
                System.err.println("[StoreInChargeDashboard] " + e.getMessage());
            }
        }
    }
}