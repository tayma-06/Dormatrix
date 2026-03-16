package controllers.dashboard.room;

import controllers.room.RoomService;
import models.room.Room;

import java.util.ArrayList;
import java.util.List;

public class RoomDashboardController {

    private final RoomService roomService;

    public RoomDashboardController(RoomService roomService) {
        this.roomService = roomService;
    }

    public boolean addRoom(String roomId, int capacity) {
        if (roomId == null || roomId.trim().isEmpty()) {
            return false;
        }
        if (capacity <= 0) {
            return false;
        }
        return roomService.addRoom(roomId.trim(), capacity);
    }

    public List<Room> getAvailableRooms() {
        return roomService.getAvailableRooms();
    }

    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    public int getTotalRoomCount() {
        return getAllRooms().size();
    }

    public int getAvailableRoomCount() {
        return getAvailableRooms().size();
    }

    public int getFullRoomCount() {
        int total = getTotalRoomCount();
        int available = getAvailableRoomCount();
        return Math.max(0, total - available);
    }

    public int getTotalCapacity() {
        int total = 0;
        List<Room> all = getAllRooms();
        for (Room room : all) {
            total += room.getCapacity();
        }
        return total;
    }

    public int getTotalOccupancy() {
        int total = 0;
        List<Room> all = getAllRooms();
        for (Room room : all) {
            total += room.getCurrentOccupancy();
        }
        return total;
    }

    public String[] buildHomePreviewLines(int choice) {
        List<String> lines = new ArrayList<>();

        lines.add("Total Rooms      : " + getTotalRoomCount());
        lines.add("Available Rooms  : " + getAvailableRoomCount());
        lines.add("Full Rooms       : " + getFullRoomCount());
        lines.add("Total Capacity   : " + getTotalCapacity());
        lines.add("Current Occupied : " + getTotalOccupancy());
        lines.add("");

        switch (choice) {
            case 1:
                lines.add("Action Preview");
                lines.add("Create a new room entry and save it");
                lines.add("to data/rooms/rooms.txt.");
                lines.add("");
                lines.add("You will be able to review:");
                lines.add("- Room ID");
                lines.add("- Capacity");
                lines.add("- Initial occupancy (0)");
                break;

            case 2:
                lines.add("Action Preview");
                lines.add("Browse available rooms with");
                lines.add("a live side preview.");
                lines.add("");
                lines.add("The browser shows:");
                lines.add("- occupancy");
                lines.add("- free seats");
                lines.add("- status");
                break;

            default:
                lines.add("Action Preview");
                lines.add("Return to previous menu.");
                break;
        }

        return lines.toArray(new String[0]);
    }

    public String[] buildRoomPreview(Room room) {
        if (room == null) {
            return new String[]{
                    "No room selected."
            };
        }

        int free = Math.max(0, room.getCapacity() - room.getCurrentOccupancy());

        return new String[]{
                "Room ID     : " + room.getRoomId(),
                "Status      : " + (room.isAvailable() ? "AVAILABLE" : "FULL"),
                "Occupancy   : " + room.getCurrentOccupancy() + "/" + room.getCapacity(),
                "Free Seats  : " + free,
                "Summary     : " + room.toString()
        };
    }
}