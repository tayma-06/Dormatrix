package controllers.facilities;

import java.util.*;
import libraries.slots.SlotAllocator;
import libraries.logs.Logger;

public class StudyRoomController {
    // Row = 6 Time Slots, Column = 10 seats per slot
    private String[][] seatMap = new String[6][10];

    // Simple boolean array to track if a student "checked-in" for their specific seat
    private boolean[][] checkInStatus = new boolean[6][10];

    public StudyRoomController() {
        // Initialize all seats as empty
        for (int i = 0; i < 6; i++) {
            Arrays.fill(seatMap[i], null);
            Arrays.fill(checkInStatus[i], false);
        }
    }

    public synchronized boolean bookSeat(String studentId, int seatNumber) {
        int currentSlot = SlotAllocator.getCurrentSlotIndex();

        // 1. Basic Validation
        if (seatNumber < 0 || seatNumber >= 10) return false;

        // 2. Availability Check (Exactly like Fridge)
        if (seatMap[currentSlot][seatNumber] != null) {
            System.out.println("Seat already taken!");
            return false;
        }

        // 3. Prevent student from booking multiple seats in the same slot
        for (int i = 0; i < 10; i++) {
            if (studentId.equals(seatMap[currentSlot][i])) {
                System.out.println("You already have a booking for this time.");
                return false;
            }
        }

        // 4. Assign Seat
        seatMap[currentSlot][seatNumber] = studentId;
        Logger.log("Seat " + (seatNumber + 1) + " reserved by " + studentId);

        // 5. Simple Timer for Bag-on-Seat
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!checkInStatus[currentSlot][seatNumber]) {
                    seatMap[currentSlot][seatNumber] = null;
                    System.out.println("\n[SYSTEM] Seat " + (seatNumber + 1) + " released (No-show).");
                }
            }
        }, 30000); // 30 seconds

        return true;
    }

    public void checkIn(int seatNumber) {
        int currentSlot = SlotAllocator.getCurrentSlotIndex();
        checkInStatus[currentSlot][seatNumber] = true;
        System.out.println("Check-in successful for seat " + (seatNumber + 1));
    }

    public String[][] getSeatMap() {
        return seatMap;
    }
}