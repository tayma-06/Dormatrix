package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.dashboard.room.StudentRoomDashboard;
import cli.views.complaint.ComplaintView;
import controllers.dashboard.room.StudentRoomDashboardController;
import controllers.room.RoomController;
import controllers.room.RoomService;
import java.io.*;
import java.util.*;
import libraries.collections.MyArrayList;
import models.complaints.Complaint;
import repo.file.FileComplaintRepository;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;


public class HallOfficeDashboardController {

    private final String STUDENT_FILE = "data/users/students.txt";

    private final MainDashboard mainDashboard = new MainDashboard();
    private final RoomController roomController = new RoomController();
    private final ComplaintView complaintView = new ComplaintView();
    private final FileComplaintRepository complaintRepo = new FileComplaintRepository();

    public void handleInput(int choice, String username) {
        switch (choice) {
            case 1:
                handleRoomAllocation();
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
            case 0:
                mainDashboard.show();
                break;
            default:
                TerminalUI.tPrint("Invalid Choice.");
        }
    }

    private void handleRoomAllocation() {
        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);
            TerminalUI.tSubDashboard("ROOM MANAGEMENT MENU", new String[]{
                "[1] View All Rooms status",
                "[2] Allocate/Change Student Room",
                "[0] Back"
            });

            int subChoice = FastInput.readInt();

                if (subChoice == 1) {
                    for (models.room.Room r : roomController.getAllRooms()) {
                        TerminalUI.tBoxLine(r.toString());
                    }
                    TerminalUI.tPause();
                } else if (subChoice == 2) {
                    updateStudentRoom();
                    TerminalUI.tPause();
                } else if (subChoice == 0) {
                    ConsoleUtil.clearScreen();
                    return;
                }
            }
        }
    }

    private void updateStudentRoom() {
        TerminalUI.tPrompt("Enter Student ID: ");
        String targetId = FastInput.readLine().trim();

        List<String> lines = new ArrayList<>();
        boolean found = false;
        String oldRoom = "UNASSIGNED";
        String currentLine = null;

        File file = new File(STUDENT_FILE);
        if (!file.exists()) {
            System.out.println("Error: Student database missing.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);

                if (parts.length > 0) {
                    String fileId = parts[0].trim().replace("\uFEFF", "");

                    if (fileId.equals(targetId)) {
                        found = true;
                        currentLine = line;
                        if (parts.length > 7 && !parts[7].trim().isEmpty()) {
                            oldRoom = parts[7].trim();
                        }
                    } else {
                        lines.add(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (!found) {
            System.out.println("Student ID [" + targetId + "] not found.");
            return;
        }

        System.out.println("Student found. Current Room: " + oldRoom);
        roomController.showAvailableRooms();

        TerminalUI.tPrompt("Enter New Room Number (or type '0' to cancel): ");
        String newRoom = FastInput.readLine().trim();

        if (newRoom.equals("0")) {
            return;
        }

        if (roomController.allocateRoom(newRoom)) {
            if (!oldRoom.equals("UNASSIGNED") && !oldRoom.equals("N/A")) {
                roomController.freeRoom(oldRoom);
            }

            String[] parts = currentLine.split("\\|", -1);
            StringBuilder newLine = new StringBuilder();

            for (int i = 0; i < 8; i++) {
                if (i == 7) {
                    newLine.append(newRoom);
                } else if (i < parts.length) {
                    newLine.append(parts[i]);
                } else {
                    newLine.append("");
                }

                if (i < 7) {
                    newLine.append("|");
                }
            }

            lines.add(newLine.toString());

            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                for (String l : lines) {
                    pw.println(l);
                }
            } catch (IOException e) {
                System.out.println("Error saving student data.");
            }

            System.out.println("SUCCESS: Room updated to " + newRoom);
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
                    case 7:
                        TerminalUI.tPrompt("Enter Room ID: ");
                        String rid = FastInput.readLine().trim();
                        new StudentRoomDashboard(new StudentRoomDashboardController(new RoomService())).showComplaints(rid);
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
}
