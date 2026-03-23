package cli.dashboard;

import controllers.dashboard.HallOfficeDashboardController;
import utils.BackgroundFiller;

import static utils.TerminalUI.*;

public class HallOfficeDashboard implements Dashboard {

    private final HallOfficeDashboardController controller = new HallOfficeDashboardController();
    private boolean firstShow = true;

    private static final BackgroundFiller.Theme THEME = BackgroundFiller.HALL;

    private static final MenuItem[] MENU = {
            new MenuItem(1, "Add New Room"),
            new MenuItem(2, "View Available Rooms"),
            new MenuItem(3, "Browse All Rooms"),
            new MenuItem(4, "Assign Room To Unassigned Student"),
            new MenuItem(5, "Review Room Change Applications"),
            new MenuItem(6, "Edit Profile"),
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
                        "HALL OFFICE DASHBOARD",
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
                System.err.println("[HallOfficeDashboard] " + e.getMessage());
            }
        }
    }
}