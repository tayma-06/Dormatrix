package cli.dashboard;

import controllers.dashboard.AdminDashboardController;
import utils.BackgroundFiller;
import utils.ConsoleColors;

import static utils.TerminalUI.*;

public class AdminDashboard implements Dashboard {

    private final AdminDashboardController controller = new AdminDashboardController();
    private boolean firstShow = true;

    private static final String BOX = ConsoleColors.fgRGB(255, 60, 60);
    private static final String TEXT = ConsoleColors.ThemeText.ADMIN_TEXT;
    private static final String BG = ConsoleColors.bgRGB(48, 0, 5);

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
                BackgroundFiller.applyAdminTheme();
                setActiveTheme(BOX, TEXT, BG);
                System.out.print(HIDE_CUR);

                drawDashboard(
                        "ADMIN DASHBOARD",
                        "Welcome, " + username,
                        MENU,
                        TEXT,
                        BOX,
                        new String[]{
                                "Use ↑ and ↓ to move",
                                "Press Enter to open"
                        },
                        3
                );

                int choice = readChoiceArrow();
                System.out.print(RESET);

                if (choice == 0) {
                    BackgroundFiller.applyAdminTheme();
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