package controllers.facilities;

import libraries.slots.FirstFitAllocator;
import exceptions.SlotUnavailableException;

public class FridgeController {
    private FirstFitAllocator allocator = new FirstFitAllocator();
    private static String[] currentFridgeSlots = new String[10];

    public void handleFridgeBooking(String studentId) {
        try {
            int assignedSlot = allocator.findSlot(currentFridgeSlots);
            currentFridgeSlots[assignedSlot] = studentId;
            System.out.println("Allocated slot: " + (assignedSlot+1));
        } catch (SlotUnavailableException e) {
            System.err.println("Booking Failed: " + e.getMessage());
        }
    }
}