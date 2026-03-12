package controllers.room;

import cli.dashboard.room.StudentRoomDashboard;
import cli.views.room.StudentRoomView;
import controllers.dashboard.room.StudentRoomDashboardController;
import models.room.Room;
import java.io.*;
import java.util.*;
import utils.ConsoleUtil;
import utils.TerminalUI;

public class RoomController {

    private final String ROOM_FILE = "data/rooms/rooms.txt";
    private final String STUDENT_FILE = "data/users/students.txt";
    private final StudentRoomView studentRoomView;
    private List<Room> rooms;

    public RoomController() {
        this.rooms = loadRooms();
        this.studentRoomView = new StudentRoomView();
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
            ConsoleUtil.clearScreen();
            int choice = studentRoomView.show(roomNumber, roomDetails);

            if (choice == 1) {
                if (roomNumber.equals("UNASSIGNED") || roomNumber.equals("N/A")) {
                    TerminalUI.tError("You do not have a room assigned yet.");
                } else {
                    new StudentRoomDashboard(new StudentRoomDashboardController(new RoomService())).showComplaints(roomNumber);
                }
                ConsoleUtil.pause();
            } else {
                ConsoleUtil.clearScreen();
                stayInMenu = false;
            }
        }
    }

    public boolean addRoom(String roomId, int capacity) {
        for (Room r : rooms) {
            if (r.getRoomId().equals(roomId)) {
                TerminalUI.tError("Room " + roomId + " already exists.");
                return false;
            }
        }
        Room newRoom = new Room(roomId, capacity, 0);
        rooms.add(newRoom);
        saveRooms();
        TerminalUI.tSuccess("Room " + roomId + " added.");
        return true;
    }

    public void showAvailableRooms() {
        this.rooms = loadRooms();

        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("AVAILABLE ROOMS");
        TerminalUI.tBoxSep();
        boolean found = false;
        for (Room r : rooms) {
            if (r.isAvailable()) {
                TerminalUI.tBoxLine(r.toString());
                found = true;
            }
        }
        if (!found) {
            TerminalUI.tBoxLine("No rooms available.");
        }
        TerminalUI.tBoxBottom();
    }

    public boolean allocateRoom(String roomId) {
        for (Room r : rooms) {
            if (r.getRoomId().equals(roomId)) {
                if (r.isAvailable()) {
                    r.incrementOccupancy();
                    saveRooms();
                    return true;
                } else {
                    TerminalUI.tError("Room " + roomId + " is full.");
                    return false;
                }
            }
        }
        TerminalUI.tError("Room ID not found.");
        return false;
    }

    public void freeRoom(String roomId) {
        if (roomId == null || roomId.equals("N/A") || roomId.isEmpty() || roomId.equals("UNASSIGNED")) {
            return;
        }

        for (Room r : rooms) {
            if (r.getRoomId().equals(roomId)) {
                r.decrementOccupancy();
                saveRooms();
                return;
            }
        }
    }

//    public void showComplaintsForRoom(String roomId) {
//        if (roomId == null || roomId.trim().isEmpty()) {
//            System.out.println("Invalid room.");
//            return;
//        }
//        MyArrayList<Complaint> all = complaintRepo.findAll();
//        boolean found = false;
//
//        System.out.println("\n------------------ COMPLAINTS FOR ROOM " + roomId + " ------------------");
//        for (int i = 0; i < all.size(); i++) {
//            Complaint c = all.get(i);
//            if (new MyString(c.getStudentRoomNo()).trim().equals(new MyString(roomId).trim())) {
//                found = true;
//                String wid = c.getAssignedWorkerId();
//                boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();
//
//                System.out.println(
//                        "ID: " + c.getComplaintId()
//                                + " | Status: " + c.getStatus().name()
//                                + " | Worker: " + (blank ? "(none)" : wid)
//                                + " | Cat: " + c.getCategory().name()
//                );
//            }
//        }
//        if (!found) System.out.println("(No complaints found for this room)");
//        System.out.println("-----------------------------------------------------------------------\n");
//    }
    private String getStudentRoomNumber(String target) {
        File file = new File(STUDENT_FILE);
        if (!file.exists()) {
            return "UNASSIGNED";
        }
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
        } catch (IOException e) {
            TerminalUI.tError("Failed to read student room assignments.");
        }
        return "UNASSIGNED";
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
            TerminalUI.tError("Failed to count room occupancy.");
        }

        return count;
    }

    private void saveRooms() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ROOM_FILE))) {
            for (Room r : rooms) {
                pw.println(r.toFileString());
            }
        } catch (IOException e) {
            TerminalUI.tError("Failed to save room data.");
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
            TerminalUI.tError("Failed to load rooms.");
        }
        return list;
    }
}
