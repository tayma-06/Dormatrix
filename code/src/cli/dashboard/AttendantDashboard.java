package cli.dashboard;

import cli.complaint.AttendantComplaintCLI;
import controllers.dashboard.AttendantDashboardController;
import utils.*;
import utils.TerminalUI.MenuItem;

import static utils.TerminalUI.*;

public class AttendantDashboard implements Dashboard {

    private final AttendantDashboardController controller = new AttendantDashboardController();
    private boolean firstShow = true;

    private static final BackgroundFiller.Theme THEME = BackgroundFiller.ATTENDANT;

    private static final MenuItem[] MENU = {
            new MenuItem(1, "Handle Student Complaints"),
            new MenuItem(2, "Handle Worker Schedule"),
            new MenuItem(3, "Add Found Items"),
            new MenuItem(4, "View Student Routine"),
            new MenuItem(5, "Announcements"),
            new MenuItem(6, "Manage Emergency Contacts"),
            new MenuItem(7, "Edit Profile"),
            new MenuItem(0, "Logout")
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
                        "ATTENDANT DASHBOARD",
                        "Welcome, " + username,
                        MENU,
                        THEME.text(),
                        THEME.box(),
                        null,
                        menuStartRow
                );

                int choice = readChoiceArrow();

                if (choice == 0) {
                    BackgroundFiller.applyAttendantTheme();
                    showLogout();
                    BackgroundFiller.resetTheme();
                    return;
                }

                if (choice == 1) {
                    new AttendantComplaintCLI().start();
                    continue;
                }

                //  clear before any sub-screen
                ConsoleUtil.clearScreen();
                BackgroundFiller.applyAttendantTheme();
                TerminalUI.setActiveTheme(
                        ConsoleColors.fgRGB(40, 220, 210),
                        ConsoleColors.ThemeText.ATTENDANT_TEXT,
                        ConsoleColors.bgRGB(0, 28, 26)
                );
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                TerminalUI.at(2, 1);

                controller.handleInput(choice, username);

            } catch (Exception e) {
                cleanup();
                System.err.println("[AttendantDashboard] " + e.getMessage());
            }
        }
    }
}