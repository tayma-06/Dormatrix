package controllers.facilities;

import java.io.*;
import java.util.*;
import libraries.slots.SlotAllocator;
import libraries.logs.Logger;

public class StudyRoomController {

    private static final String FILE_PATH = "data/facility/studyRoomSlots.txt";

    // 2 minutes in milliseconds
    private static final long SLOT_DURATION_MS = 120000L;

    // STATIC arrays ensure memory is shared across the current terminal
    private static final String[][] seatMap = new String[6][10];
    private static final boolean[][] checkInStatus = new boolean[6][10];
    private static final long[][] bookingTimestamp = new long[6][10];

    public StudyRoomController() {
        // We no longer use 'dataLoaded'. We want it to load fresh every time!
        loadData();
    }

    // ==========================================
    // THE SWEEPER: Clears expired 2-min bookings
    // ==========================================
    private synchronized void cleanUpExpiredSeats() {
        long now = System.currentTimeMillis();
        boolean changed = false;

        for (int slot = 0; slot < 6; slot++) {
            for (int seat = 0; seat < 10; seat++) {
                if (seatMap[slot][seat] != null) {
                    long age = now - bookingTimestamp[slot][seat];
                    if (age >= SLOT_DURATION_MS) {
                        seatMap[slot][seat] = null; // Free the seat
                        bookingTimestamp[slot][seat] = 0;
                        checkInStatus[slot][seat] = false;
                        changed = true;
                    }
                }
            }
        }
        if (changed) saveData(); // Update file if ghosts were removed
    }

    // ==========================================
    // 1. BOOKING LOGIC
    // ==========================================
    public synchronized boolean bookSeat(String student, int seatIndex) {
        // ALWAYS pull the latest file data from the OTHER terminal first
        loadData();
        cleanUpExpiredSeats();

        int currentSlot = SlotAllocator.getCurrentSlotIndex();

        if (seatIndex < 0 || seatIndex >= 10) {
            System.out.println("Invalid seat.");
            return false;
        }

        if (seatMap[currentSlot][seatIndex] != null) {
            System.out.println("Seat " + (seatIndex + 1) + " is already occupied!");
            return false;
        }

        // Check if student already has a seat
        for (int i = 0; i < 10; i++) {
            if (student.equals(seatMap[currentSlot][i])) {
                System.out.println("You already have a seat booked for this time slot.");
                return false;
            }
        }

        // Assign Seat
        seatMap[currentSlot][seatIndex] = student;
        checkInStatus[currentSlot][seatIndex] = false; // False because they haven't checked in yet
        bookingTimestamp[currentSlot][seatIndex] = System.currentTimeMillis();

        // Immediately push to text file so Terminal B can see it!
        saveData();

        Logger.log("Seat " + (seatIndex + 1) + " reserved by " + student);
        startWardenTimer(student, currentSlot, seatIndex);

        return true;
    }

    // ==========================================
    // 2. CHECK-IN LOGIC
    // ==========================================
    public synchronized void checkIn(String student, int seatIndex) {
        loadData();
        cleanUpExpiredSeats();

        int currentSlot = SlotAllocator.getCurrentSlotIndex();

        if (student.equals(seatMap[currentSlot][seatIndex])) {
            checkInStatus[currentSlot][seatIndex] = true;
            System.out.println("Check-in successful! Seat " + (seatIndex + 1) + " confirmed.");
            Logger.log(student + " checked in to Seat " + (seatIndex + 1));
        } else {
            System.out.println("Check-in failed. You do not own Seat " + (seatIndex + 1) + " right now.");
        }
    }

    // ==========================================
    // 3. WARDEN TIMER
    // ==========================================
    private void startWardenTimer(String student, int slot, int seat) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // Safely grab lock before altering arrays
                synchronized (StudyRoomController.this) {
                    if (!checkInStatus[slot][seat]) {
                        seatMap[slot][seat] = null;
                        bookingTimestamp[slot][seat] = 0;
                        saveData(); // Push the eviction to the text file instantly
                        System.out.println("\n[SYSTEM ALERT] Seat " + (seat + 1) + " released due to No-Show.");
                        Logger.log("AUTO-RELEASE: Seat " + (seat + 1) + " vacated (No-show by " + student + ")");
                    }
                }
            }
        }, 30000);
    }

    // ==========================================
    // 4. FILE I/O (Multi-Terminal Safe)
    // ==========================================
    private synchronized void saveData() {
        long now = System.currentTimeMillis();
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();

        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            for (int slot = 0; slot < 6; slot++) {
                for (int seat = 0; seat < 10; seat++) {
                    if (seatMap[slot][seat] != null) {
                        if (now - bookingTimestamp[slot][seat] < SLOT_DURATION_MS) {
                            out.println(slot + "," + seat + "," + seatMap[slot][seat] + "," + bookingTimestamp[slot][seat]);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Logger.log("Error saving study room data.");
        }
    }

    private synchronized void loadData() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        long now = System.currentTimeMillis();

        // 1. Create temporary blank slates so deleted seats don't carry over
        String[][] newSeatMap = new String[6][10];
        long[][] newTimestamps = new long[6][10];
        boolean[][] newCheckInStatus = new boolean[6][10];

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(",");
                if (parts.length < 4) continue;

                int slot = Integer.parseInt(parts[0].trim());
                int seat = Integer.parseInt(parts[1].trim());
                String student = parts[2].trim();
                long timestamp = Long.parseLong(parts[3].trim());

                if (now - timestamp < SLOT_DURATION_MS) {
                    newSeatMap[slot][seat] = student;
                    newTimestamps[slot][seat] = timestamp;

                    // 2. CRITICAL: Protect the Warden Timer
                    if (student.equals(seatMap[slot][seat])) {
                        // If our terminal already knew about this, keep its check-in status
                        newCheckInStatus[slot][seat] = checkInStatus[slot][seat];
                    } else {
                        // If the OTHER terminal booked this, default to true so we don't kick them
                        newCheckInStatus[slot][seat] = true;
                    }
                }
            }
        } catch (Exception e) {
            Logger.log("Corrupt data in studyRoomSlots.txt");
            return; // Abort load if file is currently being written to by the other terminal
        }

        // 3. Overwrite the real memory with the newly synced state
        for (int i = 0; i < 6; i++) {
            System.arraycopy(newSeatMap[i], 0, seatMap[i], 0, 10);
            System.arraycopy(newTimestamps[i], 0, bookingTimestamp[i], 0, 10);
            System.arraycopy(newCheckInStatus[i], 0, checkInStatus[i], 0, 10);
        }
    }

    public synchronized String[][] getSeatMap() {
        loadData(); // Sync with other terminals before viewing
        cleanUpExpiredSeats();
        return seatMap;
    }
}