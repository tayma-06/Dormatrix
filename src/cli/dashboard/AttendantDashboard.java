package cli.dashboard;

import cli.complaint.AttendantComplaintCLI;
import controllers.dashboard.AttendantDashboardController;
import utils.*;
import utils.TerminalUI.MenuItem;

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
        new MenuItem(5, "Announcements"),
        new MenuItem(6, "Manage Emergency Contacts"),
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
                ConsoleUtil.clearScreen();
                BackgroundFiller.applyAttendantTheme();
                System.out.println();
                System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
                System.out.println("║                        ATTENDANT DASHBOARD                          ║");
                System.out.println("╠═════════════════════════════════════════════════════════════════════╣");

                String welcomeMessage = "Welcome, " + username;
                int totalWidth = 69;
                int paddingLeft = (totalWidth - welcomeMessage.length()) / 2;
                int paddingRight = totalWidth - welcomeMessage.length() - paddingLeft;
                String formattedWelcome
                        = String.format("║%" + paddingLeft + "s%s%" + paddingRight + "s║", "", welcomeMessage, "");
                System.out.println(formattedWelcome);

                System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
                System.out.println("║ [1] Handle Student Complaints                                       ║");
                System.out.println("║ [2] Handle Worker Schedule                                          ║");
                System.out.println("║ [3] Add Found Items                                                 ║");
                System.out.println("║ [4] View Student Routine                                            ║");
                System.out.println("║ [5] Announcements                                                   ║");
                System.out.println("║ [6] Manage Emergency Contacts                                       ║");
                System.out.println("║ [0] Logout                                                          ║");
                System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

                System.out.println();
                System.out.print("Enter your choice: ");
                int choice = FastInput.readInt();

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
