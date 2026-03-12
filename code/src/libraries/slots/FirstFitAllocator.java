package libraries.slots;

import exceptions.SlotUnavailableException;

public class FirstFitAllocator extends SlotAllocator {
    public int findSlot(String[] slots) throws SlotUnavailableException {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                return i;
            }
        }
        throw new SlotUnavailableException("No available slots found in fridge.");
    }
}