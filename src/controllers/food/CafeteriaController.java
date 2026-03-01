package controllers.food;

import models.food.*;
import models.store.StudentBalance;
import utils.TimeManager;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class CafeteriaController {

    private final String WEEKLY_MENU_FILE = "data/foods/weekly_menu.txt";
    private final String CALENDAR_FILE = "data/foods/calendar_schedule.txt";
    private final String CONFIG_FILE = "data/foods/config.txt";
    private final String TOKEN_FILE = "data/foods/tokens.txt";
    private final String BALANCE_FILE = "data/inventories/balances.txt";

    public CafeteriaController() {
        ensureFile(TOKEN_FILE);
        ensureFile(WEEKLY_MENU_FILE);
        ensureFile(CALENDAR_FILE);
        ensureFile(CONFIG_FILE);
        ensureFile(BALANCE_FILE);
    }

    private void ensureFile(String path) {
        try {
            File f = new File(path);
            File parent = f.getParentFile();
            if (parent != null) parent.mkdirs();
            if (!f.exists()) f.createNewFile();
        } catch (IOException e) {
            System.err.println("File init error: " + path + " -> " + e.getMessage());
        }
    }

    public void scheduleSpecialMeal(LocalDate date, MealType type, String items) {
        ensureFile(CALENDAR_FILE);
        try (PrintWriter pw = new PrintWriter(new FileWriter(CALENDAR_FILE, true))) {
            pw.println(date + "|" + type + "|" + items);
        } catch (IOException e) {
            System.err.println("Scheduling Error: " + e.getMessage());
        }
    }

    public String getMenuForTime(LocalDate date, String dayOfWeek, MealType type) {
        if (type == MealType.NONE) return "Cafeteria Closed";
        String special = findInFile(CALENDAR_FILE, date + "|" + type, 2);
        if (special != null) return "[SPECIAL] " + special;
        boolean isRamadan = TimeManager.isRamadanMode();
        String searchKey = dayOfWeek.toUpperCase() + "|" + type + "|" + isRamadan;

        String weekly = findInFile(WEEKLY_MENU_FILE, searchKey, 3);
        return (weekly != null) ? weekly : "Standard menu items";
    }

    private String findInFile(String path, String key, int itemIndex) {
        ensureFile(path);
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.toUpperCase().startsWith(key.toUpperCase())) {
                    String[] parts = line.split("\\|", itemIndex + 1);
                    return (parts.length > itemIndex) ? parts[itemIndex] : null;
                }
            }
        } catch (IOException ignored) { }
        return null;
    }

    public String purchaseToken(String username) {
        MealType currentSlot = TimeManager.getCurrentMealSlot();
        if (currentSlot == MealType.NONE) return "Transaction Failed: Cafeteria is currently closed.";
        LocalDate effectiveDate = TimeManager.nowDate();
        if (hasActiveToken(username, currentSlot, effectiveDate)) return "Transaction Failed: You already have an ACTIVE token for " + currentSlot + " today.";
        double price = getPriceForMeal(currentSlot);
        StudentBalance student = loadBalance(username);
        if (student == null) return "Transaction Failed: User '" + username + "' not found.";
        if (!student.deductBalance(price)) return "Transaction Failed: Insufficient funds.";
        String uniqueID = "MT-" + effectiveDate.getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        updateBalanceFile(student);
        saveTokenToDatabase(new MealToken(uniqueID, username, currentSlot, effectiveDate, TokenStatus.ACTIVE));
        return "Success! Token ID: " + uniqueID + " | Cost: " + price + " BDT";
    }

    private boolean hasActiveToken(String username, MealType type, LocalDate date) {
        for (MealToken t : getStudentTokens(username)) {
            if (t.getType() == type && t.getDate().equals(date) && t.getStatus() == TokenStatus.ACTIVE) return true;
        }
        return false;
    }

    private StudentBalance loadBalance(String username) {
        ensureFile(BALANCE_FILE);
        try (BufferedReader br = new BufferedReader(new FileReader(BALANCE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(username + ",")) return StudentBalance.fromString(line);
            }
        } catch (IOException e) { System.out.println("Error reading balances: " + e.getMessage()); }
        return null;
    }

    private void updateBalanceFile(StudentBalance updatedStudent) {
        ensureFile(BALANCE_FILE);
        List<String> lines = new ArrayList<>();
        File file = new File(BALANCE_FILE);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(updatedStudent.getStudentId() + ",")) lines.add(updatedStudent.toString());
                else lines.add(line);
            }
        } catch (IOException e) { return; }
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (String l : lines) pw.println(l);
        } catch (IOException e) { System.err.println("Critical Error writing balances!"); }
    }

    private void saveTokenToDatabase(MealToken token) {
        ensureFile(TOKEN_FILE);
        try (PrintWriter pw = new PrintWriter(new FileWriter(TOKEN_FILE, true))) { pw.println(token.toString()); }
        catch (IOException e) { System.err.println("Error saving token: " + e.getMessage()); }
    }

    public List<MealToken> getStudentTokens(String username) {
        ensureFile(TOKEN_FILE);
        List<MealToken> studentTokens = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(TOKEN_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                MealToken token = MealToken.fromString(line);
                if (token.getStudentId().equals(username)) studentTokens.add(token);
            }
        } catch (IOException e) { System.err.println("Token read error: " + e.getMessage()); }
        return studentTokens;
    }

    public StudentBalance loadStudentBalance(String username) { return loadBalance(username); }

    private double getPriceForMeal(MealType type) {
        return switch (type) {
            case BREAKFAST -> 30.0;
            case LUNCH, DINNER -> 60.0;
            case SUHOOR -> 40.0;
            case IFTAR -> 50.0;
            default -> 0.0;
        };
    }

    public void saveMenu(List<DailyMenu> weeklyMenu) {
        ensureFile(WEEKLY_MENU_FILE);
        try (PrintWriter pw = new PrintWriter(new FileWriter(WEEKLY_MENU_FILE))) {
            for (DailyMenu menu : weeklyMenu) pw.println(menu.toString());
        } catch (IOException e) { System.err.println("Error saving menu: " + e.getMessage()); }
    }

    public void setSystemMode(boolean isRamadan) {
        ensureFile(CONFIG_FILE);
        try (PrintWriter pw = new PrintWriter(new FileWriter(CONFIG_FILE))) {
            pw.println("RAMADAN=" + isRamadan);
        } catch (IOException e) { System.err.println("Error saving configuration: " + e.getMessage()); }
    }

    public String purchaseTokenForDay(String username, LocalDate day, MealType mealType) {
        if (hasActiveToken(username, mealType, day)) return "You already have an ACTIVE token for " + mealType + " on " + day;
        double price = getPriceForMeal(mealType);
        StudentBalance student = loadStudentBalance(username);
        if (student == null) return "Transaction Failed: User '" + username + "' not found.";
        if (!student.deductBalance(price)) return "Transaction Failed: Insufficient funds.";
        String uniqueID = "MT-" + day.getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        saveTokenToDatabase(new MealToken(uniqueID, username, mealType, day, TokenStatus.ACTIVE));
        return "Success! Token ID: " + uniqueID + " | Cost: " + price + " BDT";
    }

    public List<DailyMenu> getWeeklyMenu() {
        List<DailyMenu> list = new ArrayList<>();
        ensureFile(WEEKLY_MENU_FILE);
        try (BufferedReader br = new BufferedReader(new FileReader(WEEKLY_MENU_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(!line.trim().isEmpty()){
                    list.add(DailyMenu.fromString(line));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading menu: " + e.getMessage());
        }
        return list;
    }

    public void updateSingleMeal(String day, MealType type, String newItems) {
        List<DailyMenu> currentMenu = getWeeklyMenu();
        boolean isRamadan = TimeManager.isRamadanMode();
        boolean found = false;

        for (int i = 0; i < currentMenu.size(); i++) {
            DailyMenu m = currentMenu.get(i);
            if (m.getDay().equalsIgnoreCase(day) && m.getType() == type && m.isRamadan() == isRamadan) {
                currentMenu.set(i, new DailyMenu(day, type, isRamadan, newItems));
                found = true;
                break;
            }
        }

        if (!found) {
            currentMenu.add(new DailyMenu(day, type, isRamadan, newItems));
        }

        saveMenu(currentMenu);
    }
}