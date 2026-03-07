package cli.dashboard.room;

import cli.dashboard.Dashboard;
import controllers.dashboard.room.RoomDashboardController;
import utils.ConsoleUtil;
import utils.FastInput;

public class RoomDashboard implements Dashboard {

    private final RoomDashboardController controller;

    public RoomDashboard(RoomDashboardController controller) {
        this.controller = controller;
    }

    @Override
    public void show(String username) {
        while (true) {
            ConsoleUtil.clearScreen();

            System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                         MANAGE ROOMS DASHBOARD                      ║");
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ [1] Add New Room                                                    ║");
            System.out.println("║ [2] View Available Rooms                                            ║");
            System.out.println("║ [0] Back                                                            ║");
            System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.print("Enter choice: ");

            int choice = FastInput.readInt();
            if (choice == 0) {
                ConsoleUtil.clearScreen();
                return;
            }

            controller.handleInput(choice);

            ConsoleUtil.pause();
        }
    }
}
