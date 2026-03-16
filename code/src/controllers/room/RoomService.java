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
        this.rooms = loadRooms();
        this.complaintRepo = new FileComplaintRepository();
    }

    public List<Room> getAllRooms() {
        this.rooms = loadRooms();
        applyRealOccupancies(this.rooms);
        return rooms;
    }

    public boolean addRoom(String roomId, int capacity) {
        if (roomId == null || roomId.trim().isEmpty() || capacity <= 0) {
            return false;
        }

        this.rooms = loadRooms();

        for (Room r : rooms) {
            if (r.getRoomId().equalsIgnoreCase(roomId.trim())) {
                return false;
            }
        }

        Room newRoom = new Room(roomId.trim(), capacity, 0);
        rooms.add(newRoom);
        saveRooms();
        return true;
    }

    public List<Room> getAvailableRooms() {
        this.rooms = loadRooms();
        applyRealOccupancies(this.rooms);

        List<Room> available = new ArrayList<>();
        for (Room r : rooms) {
            if (r.isAvailable()) {
                available.add(r);
            }
        }
        return available;
    }

    public Room getRoomDetailsWithRealOccupancy(String roomId) {
        this.rooms = loadRooms();
        applyRealOccupancies(this.rooms);

        for (Room r : rooms) {
            if (r.getRoomId().equalsIgnoreCase(roomId)) {
                return r;
            }
        }
        return null;
    }

    public boolean roomExists(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            return false;
        }

        this.rooms = loadRooms();
        for (Room r : rooms) {
            if (r.getRoomId().equalsIgnoreCase(roomId.trim())) {
                return true;
            }
        }
        return false;
    }

    public boolean isRoomAvailable(String roomId) {
        Room room = getRoomDetailsWithRealOccupancy(roomId);
        return room != null && room.isAvailable();
    }

    public String getStudentRoomNumber(String studentIdentifier) {
        File file = new File(STUDENT_FILE);
        if (!file.exists()) {
            return "UNASSIGNED";
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length > 1) {
                    String studentId = parts[0].trim().replace("\uFEFF", "");
                    String studentName = parts[1].trim();

                    if (studentId.equals(studentIdentifier.trim())
                            || studentName.equalsIgnoreCase(studentIdentifier.trim())) {

                        if (parts.length > 7) {
                            String roomNumber = parts[7].trim();
                            return roomNumber.isEmpty() ? "UNASSIGNED" : roomNumber;
                        }
                        return "UNASSIGNED";
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "UNASSIGNED";
    }

    public String resolveStudentId(String studentIdentifier) {
        if (studentIdentifier == null || studentIdentifier.trim().isEmpty()) {
            return null;
        }

        File file = new File(STUDENT_FILE);
        if (!file.exists()) {
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length > 1) {
                    String studentId = parts[0].trim().replace("\uFEFF", "");
                    String studentName = parts[1].trim();

                    if (studentId.equals(studentIdentifier.trim())
                            || studentName.equalsIgnoreCase(studentIdentifier.trim())) {
                        return studentId;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getStudentName(String studentIdentifier) {
        if (studentIdentifier == null || studentIdentifier.trim().isEmpty()) {
            return null;
        }

        File file = new File(STUDENT_FILE);
        if (!file.exists()) {
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length > 1) {
                    String studentId = parts[0].trim().replace("\uFEFF", "");
                    String studentName = parts[1].trim();

                    if (studentId.equals(studentIdentifier.trim())
                            || studentName.equalsIgnoreCase(studentIdentifier.trim())) {
                        return studentName;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<String> getStudentsAllocatedToRoom(String roomId) {
        List<String> students = new ArrayList<>();

        if (roomId == null || roomId.trim().isEmpty()) {
            return students;
        }

        File file = new File(STUDENT_FILE);
        if (!file.exists()) {
            return students;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);

                if (parts.length > 7) {
                    String studentId = parts[0].trim().replace("\uFEFF", "");
                    String studentName = parts[1].trim();
                    String assignedRoom = parts[7].trim();

                    if (!assignedRoom.isEmpty() && assignedRoom.equalsIgnoreCase(roomId.trim())) {
                        String namePart = studentName.isEmpty() ? "(no name)" : studentName;
                        students.add(studentId + "  |  " + namePart);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return students;
    }

    public boolean changeStudentRoom(String studentId, String newRoom) {
        if (studentId == null || studentId.trim().isEmpty()
                || newRoom == null || newRoom.trim().isEmpty()) {
            return false;
        }

        String targetRoom = newRoom.trim();

        if (!roomExists(targetRoom)) {
            return false;
        }

        Room room = getRoomDetailsWithRealOccupancy(targetRoom);
        if (room == null || !room.isAvailable()) {
            return false;
        }

        File file = new File(STUDENT_FILE);
        if (!file.exists()) {
            return false;
        }

        List<String> updated = new ArrayList<>();
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);

                if (parts.length > 0) {
                    String fileId = parts[0].trim().replace("\uFEFF", "");
                    if (fileId.equals(studentId.trim())) {
                        found = true;

                        String oldRoom = parts.length > 7 ? parts[7].trim() : "UNASSIGNED";
                        if (oldRoom.equalsIgnoreCase(targetRoom)) {
                            return false;
                        }

                        String[] rebuilt = ensureLength(parts, 8);
                        rebuilt[7] = targetRoom;
                        updated.add(joinWithPipe(rebuilt));
                    } else {
                        updated.add(line);
                    }
                } else {
                    updated.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (!found) {
            return false;
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (String s : updated) {
                pw.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        syncRoomOccupanciesFromStudents();
        return true;
    }

    public MyArrayList<Complaint> getComplaintsForRoom(String roomId) {
        MyArrayList<Complaint> all = complaintRepo.findAll();
        MyArrayList<Complaint> result = new MyArrayList<>();

        if (roomId == null || roomId.trim().isEmpty()) {
            return result;
        }

        for (int i = 0; i < all.size(); i++) {
            Complaint c = all.get(i);
            if (new MyString(c.getStudentRoomNo()).trim().equals(new MyString(roomId).trim())) {
                result.add(c);
            }
        }
        return result;
    }

    private void applyRealOccupancies(List<Room> roomList) {
        for (Room r : roomList) {
            r.setCurrentOccupancy(countStudentsInRoom(r.getRoomId()));
        }
    }

    private void syncRoomOccupanciesFromStudents() {
        this.rooms = loadRooms();
        applyRealOccupancies(this.rooms);
        saveRooms();
    }

    private int countStudentsInRoom(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            return 0;
        }

        File file = new File(STUDENT_FILE);
        if (!file.exists()) {
            return 0;
        }

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Room> loadRooms() {
        List<Room> list = new ArrayList<>();
        File file = new File(ROOM_FILE);
        if (!file.exists()) {
            return list;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Room r = Room.fromString(line);
                if (r != null) {
                    list.add(r);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private String[] ensureLength(String[] parts, int len) {
        String[] out = new String[len];
        for (int i = 0; i < len; i++) {
            out[i] = i < parts.length ? parts[i] : "";
        }
        return out;
    }

    private String joinWithPipe(String[] parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            sb.append(parts[i] == null ? "" : parts[i]);
            if (i < parts.length - 1) {
                sb.append("|");
            }
        }
        return sb.toString();
    }
}