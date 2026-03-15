package cli.dashboard;

import controllers.dashboard.StudentDashboardController;
import utils.*;
import utils.TerminalUI.MenuItem;

import static utils.TerminalUI.*;

public class StudentDashboard implements Dashboard {

    private final StudentDashboardController controller
            = new StudentDashboardController();
    private boolean firstShow = true;

    private static final BackgroundFiller.Theme THEME = BackgroundFiller.STUDENT;

    private static final MenuItem[] MENU = {
            new MenuItem(1, "View Room Info"),
            new MenuItem(2, "Facility Booking"),
            new MenuItem(3, "Meal Token Purchase"),
            new MenuItem(4, "Store Account & Dues"),
            new MenuItem(5, "Lost & Found"),
            new MenuItem(6, "Complaint Menu"),
            new MenuItem(7, "Weekly Routine"),
            new MenuItem(8, "View Announcements"),
            new MenuItem(9, "Store Shopping Cart"),
            new MenuItem(10, "Emergency Contacts"),
            new MenuItem(11, "Edit Profile"),
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
                        "STUDENT DASHBOARD",
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
                System.err.println("[StudentDashboard] " + e.getMessage());
            }
        }
    }
}