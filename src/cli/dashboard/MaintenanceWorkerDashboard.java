package cli.dashboard;

import cli.complaint.WorkerComplaintCLI;
import controllers.dashboard.MaintenanceWorkerDashboardController;
import utils.*;
import static utils.TerminalUI.*;

public class MaintenanceWorkerDashboard implements Dashboard {

    private final MaintenanceWorkerDashboardController controller
            = new MaintenanceWorkerDashboardController();
    private boolean firstShow = true;

    private static final String BOX = ConsoleColors.fgRGB(60, 230, 100);   // neon green box
    private static final String TEXT = ConsoleColors.ThemeText.MAINTENANCE_TEXT;
    private static final String BG = ConsoleColors.bgRGB(0, 25, 8);
    private static final String MUTED = ConsoleColors.Accent.MUTED;

    private static final MenuItem[] MENU = {
        new MenuItem(1, "Work Field"),
        new MenuItem(2, "Task Queue"),
        new MenuItem(3, "Weekly Schedule"),
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
                BackgroundFiller.applyMaintenanceTheme();
                setActiveTheme(BOX, TEXT, BG);
                System.out.print(HIDE_CUR);

                int menuStartRow = 3;
                drawDashboard(
                        "MAINTENANCE WORKER DASHBOARD",
                        "Welcome, " + username,
                        MENU, TEXT, BOX,
                        null,
                        menuStartRow
                );

                int choice = readChoiceArrow();
                System.out.print(RESET);

                if (choice == 0) {
                    BackgroundFiller.applyMaintenanceTheme();
                    showLogout();
                    BackgroundFiller.resetTheme();
                    return;
                }

                if (choice == 1) {
                    String workerField = controller.getWorkerField(username);
                    System.out.println();
                    drawDashboardHeader("YOUR WORK FIELD", "", BOX, TEXT, BG);
                    drawMenuItem("Work Field: " + workerField, BOX, TEXT, BG);
                    drawBoxBottom(BOX, BG);
                    System.out.println();
                    drawInputPrompt("Press Enter to continue...", TEXT, BG);
                    FastInput.readLine();
                    continue;
                }

                if (choice == 2) {
                    new WorkerComplaintCLI().start(username);
                    continue;
                }

                controller.handleInput(choice, username);

            } catch (Exception e) {
                cleanup();
                System.err.println("[MaintenanceWorkerDashboard] " + e.getMessage());
            }
        }
    }
}
