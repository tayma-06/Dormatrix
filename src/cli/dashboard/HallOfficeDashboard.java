package cli.dashboard;

import controllers.dashboard.HallOfficeDashboardController;
import cli.announcement.AnnouncementBoardCLI;
import utils.BackgroundFiller;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.*;
import static utils.TerminalUI.*;

public class HallOfficeDashboard implements Dashboard {

    private final HallOfficeDashboardController controller = new HallOfficeDashboardController();
    private boolean firstShow = true;

    private static final String BOX = ConsoleColors.fgRGB(255, 80, 190);   // hot pink box
    private static final String TEXT = ConsoleColors.ThemeText.HALL_TEXT;
    private static final String BG = ConsoleColors.bgRGB(35, 0, 25);
    private static final String MUTED = ConsoleColors.Accent.MUTED;

    private static final MenuItem[] MENU = {
        new MenuItem(1, "Update Student Hall Room Info"),
        new MenuItem(2, "View Student Complaints"),
        new MenuItem(3, "View Worker Schedule"),
        new MenuItem(4, "Handle Attendant Task"),
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
                BackgroundFiller.applyHallOfficeTheme();
                setActiveTheme(BOX, TEXT, BG);
                System.out.print(HIDE_CUR);

                int menuStartRow = 3;
                drawDashboard(
                        "HALL OFFICE DASHBOARD",
                        "Welcome, " + username,
                        MENU, TEXT, BOX,
                        null,
                        menuStartRow
                );

                int choice = readChoiceArrow();
                System.out.print(RESET);

                if (choice == 0) {
                    BackgroundFiller.applyHallOfficeTheme();
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
