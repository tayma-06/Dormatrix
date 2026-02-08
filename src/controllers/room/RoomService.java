package controllers.room;

import models.room.Room;
import repo.file.FileComplaintRepository;
import libraries.collections.MyArrayList;
import libraries.collections.MyString;
import models.complaints.Complaint;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RoomService {

    private final String ROOM_FILE = "data/rooms/rooms.txt";
    private final String STUDENT_FILE = "data/users/students.txt";

    private List<Room> rooms;
    private final FileComplaintRepository complaintRepo;

    public RoomService() {
        this.rooms = loadRooms(); // Initialize with loaded rooms
        this.complaintRepo = new FileComplaintRepository();
    }

    public List<Room> getAllRooms() {
        this.rooms = loadRooms(); // Always reload rooms when getting them
        return rooms;
    }

    public boolean addRoom(String roomId, int capacity) {
        this.rooms = loadRooms(); // Reload rooms to get the latest data

        for (Room r : rooms) {
            if (r.getRoomId().equals(roomId)) {
                return false;
            }
        }

        Room newRoom = new Room(roomId, capacity, 0);
        rooms.add(newRoom);
        saveRooms();
        return true;
    }

    public List<Room> getAvailableRooms() {
        this.rooms = loadRooms(); // Reload rooms before checking availability

        List<Room> available = new ArrayList<>();
        for (Room r : rooms) {
            if (r.isAvailable()) available.add(r);
        }
        return available;
    }

    public Room getRoomDetailsWithRealOccupancy(String roomId) {
        this.rooms = loadRooms(); // Reload rooms to ensure current data

        Room room = null;
        for (Room r : rooms) {
            if (r.getRoomId().equals(roomId)) {
                room = r;
                break;
            }
        }

        if (room != null) {
            int realOcc = countStudentsInRoom(roomId);
            room.setCurrentOccupancy(realOcc);
        }

        return room;
    }

    public String getStudentRoomNumber(String studentIdentifier) {
        this.rooms = loadRooms(); // Reload rooms if needed

        File file = new File(STUDENT_FILE);
        if (!file.exists()) return "UNASSIGNED";

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length > 1) {
                    String studentId = parts[0].trim();
                    String studentName = parts[1].trim();
                    if (studentId.equals(studentIdentifier.trim())
                            || studentName.equalsIgnoreCase(studentIdentifier.trim())) {

                        if (parts.length > 7) {
                            String roomNumber = parts[7].trim();
                            return roomNumber.isEmpty() ? "UNASSIGNED" : roomNumber;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "UNASSIGNED";
    }

    public boolean allocateRoom(String roomId) {
        this.rooms = loadRooms(); // Reload rooms before allocating

        for (Room r : rooms) {
            if (r.getRoomId().equals(roomId)) {
                if (r.isAvailable()) {
                    r.incrementOccupancy();
                    saveRooms();
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public void freeRoom(String roomId) {
        this.rooms = loadRooms(); // Reload rooms before freeing any room

        if (roomId == null || roomId.equals("N/A") || roomId.isEmpty() || roomId.equals("UNASSIGNED")) return;

        for (Room r : rooms) {
            if (r.getRoomId().equals(roomId)) {
                r.decrementOccupancy();
                saveRooms();
                return;
            }
        }
    }

    public MyArrayList<Complaint> getComplaintsForRoom(String roomId) {
        MyArrayList<Complaint> all = complaintRepo.findAll();
        MyArrayList<Complaint> result = new MyArrayList<>();

        if (roomId == null || roomId.trim().isEmpty()) return result;

        for (int i = 0; i < all.size(); i++) {
            Complaint c = all.get(i);
            if (new MyString(c.getStudentRoomNo()).trim().equals(new MyString(roomId).trim())) {
                result.add(c);
            }
        }
        return result;
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
            for (Room r : rooms) pw.println(r.toFileString());
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
}
