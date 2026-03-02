package controllers.facilities;

import java.io.*;
import java.util.*;
import libraries.slots.SlotAllocator;
import libraries.logs.Logger;

public class StudyRoomController {

    private static final String FILE_PATH = "data/studyRoomSlots.txt";

    // STATIC arrays ensure all users share the exact same room data
    // Row = 6 Time Slots, Column = 10 physical seats
    private static final String[][] seatMap = new String[6][10];
    private static final boolean[][] checkInStatus = new boolean[6][10];
    private static boolean dataLoaded = false;

    public StudyRoomController() {
        // Only load data from the text file once when the program starts
        if (!dataLoaded) {
            loadData();
            dataLoaded = true;
        }
    }

    // ==========================================
    // 1. BOOKING LOGIC
    // ==========================================
    public synchronized boolean bookSeat(String student, int seatNumber) {
        int currentSlot = SlotAllocator.getCurrentSlotIndex();

        // Check 1: Is the seat number valid?
        if (seatNumber < 0 || seatNumber >= 10) {
            System.out.println("Invalid seat. Please choose 0-9.");
            return false;
        }

        // Check 2: Is the seat already taken?
        if (seatMap[currentSlot][seatNumber] != null) {
            System.out.println("Seat " + (seatNumber + 1) + " is already occupied!");
            return false;
        }

        // Check 3: Does this student already have a seat in this 2-minute slot?
        for (int i = 0; i < 10; i++) {
            if (student.equals(seatMap[currentSlot][i])) {
                System.out.println("You already have a seat booked for this time slot.");
                return false;
            }
        }

        // --- Assign Seat ---
        seatMap[currentSlot][seatNumber] = student;
        checkInStatus[currentSlot][seatNumber] = false; // Reset check-in status
        saveData(); // Update the .txt file immediately

        Logger.log("Seat " + (seatNumber + 1) + " reserved by " + student);

        // --- Start "Bag-on-Seat" Warden Timer ---
        startWardenTimer(student, currentSlot, seatNumber);

        return true;
    }

    // ==========================================
    // 2. CHECK-IN LOGIC
    // ==========================================
    public void checkIn(String student, int seatNumber) {
        int currentSlot = SlotAllocator.getCurrentSlotIndex();

        // Verify the student actually owns this seat in the current slot
        if (student.equals(seatMap[currentSlot][seatNumber])) {
            checkInStatus[currentSlot][seatNumber] = true;
            System.out.println("Check-in successful! Seat " + (seatNumber + 1) + " confirmed.");
            Logger.log(student + " checked in to Seat " + (seatNumber + 1));
        } else {
            System.out.println("Check-in failed. You do not own Seat " + (seatNumber + 1) + " right now.");
        }
    }

    // ==========================================
    // 3. WARDEN TIMER (AUTO-RELEASE)
    // ==========================================
    private void startWardenTimer(String student, int slot, int seat) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // If 30 seconds pass and the check-in status is still false
                if (!checkInStatus[slot][seat]) {
                    seatMap[slot][seat] = null; // Kick them out
                    saveData(); // Save the newly emptied room to the file

                    System.out.println("\n[SYSTEM ALERT] Seat " + (seat + 1) + " released due to No-Show.");
                    Logger.log("AUTO-RELEASE: Seat " + (seat + 1) + " vacated (No-show by " + student + ")");
                }
            }
        }, 30000); // 30,000 milliseconds = 30 real-world seconds
    }

    // ==========================================
    // 4. FILE I/O (SAVE & LOAD)
    // ==========================================
    private synchronized void saveData() {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (int slot = 0; slot < 6; slot++) {
                for (int seat = 0; seat < 10; seat++) {
                    if (seatMap[slot][seat] != null) {
                        // Format: Slot,Seat,StudentName
                        out.println(slot + "," + seat + "," + seatMap[slot][seat]);
                    }
                }
            }
        } catch (IOException e) {
            Logger.log("Error saving study room data: " + e.getMessage());
        }
    }

    private void loadData() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return; // If file doesn't exist yet, just start empty

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(",");
                if (parts.length == 3) {
                    int slot = Integer.parseInt(parts[0]);
                    int seat = Integer.parseInt(parts[1]);
                    String student = parts[2];
                    seatMap[slot][seat] = student;
                }
            }
        } catch (FileNotFoundException e) {
            Logger.log("Could not load study room file.");
        } catch (Exception e) {
            Logger.log("Corrupt data in studyRoomSlots.txt");
        }
    }

    // Helper for CLI to show available seats
    public String[][] getSeatMap() {
        return seatMap;
    }
}