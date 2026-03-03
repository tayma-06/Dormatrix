package controllers.facilities;

import libraries.slots.SlotAllocator;
import java.io.*;
import java.util.*;

public class LaundryController {
    private static final String FILE_PATH = "data/facility/laundrySlots.txt";
    // Static array ensures all students share the same 6 slots
    private static final String[] laundrySlots = new String[6];
    private static boolean dataLoaded = false;

    public LaundryController() {
        if (!dataLoaded) {
            loadData();
            dataLoaded = true;
        }
    }

    // --- DISPLAY STATUS LOGIC ---
    public void displayLaundryStatus() {
        System.out.println("\n--- Laundry Machine Schedule (6 Slots) ---");
        for (int i = 0; i < laundrySlots.length; i++) {
            String status = (laundrySlots[i] == null) ? "EMPTY" : "TAKEN";
            System.out.print("[ Slot " + (i + 1) + ":" + status + " ]  ");
            if (i == 2) System.out.println();
        }
    }

    // --- BOOKING LOGIC WITH PERSISTENCE ---
    public String bookLaundry(int slotIndex, String student) {
        // Prevent out of bounds
        if (slotIndex < 0 || slotIndex >= 6) return "Invalid Slot Index";

        for (int i = 0; i < laundrySlots.length; i++) {
            if (student.equals(laundrySlots[i])) {
                return "Booking Failed: You already have an active booking for Slot " + (i + 1) + ".";
            }
        }

        // 3. Slot Availability Check: Is the requested slot already taken by someone else?
        if (laundrySlots[slotIndex] != null) {
            return "Booking Failed: Slot " + (slotIndex + 1) + " is already occupied.";
        }
        // Assign and Persist
        laundrySlots[slotIndex] = student;
        saveData();
        return "Success: Laundry Slot " + (slotIndex + 1) + " booked.";
    }

    // --- PERSISTENCE: SAVE ---
    private synchronized void saveData() {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (int i = 0; i < laundrySlots.length; i++) {
                if (laundrySlots[i] != null) {
                    out.println(i + "," + laundrySlots[i]);
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving laundry data.");
        }
    }

    // --- PERSISTENCE: LOAD ---
    private void loadData() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(",");
                if (parts.length == 2) {
                    int index = Integer.parseInt(parts[0]);
                    laundrySlots[index] = parts[1];
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading laundry data.");
        }
    }
}