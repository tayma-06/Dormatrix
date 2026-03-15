package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.dashboard.room.StudentRoomDashboard;
import cli.profile.EditProfileCLI;
import cli.views.complaint.ComplaintView;
import controllers.dashboard.room.StudentRoomDashboardController;
import controllers.room.RoomController;
import controllers.room.RoomService;
import libraries.collections.MyArrayList;
import models.complaints.Complaint;
import models.room.Room;
import repo.file.FileComplaintRepository;
import utils.ConsoleColors;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

    private void handleRoomAllocation() {
        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);
            TerminalUI.tSubDashboard("ROOM MANAGEMENT MENU", new String[]{
                    "[1] View Available Rooms",
                    "[2] Allocate / Change Student Room",
                    "[0] Back"
            });

            int subChoice = FastInput.readInt();

            if (subChoice == 1) {
                roomController.showAvailableRooms();
            } else if (subChoice == 2) {
                updateStudentRoom();
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
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("ALLOCATE / CHANGE STUDENT ROOM");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Enter Student ID to continue.");
        TerminalUI.tBoxSep();
        TerminalUI.tPrompt("Student ID: ");

        String targetId = FastInput.readLine().trim();
        if (targetId.isEmpty()) {
            TerminalUI.tError("Student ID cannot be empty.");
            TerminalUI.tPause();
            return;
        }

        StudentRecord student = findStudent(targetId);
        if (student == null) {
            TerminalUI.tError("Student ID [" + targetId + "] not found.");
            TerminalUI.tPause();
            return;
        }

        String selectedRoom = roomController.pickAvailableRoomInteractive();
        if (selectedRoom == null || selectedRoom.trim().isEmpty()) {
            return;
        }

        Room previewRoom = roomController.getRoomWithRealOccupancy(selectedRoom);
        showAllocationPreview(student, previewRoom);

        String confirm = FastInput.readLine().trim().toLowerCase();
        if (!confirm.equals("y") && !confirm.equals("yes")) {
            TerminalUI.tError("Room allocation cancelled.");
            TerminalUI.tPause();
            return;
        }

        if (!roomController.allocateRoom(selectedRoom)) {
            TerminalUI.tError("Could not allocate selected room.");
            TerminalUI.tPause();
            return;
        }

        if (!student.oldRoom.equals("UNASSIGNED") && !student.oldRoom.equals("N/A")) {
            roomController.freeRoom(student.oldRoom);
        }

        boolean saved = writeStudentRoom(student.id, selectedRoom);
        if (!saved) {
            TerminalUI.tError("Room updated in room list, but student file save failed.");
            TerminalUI.tPause();
            return;
        }

        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("ROOM UPDATED");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Student ID : " + student.id);
        TerminalUI.tBoxLine("Student    : " + student.name);
        TerminalUI.tBoxLine("Old Room   : " + student.oldRoom);
        TerminalUI.tBoxLine("New Room   : " + selectedRoom);
        TerminalUI.tBoxBottom();
        TerminalUI.tPause();
    }

    private void showAllocationPreview(StudentRecord student, Room room) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("ALLOCATION PREVIEW");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Student ID : " + student.id);
        TerminalUI.tBoxLine("Student    : " + student.name);
        TerminalUI.tBoxLine("Current    : " + student.oldRoom);

        if (room != null) {
            int free = Math.max(0, room.getCapacity() - room.getCurrentOccupancy());
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("Selected Room : " + room.getRoomId());
            TerminalUI.tBoxLine("Occupancy     : " + room.getCurrentOccupancy() + "/" + room.getCapacity());
            TerminalUI.tBoxLine("Free Seats    : " + free);
            TerminalUI.tBoxLine("Status        : " + (room.isAvailable() ? "AVAILABLE" : "FULL"));
        }

        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Confirm allocation? Type Y to proceed, anything else to cancel.");
        TerminalUI.tBoxBottom();
        TerminalUI.tPrompt("Confirm [Y/N]: ");
    }

    private StudentRecord findStudent(String targetId) {
        File file = new File(STUDENT_FILE);
        if (!file.exists()) {
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length > 1) {
                    String fileId = parts[0].trim().replace("\uFEFF", "");
                    if (fileId.equals(targetId.trim())) {
                        StudentRecord record = new StudentRecord();
                        record.id = fileId;
                        record.name = parts[1].trim();
                        record.oldRoom = (parts.length > 7 && !parts[7].trim().isEmpty())
                                ? parts[7].trim()
                                : "UNASSIGNED";
                        return record;
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }

        return null;
    }

    private boolean writeStudentRoom(String targetId, String newRoom) {
        File file = new File(STUDENT_FILE);
        if (!file.exists()) {
            return false;
        }

        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);

                if (parts.length > 0) {
                    String fileId = parts[0].trim().replace("\uFEFF", "");
                    if (fileId.equals(targetId.trim())) {
                        found = true;

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
                    } else {
                        lines.add(line);
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }

        if (!found) {
            return false;
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (String l : lines) {
                pw.println(l);
            }
            return true;
        } catch (IOException e) {
            return false;
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
                    new StudentRoomDashboard(
                            new StudentRoomDashboardController(new RoomService())
                    ).showComplaints(rid);
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

    private static class StudentRecord {
        String id;
        String name;
        String oldRoom;
    }
}