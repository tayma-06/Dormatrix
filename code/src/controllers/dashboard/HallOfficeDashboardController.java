package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.dashboard.room.AvailableRoomPreviewDashboard;
import cli.dashboard.room.RoomChangeRequestDashboard;
import cli.dashboard.room.StudentRoomDashboard;
import cli.profile.EditProfileCLI;
import cli.views.complaint.ComplaintView;
import controllers.dashboard.room.RoomChangeRequestDashboardController;
import controllers.dashboard.room.StudentRoomDashboardController;
import controllers.room.RoomController;
import controllers.room.RoomService;
import java.util.*;
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

    public void handleInput(int choice, String username) {
        switch (choice) {
            case 1:
                handleRoomAllocation(username);
                break;
            case 2:
                handleComplaintManagement();
                break;
            case 3:
                TerminalUI.tPrint(">> Feature [Worker Schedule] is under development.");
                break;
            case 4:
                TerminalUI.tPrint(">> Feature [Attendant Task] is under development.");
                break;
            case 5:
                new EditProfileCLI(username, "HALL_OFFICER").start();
                break;
            case 0:
                mainDashboard.show();
                break;
            default:
                TerminalUI.tPrint("Invalid Choice.");
        }
    }

    private void handleRoomAllocation(String officerName) {
        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);
            TerminalUI.tSubDashboard("ROOM MANAGEMENT MENU", new String[]{
                    "[1] Live Preview Available Rooms",
                    "[2] Allocate / Change Student Room",
                    "[3] Review Room Change Applications",
                    "[0] Back"
            });

            int subChoice = FastInput.readInt();

            if (subChoice == 1) {
                new AvailableRoomPreviewDashboard(roomService).show();
            } else if (subChoice == 2) {
                updateStudentRoom();
                TerminalUI.tPause();
            } else if (subChoice == 3) {
                new RoomChangeRequestDashboard(
                        new RoomChangeRequestDashboardController()
                ).show(officerName);
            } else if (subChoice == 0) {
                ConsoleUtil.clearScreen();
                return;
            } else {
                TerminalUI.tPrint("Invalid sub-choice.");
                TerminalUI.tPause();
            }
        }
    }

    private void updateStudentRoom() {
        TerminalUI.tPrompt("Enter Student ID: ");
        String targetId = FastInput.readLine().trim();

        String resolvedId = roomService.resolveStudentId(targetId);
        if (resolvedId == null || resolvedId.trim().isEmpty()) {
            System.out.println("Student ID not found.");
            return;
        }

        String studentName = roomService.getStudentName(resolvedId);
        String oldRoom = roomService.getStudentRoomNumber(resolvedId);

        System.out.println("Student found: " + (studentName == null ? resolvedId : studentName));
        System.out.println("Current Room : " + oldRoom);

        TerminalUI.tPrompt("Enter New Room Number (or 0 to cancel): ");
        String newRoom = FastInput.readLine().trim();

        if ("0".equals(newRoom)) {
            return;
        }

        boolean ok = roomService.changeStudentRoom(resolvedId, newRoom);
        if (ok) {
            System.out.println("SUCCESS: Room updated to " + newRoom);
        } else {
            System.out.println("ERROR: Could not update room. Check room existence / availability.");
        }
    }

    private void handleComplaintManagement() {
        while (true) {
            ConsoleUtil.clearScreen();
            complaintView.attendantMenu();
            int choice = FastInput.readInt();

            switch (choice) {
                case 1:
                    MyArrayList<Complaint> all = complaintRepo.findAll();
                    complaintView.attendantList(all);
                    ConsoleUtil.pause();
                    break;
                case 2:
                    System.out.println(">> Filtering Pending... (To be implemented fully)");
                    complaintView.attendantList(complaintRepo.findAll());
                    ConsoleUtil.pause();
                    break;
                case 0:
                    ConsoleUtil.clearScreen();
                    return;
                default:
                    TerminalUI.tPrint("Option " + choice + " is coming soon.");
                    ConsoleUtil.pause();
            }
        }
    }
}