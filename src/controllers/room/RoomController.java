package controllers.room;

import cli.views.room.StudentRoomView;
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
    }

    public List<Room> getAllRooms() {
        return rooms;
    }

    public void showStudentRoomDetails(String studentIdentifier) {
        this.rooms = loadRooms();

        String roomNumber = getStudentRoomNumber(studentIdentifier);

        Room roomDetails = null;
        if (!roomNumber.equals("UNASSIGNED") && !roomNumber.equals("N/A")) {
            for (Room r : rooms) {
                if (r.getRoomId().equals(roomNumber)) {
                    roomDetails = r;
                    break;
                }
            }
            if (roomDetails != null) {
                int realOccupancy = countStudentsInRoom(roomNumber);
                roomDetails.setCurrentOccupancy(realOccupancy);
            }
        }

        boolean stayInMenu = true;
        while (stayInMenu) {
            int choice = studentRoomView.show(roomNumber, roomDetails);

            if (choice == 1) {
                if (roomNumber.equals("UNASSIGNED") || roomNumber.equals("N/A")) {
                    System.out.println("\n>> You do not have a room assigned yet.");
                } else {
                    showComplaintsForRoom(roomNumber);
                }
                System.out.print("Press Enter to return...");
                new Scanner(System.in).nextLine();
            } else {
                stayInMenu = false;
            }
        }
    }

    public boolean addRoom(String roomId, int capacity) {
        for (Room r : rooms) {
            if (r.getRoomId().equals(roomId)) {
                System.out.println("Error: Room " + roomId + " already exists.");
                return false;
            }
        }
        Room newRoom = new Room(roomId, capacity, 0);
        rooms.add(newRoom);
        saveRooms();
        System.out.println("Success: Room " + roomId + " added.");
        return true;
    }

    public void showAvailableRooms() {
        this.rooms = loadRooms();

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
                                + " | Status: " + c.getStatus().name()
                                + " | Worker: " + (blank ? "(none)" : wid)
                                + " | Cat: " + c.getCategory().name()
                );
            }
        }
        if (!found) System.out.println("(No complaints found for this room)");
        System.out.println("-----------------------------------------------------------------------\n");
    }
    private String getStudentRoomNumber(String target) {
        File file = new File(STUDENT_FILE);
        if (!file.exists()) return "UNASSIGNED";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length > 1) {
                    String fileId = parts[0].trim().replace("\uFEFF", "");
                    String fileName = parts[1].trim();
                    if (fileId.equals(target.trim()) || fileName.equalsIgnoreCase(target.trim())) {
                        if (parts.length > 7) {
                            String r = parts[7].trim();
                            return r.isEmpty() ? "UNASSIGNED" : r;
                        }
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return "UNASSIGNED";
    }
    private int countStudentsInRoom(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) return 0;

        File file = new File(STUDENT_FILE);
        if (!file.exists()) return 0;

        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length > 7) {
                    String r = parts[7].trim();
                    if (!r.isEmpty() && r.equalsIgnoreCase(roomId.trim())) {
                        count++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return count;
    }

    private void saveRooms() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ROOM_FILE))) {
            for (Room r : rooms) {
                pw.println(r.toFileString());
            }
        } catch (IOException e) { e.printStackTrace(); }
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
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }
}
