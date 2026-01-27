package controllers.dashboard;

import controllers.room.RoomController;
import java.io.*;
import java.util.*;

public class HallOfficeDashboardController {

    private final Scanner sc = new Scanner(System.in);
    private final String STUDENT_FILE = "data/users/students.txt";
    private final RoomController roomController = new RoomController();

    public void handleInput(int choice, String username) {
        switch (choice) {
            case 1:
                handleRoomAllocation();
                break;
            case 2:
                System.out.println(">> Feature [View Complaints] is under development.");
                break;
            case 3:
                System.out.println(">> Feature [Worker Schedule] is under development.");
                break;
            case 4:
                System.out.println(">> Feature [Attendant Task] is under development.");
                break;
            default:
                System.out.println("Invalid Choice.");
        }
    }

    private void handleRoomAllocation() {
        System.out.println("\n=== Room Management Menu ===");
        System.out.println("1. View All Rooms status");
        System.out.println("2. Allocate/Change Student Room");
        System.out.print("Enter choice: ");
        int subChoice = sc.nextInt();
        sc.nextLine();

        if (subChoice == 1) {
            for (models.room.Room r : roomController.getAllRooms()) {
                System.out.println(r);
            }
        } else if (subChoice == 2) {
            updateStudentRoom();
        } else {
            System.out.println("Invalid sub-choice.");
        }
    }
    private void updateStudentRoom() {
        System.out.print("Enter Student ID: ");
        String targetId = sc.nextLine();
        List<String> lines = new ArrayList<>();
        boolean found = false;
        String oldRoom = null;
        String currentLine = null;
        File file = new File(STUDENT_FILE);
        if (!file.exists()) {
            System.out.println("Student database missing.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length > 0 && parts[0].trim().equals(targetId)) {
                    found = true;
                    currentLine = line;
                    // Check if they already have a room (index 7)
                    oldRoom = (parts.length > 7) ? parts[7] : "N/A";
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) { return; }
        if (!found) {
            System.out.println("Student not found.");
            return;
        }
        System.out.println("Student found. Current Room: " + oldRoom);
        roomController.showAvailableRooms();

        System.out.print("Enter New Room Number (or type '0' to cancel): ");
        String newRoom = sc.nextLine();

        if (newRoom.equals("0")) return;
        if (roomController.allocateRoom(newRoom)) {
            if (!oldRoom.equals("N/A") && !oldRoom.isEmpty()) {
                roomController.freeRoom(oldRoom);
            }
            String[] parts = currentLine.split("\\|");
            StringBuilder newLine = new StringBuilder();
            for(int i=0; i<7; i++) {
                if (i < parts.length) newLine.append(parts[i]).append("|");
                else newLine.append("|");
            }
            newLine.append(newRoom);
            lines.add(newLine.toString());
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                for (String l : lines) pw.println(l);
            } catch (IOException e) {
                System.out.println("Error saving student data.");
            }

            System.out.println("SUCCESS: Room updated to " + newRoom);
        }
    }
}