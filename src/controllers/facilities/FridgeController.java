package controllers.facilities;

import exceptions.SlotUnavailableException;
import libraries.slots.FirstFitAllocator;

public class FridgeController {
    private FirstFitAllocator allocator = new FirstFitAllocator();
    private String[] currentFridgeSlots = new String[10];

    public void handleFridgeBooking(String studentId) {
        try {
            int assignedSlot = allocator.findSlot(currentFridgeSlots);
            currentFridgeSlots[assignedSlot] = studentId;
            System.out.println("Allocated slot: " + assignedSlot);
        } catch (SlotUnavailableException e) {
            // This is where you handle the exception you created
            System.err.println("Booking Failed: " + e.getMessage());
        }
    }
}