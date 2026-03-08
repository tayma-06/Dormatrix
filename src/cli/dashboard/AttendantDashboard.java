package cli.dashboard;

import cli.complaint.AttendantComplaintCLI;
import controllers.dashboard.AttendantDashboardController;
import utils.*;
import static utils.TerminalUI.*;

public class AttendantDashboard implements Dashboard {

    private final AttendantDashboardController controller = new AttendantDashboardController();
    private boolean firstShow = true;

    private static final String BOX = ConsoleColors.fgRGB(40, 220, 210);   // bright cyan box
    private static final String TEXT = ConsoleColors.ThemeText.ATTENDANT_TEXT;
    private static final String BG = ConsoleColors.bgRGB(0, 28, 26);
    private static final String MUTED = ConsoleColors.Accent.MUTED;

    private static final MenuItem[] MENU = {
        new MenuItem(1, "Handle Student Complaints"),
        new MenuItem(2, "Handle Worker Schedule"),
        new MenuItem(3, "Add Found Items"),
        new MenuItem(4, "View Student Routine"),
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
                BackgroundFiller.applyAttendantTheme();
                setActiveTheme(BOX, TEXT, BG);
                System.out.print(HIDE_CUR);

                int menuStartRow = 3;
                int promptRow = drawDashboard(
                        "ATTENDANT DASHBOARD",
                        "Welcome, " + username,
                        MENU, TEXT, BOX,
                        null,
                        menuStartRow
                );

                System.out.print(SHOW_CUR);
                int choice = FastInput.readInt();
                System.out.print(RESET);

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

                controller.handleInput(choice, username);

            } catch (Exception e) {
                cleanup();
                System.err.println("[AttendantDashboard] " + e.getMessage());
            }
        }
    }
}
