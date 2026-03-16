package cli.forms.room;

import controllers.dashboard.room.StudentRoomDashboardController;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

public class RoomChangeApplicationForm {

    private final StudentRoomDashboardController controller;

    public RoomChangeApplicationForm(StudentRoomDashboardController controller) {
        this.controller = controller;
    }

    public void show(String studentIdentifier, String currentRoom) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());

        if (currentRoom == null || currentRoom.equals("UNASSIGNED") || currentRoom.equals("N/A")) {
            TerminalUI.tError("You do not have an assigned room yet.");
            TerminalUI.tPause();
            return;
        }

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("ROOM CHANGE APPLICATION");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Current Room : " + currentRoom);
        TerminalUI.tBoxLine("Enter 0 at any prompt to cancel.");
        TerminalUI.tBoxBottom();

        TerminalUI.tEmpty();
        TerminalUI.tPrompt("Requested Room: ");
        String requestedRoom = FastInput.readLine().trim();
        if ("0".equals(requestedRoom)) {
            return;
        }

        TerminalUI.tPrompt("Reason: ");
        String reason = FastInput.readLine().trim();
        if ("0".equals(reason)) {
            return;
        }

        String result = controller.submitRoomChangeApplication(studentIdentifier, requestedRoom, reason);

        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());

        if (result.toLowerCase().contains("successfully")) {
            TerminalUI.tSuccess(result);
        } else {
            TerminalUI.tError(result);
        }
        TerminalUI.tPause();
    }
}