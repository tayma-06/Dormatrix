package controllers.facilities;

import java.util.*;
import libraries.slots.SlotAllocator;

public class StudyRoomController {
    // 6 slots, each holding a list of booked Student IDs (if multiple seats)
    private Map<Integer, List<String>> bookings = new HashMap<>();
    private Set<String> checkedInUsers = new HashSet<>();

    public boolean bookSeat(String studentId) {
        int currentSlot = SlotAllocator.getCurrentSlotIndex();

        // Prevent double booking in same slot
        if (bookings.getOrDefault(currentSlot, new ArrayList<>()).contains(studentId)) {
            return false;
        }

        bookings.computeIfAbsent(currentSlot, k -> new ArrayList<>()).add(studentId);

        // Bag-on-seat: 30-second real-life grace period (equivalent to virtual check-in)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!checkedInUsers.contains(studentId)) {
                    bookings.get(currentSlot).remove(studentId);
                    System.out.println("\nNo-show: Seat released for " + studentId);
                }
            }
        }, 30000);

        return true;
    }

    public void checkIn(String studentId) {
        checkedInUsers.add(studentId);
    }
}