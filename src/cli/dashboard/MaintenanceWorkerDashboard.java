package cli.dashboard;

import cli.complaint.WorkerComplaintCLI;
import controllers.dashboard.MaintenanceWorkerDashboardController;
import utils.BackgroundFiller;
import utils.ConsoleUtil;
import utils.FastInput;

public class MaintenanceWorkerDashboard implements Dashboard {

    private final MaintenanceWorkerDashboardController controller
            = new MaintenanceWorkerDashboardController();

    @Override
    public void show(String username) {
        while (true) {
            ConsoleUtil.clearScreen();
            BackgroundFiller.applyMaintenanceTheme();
            System.out.println();
            System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                   MAINTENANCE WORKER DASHBOARD                      ║");
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");

            String welcomeMessage = "Welcome, " + username;
            int totalWidth = 69;
            int paddingLeft = (totalWidth - welcomeMessage.length()) / 2;
            int paddingRight = totalWidth - welcomeMessage.length() - paddingLeft;
            String formattedWelcome
                    = String.format("║%" + paddingLeft + "s%s%" + paddingRight + "s║", "", welcomeMessage, "");
            System.out.println(formattedWelcome);

            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ [1] Work Field                                                      ║");
            System.out.println("║ [2] Task Queue                                                      ║");
            System.out.println("║ [3] My Visit Schedule                                               ║");
            System.out.println("║ [0] Logout                                                          ║");
            System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

            System.out.println();
            System.out.print("Enter your choice: ");

            int choice = FastInput.readInt();

            if (choice == 0) {
                ConsoleUtil.clearScreen();
                System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
                System.out.println("║                         Logging Out....                             ║");
                System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
                BackgroundFiller.resetTheme();
                return;
            }

            if (choice == 1) {
                // Fetch the worker's field from the controller
                String workerField = controller.getWorkerField(username);
                System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
                System.out.println("║                         YOUR WORK FIELD                             ║");
                System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
                System.out.println(String.format("║ %-67s ║", "Work Field: " + workerField));
                System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
                continue;
            }

            if (choice == 2) {
                new WorkerComplaintCLI().start(username);
                continue;
            }

            if (choice == 3) {
                new cli.schedule.WorkerVisitBoardCLI().show(username);
                continue;
            }

            controller.handleInput(choice, username);
        }
    }
}
