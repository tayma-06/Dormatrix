package cli.dashboard.room;

import cli.dashboard.Dashboard;
import controllers.dashboard.room.RoomDashboardController;
import utils.*;
import static utils.TerminalUI.*;

public class RoomDashboard implements Dashboard {

    private final RoomDashboardController controller;

    private static final MenuItem[] MENU = {
        new MenuItem(1, "Add New Room"),
        new MenuItem(2, "View Available Rooms"),
        new MenuItem(0, "Back"),};

    public RoomDashboard(RoomDashboardController controller) {
        this.controller = controller;
    }

    @Override
    public void show(String username) {
        while (true) {
            try {
                ConsoleUtil.clearScreen();
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                System.out.print(HIDE_CUR);

                int menuStartRow = 3;
                int promptRow = drawDashboard(
                        "MANAGE ROOMS",
                        "Room Management",
                        MENU,
                        TerminalUI.getActiveTextColor(),
                        TerminalUI.getActiveBoxColor(),
                        null,
                        menuStartRow
                );

                System.out.print(SHOW_CUR);
                int choice = FastInput.readInt();
                System.out.print(RESET);

                if (choice == 0) {
                    ConsoleUtil.clearScreen();
                    return;
                }

                controller.handleInput(choice);
                ConsoleUtil.pause();

            } catch (Exception e) {
                cleanup();
                System.err.println("[RoomDashboard] " + e.getMessage());
            }
        }
    }
}
