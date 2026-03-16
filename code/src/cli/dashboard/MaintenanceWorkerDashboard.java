package cli.dashboard;

import controllers.dashboard.MaintenanceWorkerDashboardController;
import utils.BackgroundFiller;

import static utils.TerminalUI.*;

public class MaintenanceWorkerDashboard implements Dashboard {

    private final MaintenanceWorkerDashboardController controller =
            new MaintenanceWorkerDashboardController();
    private boolean firstShow = true;

    private static final BackgroundFiller.Theme THEME = BackgroundFiller.MAINTENANCE;

    private static final MenuItem[] MENU = {
            new MenuItem(1, "View Work Field"),
            new MenuItem(2, "View Assigned Tasks"),
            new MenuItem(3, "View Routine"),
            new MenuItem(4, "View Profile"),
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

                drawDashboard(
                        "MAINTENANCE WORKER DASHBOARD",
                        "Welcome, " + username,
                        MENU,
                        THEME.text(),
                        THEME.box(),
                        null,
                        3
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
                System.err.println("[MaintenanceWorkerDashboard] " + e.getMessage());
            }
        }
    }
}