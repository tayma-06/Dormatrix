package cli.views.room;

import models.room.Room;
import java.util.Scanner;

public class StudentRoomView {

    private final Scanner scanner;

    public StudentRoomView() {
        this.scanner = new Scanner(System.in);
    }

    public int show(String roomNumber, Room room) {
        System.out.println("\n---------------------------------------------");
        System.out.println("|               MY ROOM STATUS              |");
        System.out.println("---------------------------------------------");

        if (roomNumber.equals("UNASSIGNED") || roomNumber.equals("N/A")) {
            System.out.printf("| %-10s %-30s |\n", "Status:", "NOT ASSIGNED");
            System.out.printf("| %-10s %-30s |\n", "Action:", "Please contact Hall Office.");
        } else {
            System.out.printf("| %-10s %-30s |\n", "Room No:", roomNumber);

            if (room != null) {
                String status = room.isAvailable() ? "AVAILABLE" : "FULL";
                System.out.printf("| %-10s %-30s |\n", "Occupancy:",
                        room.getCurrentOccupancy() + "/" + room.getCapacity());
                System.out.printf("| %-10s %-30s |\n", "Status:", status);
            } else {
                System.out.printf("| %-10s %-30s |\n", "Info:", "Room details not found.");
            }
        }
        System.out.println("---------------------------------------------");
        System.out.println("| 1. My Room Complaints                     |");
        System.out.println("| 0. Exit                                   |");
        System.out.println("---------------------------------------------");
        System.out.print("Enter choice: ");

        while (true) {
            String input = scanner.nextLine().trim();
            if (input.equals("0")) {
                return 0;
            } else if (input.equals("1")) {
                return 1;
            } else {
                System.out.print("Invalid choice. Enter 0 or 1: ");
            }
        }
    }
}
