package cli.dashboard;

import controllers.dashboard.AdminDashboardController;
import utils.BackgroundFiller;

import static utils.TerminalUI.*;

public class AdminDashboard implements Dashboard {

    private final AdminDashboardController controller = new AdminDashboardController();
    private boolean firstShow = true;

    private static final BackgroundFiller.Theme THEME = BackgroundFiller.ADMIN;

    private static final MenuItem[] MENU = {
            new MenuItem(1, "Create Account"),
            new MenuItem(2, "Delete Account"),
            new MenuItem(3, "View & Search Accounts"),
            new MenuItem(4, "Manage Rooms"),
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
                        "ADMIN DASHBOARD",
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
                System.err.println("[AdminDashboard] " + e.getMessage());
                return;
            }
        }
    }
}