package controllers.dashboard.room;

import controllers.room.RoomService;
import models.room.Room;
import utils.FastInput;

import java.util.List;

public class RoomDashboardController {

    private final RoomService roomService;

    public RoomDashboardController(RoomService roomService) {
        this.roomService = roomService;
    }

    public void handleInput(int choice) {
        switch (choice) {
            case 1 -> addNewRoomFlow();
            case 2 -> showAvailableRooms();
            default -> System.out.println("Invalid choice!");
        }
    }

    private void addNewRoomFlow() {
        System.out.println("\n--- Add New Room ---");
        System.out.print("Enter Room Number/ID : ");
        String roomId = FastInput.readNonEmptyLine();

        System.out.print("Enter Room Capacity (e.g., 4): ");
        int capacity = FastInput.readInt();

        boolean ok = roomService.addRoom(roomId, capacity);
        System.out.println(ok ? "Success: Room " + roomId + " added." : "Error: Room " + roomId + " already exists.");
    }

    private void showAvailableRooms() {
        List<Room> available = roomService.getAvailableRooms();

        System.out.println("\n--- Available Rooms ---");
        if (available.isEmpty()) {
            System.out.println("No rooms available.");
            return;
        }

        for (Room r : available) System.out.println(r);
    }
}
