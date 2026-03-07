package cli.dashboard;

import controllers.dashboard.StudentDashboardController;
import utils.*;
import static utils.TerminalUI.*;

public class StudentDashboard implements Dashboard {

    private final StudentDashboardController controller
            = new StudentDashboardController();
    private boolean firstShow = true;

    private static final String BOX = ConsoleColors.Accent.BOX;
    private static final String TEXT = ConsoleColors.ThemeText.STUDENT_TEXT;
    private static final String BG = ConsoleColors.bgRGB(0, 4, 53);
    private static final String MUTED = ConsoleColors.Accent.MUTED;

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
                BackgroundFiller.applyStudentTheme();
                setActiveTheme(BOX, TEXT, BG);
                System.out.print(HIDE_CUR);

                int menuStartRow = 3;
                int promptRow = drawDashboard(
                        "STUDENT DASHBOARD",
                        "Welcome, " + username,
                        MENU, TEXT, BOX,
                        null,
                        menuStartRow
                );

                System.out.print(SHOW_CUR);
                int choice = FastInput.readInt();
                System.out.print(RESET);

                if (choice == 0) {
                    BackgroundFiller.applyStudentTheme();
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
