package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.dashboard.room.StudentRoomDashboard;
import controllers.dashboard.room.StudentRoomDashboardController;
import controllers.room.RoomController;
import cli.views.complaint.ComplaintView;
import controllers.room.RoomService;
import repo.file.FileComplaintRepository;
import models.complaints.Complaint;
import libraries.collections.MyArrayList;

import java.io.*;
import java.util.*;

public class HallOfficeDashboardController {

    private final Scanner sc = new Scanner(System.in);
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
                System.out.println(">> Feature [Worker Schedule] is under development.");
                break;
            case 4:
                System.out.println(">> Feature [Attendant Task] is under development.");
                break;
            case 0:
                mainDashboard.show();
                break;
            default:
                System.out.println("Invalid Choice.");
        }
    }

    private void handleRoomAllocation() {
        while (true) {
            System.out.println();
            System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                          ROOM MANAGEMENT MENU                       ║");
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ 1. View All Rooms status                                            ║");
            System.out.println("║ 2. Allocate/Change Student Room                                     ║");
            System.out.println("║ 0. Back                                                             ║");
            System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.print("Enter choice: ");

            if (sc.hasNextInt()) {
                int subChoice = sc.nextInt();
                sc.nextLine();

                if (subChoice == 1) {
                    for (models.room.Room r : roomController.getAllRooms()) {
                        System.out.println(r);
                    }
                } else if (subChoice == 2) {
                    updateStudentRoom();
                } else if (subChoice == 0) {
                    return;
                } else {
                    System.out.println("Invalid sub-choice.");
                }
            } else {
                sc.nextLine();
            }
        }
    }

    private void updateStudentRoom() {
        System.out.print("Enter Student ID: ");
        String targetId = sc.nextLine().trim();

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

        System.out.print("Enter New Room Number (or type '0' to cancel): ");
        String newRoom = sc.nextLine().trim();

        if (newRoom.equals("0")) return;

        if (roomController.allocateRoom(newRoom)) {
            if (!oldRoom.equals("UNASSIGNED") && !oldRoom.equals("N/A")) {
                roomController.freeRoom(oldRoom);
            }

            String[] parts = currentLine.split("\\|", -1);
            StringBuilder newLine = new StringBuilder();

            for(int i = 0; i < 8; i++) {
                if (i == 7) {
                    newLine.append(newRoom);
                } else if (i < parts.length) {
                    newLine.append(parts[i]);
                } else {
                    newLine.append("");
                }

                if (i < 7) newLine.append("|");
            }

            lines.add(newLine.toString());

            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                for (String l : lines) pw.println(l);
            } catch (IOException e) {
                System.out.println("Error saving student data.");
            }

            System.out.println("SUCCESS: Room updated to " + newRoom);
        }
    }

    private void handleComplaintManagement() {
        while (true) {
            complaintView.attendantMenu();
            if (sc.hasNextInt()) {
                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1:
                        MyArrayList<Complaint> all = complaintRepo.findAll();
                        complaintView.list(all);
                        break;
                    case 2:
                        System.out.println(">> Filtering Pending... (To be implemented fully)");
                        complaintView.list(complaintRepo.findAll());
                        break;
                    case 7:
                        System.out.print("Enter Room ID: ");
                        String rid = sc.nextLine().trim();
                        new StudentRoomDashboard(new StudentRoomDashboardController(new RoomService())).showComplaints(rid);
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Option " + choice + " is coming soon.");
                }
            } else {
                sc.nextLine();
            }
        }
    }
}