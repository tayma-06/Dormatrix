package controllers.room;

import cli.views.StudentRoomView;
import models.room.Room;
import java.io.*;
import java.util.*;

import repo.file.FileComplaintRepository;
import libraries.collections.MyArrayList;
import libraries.collections.MyString;
import models.complaints.Complaint;


public class RoomController {

    private final String ROOM_FILE = "data/rooms/rooms.txt";
    private final String STUDENT_FILE = "data/users/students.txt";

    private final StudentRoomView studentRoomView;
    private List<Room> rooms;

    private final FileComplaintRepository complaintRepo;


    public RoomController() {
        this.rooms = loadRooms();
        this.studentRoomView = new StudentRoomView();
        this.complaintRepo = new FileComplaintRepository();

        if (this.rooms.isEmpty()) {
            seedRooms();
            this.rooms = loadRooms();
        }
    }

    public List<Room> getAllRooms() {
        return rooms;
    }

    public String fetchStudentRoomNumber(String studentIdentifier) {
        return getStudentRoomNumber(studentIdentifier);
    }

    // ==========================================
    // 1. STUDENT FEATURE: SHOW MY ROOM
    // ==========================================
    public void showStudentRoomDetails(String studentIdentifier) {
        // Renamed 'studentId' to 'studentIdentifier' since it can now be a Name or ID
        String roomNumber = getStudentRoomNumber(studentIdentifier);

        Room roomDetails = null;
        if (!roomNumber.equals("UNASSIGNED") && !roomNumber.equals("N/A")) {
            for (Room r : rooms) {
                if (r.getRoomId().equals(roomNumber)) {
                    roomDetails = r;
                    break;
                }
            }
        }
        studentRoomView.show(roomNumber, roomDetails);
    }

    // ==========================================
    // 2. CORE LOGIC: SEARCH BY ID OR NAME
    // ==========================================
    private String getStudentRoomNumber(String target) {
        File file = new File(STUDENT_FILE);
        if (!file.exists()) return "UNASSIGNED";

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);

                if (parts.length > 1) {
                    // Column 0: ID
                    String fileId = parts[0].trim().replace("\uFEFF", "");
                    // Column 1: Name
                    String fileName = parts[1].trim();

                    // CHECK BOTH: Match ID OR Match Name (Case Insensitive)
                    boolean matchesId = fileId.equals(target.trim());
                    boolean matchesName = fileName.equalsIgnoreCase(target.trim());

                    if (matchesId || matchesName) {
                        // Return Room (Index 7)
                        if (parts.length > 7) {
                            String r = parts[7].trim();
                            return r.isEmpty() ? "UNASSIGNED" : r;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "UNASSIGNED";
    }

    // ==========================================
    // 3. ADMIN / SYSTEM LOGIC
    // ==========================================
    public void showAvailableRooms() {
        System.out.println("\n--- Available Rooms ---");
        boolean found = false;
        for (Room r : rooms) {
            if (r.isAvailable()) {
                System.out.println(r);
                found = true;
            }
        }
        if (!found) System.out.println("No rooms available.");
    }

    public boolean allocateRoom(String roomId) {
        for (Room r : rooms) {
            if (r.getRoomId().equals(roomId)) {
                if (r.isAvailable()) {
                    r.incrementOccupancy();
                    saveRooms();
                    return true;
                } else {
                    System.out.println("Error: Room " + roomId + " is full.");
                    return false;
                }
            }
        }
        System.out.println("Error: Room ID not found.");
        return false;
    }

    public void freeRoom(String roomId) {
        if (roomId == null || roomId.equals("N/A") || roomId.isEmpty() || roomId.equals("UNASSIGNED")) return;

        for (Room r : rooms) {
            if (r.getRoomId().equals(roomId)) {
                r.decrementOccupancy();
                saveRooms();
                return;
            }
        }
    }

    // ==========================================
    // Shows complaints for room
    // ==========================================
    public void showComplaintsForRoom(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            System.out.println("Invalid room.");
            return;
        }

        MyArrayList<Complaint> all = complaintRepo.findAll();
        boolean found = false;

        System.out.println("\n------------------ COMPLAINTS FOR ROOM " + roomId + " ------------------");

        for (int i = 0; i < all.size(); i++) {
            Complaint c = all.get(i);

            if (new MyString(c.getStudentRoomNo()).trim().equals(new MyString(roomId).trim())) {
                found = true;

                String wid = c.getAssignedWorkerId();
                boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();

                System.out.println(
                        "ID: " + c.getComplaintId()
                                + " | Student: " + c.getStudentId()
                                + " | Cat: " + c.getCategory().name()
                                + " | Status: " + c.getStatus().name()
                                + " | Worker: " + (blank ? "(none)" : wid)
                                + " | Priority: " + c.getPriority().name()
                );
            }
        }

        if (!found) System.out.println("(No complaints found for this room)");
        System.out.println("-----------------------------------------------------------------------\n");
    }

    public void showComplaintsForMyRoom(String studentIdentifier) {
        String roomNumber = getStudentRoomNumber(studentIdentifier);
        if (roomNumber == null || roomNumber.equals("UNASSIGNED") || roomNumber.equals("N/A")) {
            System.out.println("You are not assigned a room yet. No room-based complaints to show.");
            return;
        }
        showComplaintsForRoom(roomNumber);
    }



    // ==========================================
    // 4. DATA HELPERS
    // ==========================================
    private void saveRooms() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ROOM_FILE))) {
            for (Room r : rooms) {
                pw.println(r.toFileString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Room> loadRooms() {
        List<Room> list = new ArrayList<>();
        File file = new File(ROOM_FILE);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Room r = Room.fromString(line);
                if (r != null) list.add(r);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void seedRooms() {
        new File("data/rooms").mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(ROOM_FILE))) {
            for (int i = 301; i <= 320; i++) {
                pw.println(i + "|4|0");
            }
        } catch (IOException e) { }
    }
}