package cli.dashboard;

import controllers.dashboard.MaintenanceWorkerDashboardController;
import cli.complaint.WorkerComplaintCLI;
import utils.BackgroundFiller;
import utils.FastInput;
import utils.ConsoleUtil;

public class MaintenanceWorkerDashboard implements Dashboard {

    private final MaintenanceWorkerDashboardController controller =
            new MaintenanceWorkerDashboardController();

    @Override
    public void show(String username) {
        while (true) {
            ConsoleUtil.clearScreen();
            BackgroundFiller.applyMaintenanceTheme();
            System.out.println();
            System.out.println("-----------------------------------------------------------------");
            System.out.println("|                 MAINTENANCE WORKER DASHBOARD                  |");
            System.out.println("-----------------------------------------------------------------");
            System.out.println("  Welcome, " + username);
            System.out.println("-----------------------------------------------------------------");
            System.out.println("| 1. Work Field                                                 |");
            System.out.println("| 2. View Task Queue                                            |");
            System.out.println("| 3. Updated Student Complain Status                            |");
            System.out.println("| 0. Logout                                                     |");
            System.out.println("-----------------------------------------------------------------");
            System.out.print("Enter your choice: ");

            int choice = FastInput.readInt();

            if (choice == 0) {
                System.out.println("-----------------------------------------------------------------------");
                System.out.println("| Logging Out....                                                     |");
                System.out.println("-----------------------------------------------------------------------");
                BackgroundFiller.resetTheme();
                return;
            }

            if (choice == 2 || choice == 3) {
                new WorkerComplaintCLI().start(username);
                continue;
            }

            controller.handleInput(choice, username);
        }
    }
}
