package controllers.food;

import models.food.*;
import models.store.StudentBalance;
import java.time.LocalDate;
import java.io.*;
import java.util.*;

public class CafeteriaController {
    // Standardizing file paths
    private final String WEEKLY_MENU_FILE = "data/foods/weekly_menu.txt";
    private final String CALENDAR_FILE = "data/foods/calendar_schedule.txt";
    private final String CONFIG_FILE = "data/foods/config.txt";
    private final String TOKEN_FILE = "data/foods/tokens.txt";
    private final String BALANCE_FILE = "data/inventories/balances.txt";

    // --- Calendar & Scheduling ---

    public void scheduleSpecialMeal(LocalDate date, MealType type, String items) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CALENDAR_FILE, true))) {
            pw.println(date.toString() + "|" + type + "|" + items);
        } catch (IOException e) {
            System.err.println("Scheduling Error: " + e.getMessage());
        }
    }

    public String getMenuForTime(LocalDate date, String dayOfWeek, MealType type) {
        // Priority 1: Calendar Override
        String special = findInFile(CALENDAR_FILE, date.toString() + "|" + type);
        if (special != null) return "[SPECIAL] " + special;

        // Priority 2: Weekly Standard
        String weekly = findInFile(WEEKLY_MENU_FILE, dayOfWeek + "|" + type);
        return (weekly != null) ? weekly : "Standard menu items";
    }

    private String findInFile(String path, String key) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(key)) {
                    String[] parts = line.split("\\|");
                    return (parts.length > 2) ? parts[2] : null;
                }
            }
        } catch (IOException e) { /* Expected if file doesn't exist */ }
        return null;
    }

    // --- Token Logic ---

    public String purchaseToken(String username) {
        MealType currentSlot = utils.TimeManager.getCurrentMealSlot();
        if (currentSlot == MealType.NONE) {
            return "Transaction Failed: Cafeteria is currently closed.";
        }

        double price = getPriceForMeal(currentSlot);
        StudentBalance student = loadBalance(username);

        if (student == null) {
            return "Transaction Failed: User '" + username + "' not found.";
        }

        if (!student.deductBalance(price)) {
            return "Transaction Failed: Insufficient funds.";
        }

        String uniqueID = "MT-" + LocalDate.now().getYear() + "-" +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        updateBalanceFile(student);

        // FIXED: Using TokenStatus.ACTIVE instead of 'false'
        saveTokenToDatabase(new MealToken(
                uniqueID, username, currentSlot, LocalDate.now(), TokenStatus.ACTIVE
        ));

        return "Success! Token ID: " + uniqueID + " | Cost: " + price + " BDT";
    }

    // --- Persistence (File I/O) ---

    private StudentBalance loadBalance(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(BALANCE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(username + ",")) {
                    return StudentBalance.fromString(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading balances: " + e.getMessage());
        }
        return null;
    }

    private void updateBalanceFile(StudentBalance updatedStudent) {
        List<String> lines = new ArrayList<>();
        try {
            File file = new File(BALANCE_FILE);
            if (!file.exists()) return;

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(updatedStudent.getStudentId() + ",")) {
                        lines.add(updatedStudent.toString());
                    } else {
                        lines.add(line);
                    }
                }
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                for (String l : lines) pw.println(l);
            }
        } catch (IOException e) {
            System.err.println("Critical Error updating balances!");
        }
    }

    private void saveTokenToDatabase(MealToken token) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(TOKEN_FILE, true))) {
            pw.println(token.toString());
        } catch (IOException e) {
            System.err.println("Error saving token: " + e.getMessage());
        }
    }

    public List<MealToken> getStudentTokens(String username) {
        List<MealToken> studentTokens = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(TOKEN_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                MealToken token = MealToken.fromString(line);
                if (token.getStudentId().equals(username)) {
                    studentTokens.add(token);
                }
            }
        } catch (IOException e) { }
        return studentTokens;
    }

    public StudentBalance loadStudentBalance(String username) {
        return loadBalance(username);
    }

    private double getPriceForMeal(MealType type) {
        return switch (type) {
            case BREAKFAST -> 30.0;
            case LUNCH, DINNER -> 60.0;
            case SUHOOR -> 40.0;
            case IFTAR -> 50.0;
            default -> 0.0;
        };
    }
    // Inside CafeteriaController.java
    public void saveMenu(List<DailyMenu> weeklyMenu) {
        new File("data/foods").mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(WEEKLY_MENU_FILE))) {
            for (DailyMenu menu : weeklyMenu) {
                pw.println(menu.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving menu: " + e.getMessage());
        }
    }

    public void setSystemMode(boolean isRamadan) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CONFIG_FILE))) {
            pw.println("RAMADAN=" + isRamadan);
        } catch (IOException e) {
            System.err.println("Error saving configuration: " + e.getMessage());
        }
    }
}