package libraries.slots;

import java.time.LocalTime;

public abstract class SlotAllocator {
    public static final int REAL_MINS_PER_DAY = 12;
    public static final int REAL_MINS_PER_SLOT = 2;
    public static final int TOTAL_SLOTS = 6; // 12 / 2

    public int getCurrentSlotIndex() {
        // Uses system clock to find which 2-minute window we are in within the 12-min cycle
        long minuteInHour = LocalTime.now().getMinute();
        long secondInHour = LocalTime.now().getSecond();

        long totalSecondsInHour = (minuteInHour * 60) + secondInHour;
        long secondsInCycle = totalSecondsInHour % (REAL_MINS_PER_DAY * 60);

        return (int) (secondsInCycle / (REAL_MINS_PER_SLOT * 60));
    }
}