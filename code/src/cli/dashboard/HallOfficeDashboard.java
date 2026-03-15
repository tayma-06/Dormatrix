package cli.dashboard;

import controllers.dashboard.HallOfficeDashboardController;
import utils.BackgroundFiller;

import static utils.TerminalUI.*;

public class HallOfficeDashboard implements Dashboard {

    private final HallOfficeDashboardController controller = new HallOfficeDashboardController();
    private boolean firstShow = true;

    private static final BackgroundFiller.Theme THEME = BackgroundFiller.HALL;

    private static final MenuItem[] MENU = {
            new MenuItem(1, "Update Student Hall Room Info"),
            new MenuItem(2, "View Student Complaints"),
            new MenuItem(3, "View Worker Schedule"),
            new MenuItem(4, "Handle Attendant Task"),
            new MenuItem(5, "Edit Profile"),
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