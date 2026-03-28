package controllers.miscellaneous;

import models.miscellaneous.LostItem;
import models.miscellaneous.FoundItem;
import libraries.logs.Logger;

import java.io.*;
import java.util.*;

public class LostFoundController {
    // Storing these in the root data folder based on your original architecture
    private static final String LOST_FILE = "data/lostItems.txt";
    private static final String FOUND_FILE = "data/foundItems.txt";

    // --- REPORT A LOST ITEM (UPDATED TO USE UUID) ---
    public void reportLostItem(String name, String desc, String userId) {
        // Generates a bulletproof unique ID and takes just the first 8 characters
        String uniqueHash = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String id = "LID-" + uniqueHash;

        String date = new java.util.Date().toString();
        LostItem item = new LostItem(id, name, desc, userId, date);

        saveToFile(LOST_FILE, item.toString());
        // Updated the logger to also record the new ID for your audit trail
        Logger.log("User " + userId + " reported lost item: " + name + " (ID: " + id + ")");
    }

    // --- ADD A FOUND ITEM ---
    public void addFoundItem(String lostItemId) {
        String uniqueHash = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String id = "FID-" + uniqueHash;

        // Create the item using just the ID and the Lost Item's ID
        FoundItem item = new FoundItem(id, lostItemId);

        saveToFile(FOUND_FILE, item.toString());
        Logger.log("Staff marked lost item " + lostItemId + " as FOUND. (Found ID: " + id + ")");
    }

    // --- VIEW ALL FOUND ITEMS ---
    public List<String> getFoundItems() {
        return loadLines(FOUND_FILE);
    }

    // --- VIEW ALL LOST ITEMS ---
    public List<String> getLostItems() {
        return loadLines(LOST_FILE);
    }

    // --- CLAIM VERIFICATION MODULE ---
    public boolean verifyAndClaim(String itemId, String claimantId) {
        List<String> items = loadLines(FOUND_FILE);
        boolean found = false;

        File file = new File(FOUND_FILE);
        file.getParentFile().mkdirs(); // Safety check

        try (PrintWriter out = new PrintWriter(new FileWriter(file, false))) {
            for (String line : items) {
                String[] parts = line.split(",");
                // If ID matches and it is NOT currently claimed (parts[4] is the boolean)
                if (parts[0].equals(itemId) && parts[4].equals("false")) {
                    // Update the string: isClaimed = true, claimantId = claimantId
                    out.println(parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3] + ",true," + claimantId);
                    found = true;
                    Logger.log("Item " + itemId + " claimed by " + claimantId + " pending verification.");
                } else {
                    out.println(line); // Write back unchanged
                }
            }
        } catch (IOException e) {
            System.err.println("Error updating claim status.");
        }
        return found;
    }

    // --- REUSABLE FILE I/O HELPERS ---
    private void saveToFile(String path, String data) {
        File file = new File(path);
        file.getParentFile().mkdirs(); // Ensures the data folder exists so it doesn't crash

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(data);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error saving to " + path);
        }
    }

    private List<String> loadLines(String path) {
        List<String> lines = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return lines;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading " + path);
        }
        return lines;
    }
}