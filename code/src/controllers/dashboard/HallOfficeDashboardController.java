package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.dashboard.room.RoomChangeRequestDashboard;
import cli.dashboard.room.RoomDashboard;
import cli.profile.EditProfileCLI;
import cli.views.complaint.ComplaintView;
import controllers.dashboard.room.RoomChangeRequestDashboardController;
import controllers.dashboard.room.RoomDashboardController;
import controllers.room.RoomController;
import controllers.room.RoomService;
import libraries.collections.MyArrayList;
import models.complaints.Complaint;
import repo.file.FileComplaintRepository;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

public class HallOfficeDashboardController {

    private final MainDashboard mainDashboard = new MainDashboard();
    private final RoomController roomController = new RoomController();
    private final ComplaintView complaintView = new ComplaintView();
    private final FileComplaintRepository complaintRepo = new FileComplaintRepository();
    private final RoomService roomService = new RoomService();
    private final RoomDashboard roomDashboard = new RoomDashboard(new RoomDashboardController(roomService));

    public void handleInput(int choice, String username) {
        switch (choice) {
            case 1:
                roomDashboard.showAddRoomOnly();
                break;
            case 2:
                roomDashboard.showAvailableRoomsOnly();
                break;
            case 3:
                roomDashboard.showAllRoomsOnly();
                break;
            case 4:
                assignRoomToUnassignedStudent();
                break;
            case 5:
                reviewRoomChangeApplications(username);
                break;
            case 6:
                new EditProfileCLI(username, "HALL_OFFICER").start();
                break;
            case 0:
                mainDashboard.show();
                break;
            default:
                TerminalUI.tPrint("Invalid Choice.");
        }
    }

    private void assignRoomToUnassignedStudent() {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.tPrompt("Enter Student ID or Name (0 to cancel): ");
        String targetId = FastInput.readLine().trim();

        if ("0".equals(targetId)) {
            return;
        }

        String resolvedId = roomService.resolveStudentId(targetId);
        if (resolvedId == null || resolvedId.trim().isEmpty()) {
            TerminalUI.tError("Student ID not found.");
            TerminalUI.tPause();
            return;
        }

        String studentName = roomService.getStudentName(resolvedId);
        String currentRoom = roomService.getStudentRoomNumber(resolvedId);

        if (!roomService.isStudentUnassigned(resolvedId)) {
            TerminalUI.tError("This student already has room " + currentRoom + ". Use room change applications for assigned students.");
            TerminalUI.tPause();
            return;
        }

        String chosenRoom = roomController.pickAvailableRoomInteractive();
        if (chosenRoom == null || chosenRoom.trim().isEmpty()) {
            return;
        }

        boolean ok = roomService.changeStudentRoom(resolvedId, chosenRoom);
        if (ok) {
            TerminalUI.tSuccess("Assigned room " + chosenRoom + " to " + (studentName == null ? resolvedId : studentName) + ".");
        } else {
            TerminalUI.tError("Could not assign the selected room. Please check room availability again.");
        }
        TerminalUI.tPause();
    }

    private void reviewRoomChangeApplications(String officerName) {
        RoomChangeRequestDashboardController requestController = new RoomChangeRequestDashboardController();
        if (requestController.getPendingApplications().size() == 0) {
            TerminalUI.tError("There is no room change application to review right now.");
            TerminalUI.tPause();
            return;
        }

        new RoomChangeRequestDashboard(requestController).show(officerName);
    }
}