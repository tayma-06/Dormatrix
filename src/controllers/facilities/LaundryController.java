package controllers.facilities;

import libraries.slots.SlotAllocator;

public class LaundryController {
    private String[] laundrySlots = new String[6]; // 6 slots of 2 mins each

    public String bookLaundry(int slotIndex, String studentId) {
        if (laundrySlots[slotIndex] != null) return "Slot Occupied";
        laundrySlots[slotIndex] = studentId;
        return "Success";
    }

}