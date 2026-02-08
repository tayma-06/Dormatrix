package controllers.food;

import models.food.DailyMenu;
import models.food.MealType;
import models.store.StudentBalance;

import java.io.*;
import java.util.*;

public class CafeteriaController {
    private final String MENU_FILE = "data/foods/weekly_menu.txt";
    private final String CONFIG_FILE = "data/foods/config.txt";

    public void saveMenu(List<DailyMenu> weeklyMenu) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(MENU_FILE))) {
            for (DailyMenu menu : weeklyMenu) {
                pw.println(menu.toString());
            }
        } catch (IOException e) {
            System.out.println("Error saving menu: " + e.getMessage());
        }
    }

    public String getTodaysMenu(String day, MealType type) {
        try (BufferedReader br = new BufferedReader(new FileReader(MENU_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                DailyMenu menu = DailyMenu.fromString(line);
                if (menu.toString().startsWith(day + "|" + type)) {
                    return menu.getItems();
                }
            }
        } catch (IOException e) {
            return "Menu not set for today.";
        }
        return "No menu available.";
    }

    public void setSystemMode(boolean isRamadan) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CONFIG_FILE))) {
            pw.println("RAMADAN=" + isRamadan);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String purchaseToken(String username) {
        // 1. Determine active meal and price
        MealType currentSlot = utils.TimeManager.getCurrentMealSlot();
        if (currentSlot == MealType.NONE) {
            return "Transaction Failed: Cafeteria is currently closed.";
        }

        double price = getPriceForMeal(currentSlot);

        // 2. Load Student Balance
        models.store.StudentBalance student = loadBalance(username);
        if (student == null) {
            return "Transaction Failed: User '" + username + "' not found in balances.txt.";
        }

        // 3. Check and Deduct Balance
        if (!student.deductBalance(price)) {
            return "Transaction Failed: Insufficient funds. Required: " + price + " BDT.";
        }

        // 4. Generate Unique Token ID (MT + Year + 6 unique chars)
        String uniqueID = "MT-" + java.time.LocalDate.now().getYear() + "-" +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // 5. Save everything
        updateBalanceFile(student);
        saveTokenToDatabase(new models.food.MealToken(
                uniqueID, username, currentSlot, java.time.LocalDate.now(), false
        ));

        return "Success! Token ID: " + uniqueID + " | Meal: " + currentSlot + " | Cost: " + price + " BDT";
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

    private models.store.StudentBalance loadBalance(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader("data/inventories/balances.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(username + ",")) {
                    return models.store.StudentBalance.fromString(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading balances: " + e.getMessage());
        }
        return null;
    }

    private void updateBalanceFile(models.store.StudentBalance updatedStudent) {
        List<String> lines = new ArrayList<>();
        try {
            File file = new File("data/inventories/balances.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(updatedStudent.getStudentId() + ",")) {
                    lines.add(updatedStudent.toString()); // Add the updated version
                } else {
                    lines.add(line);
                }
            }
            br.close();

            PrintWriter pw = new PrintWriter(new FileWriter(file));
            for (String l : lines) pw.println(l);
            pw.close();
        } catch (IOException e) {
            System.out.println("Critical Error updating balance file!");
        }
    }

    private void saveTokenToDatabase(models.food.MealToken token) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("data/foods/tokens.txt", true))) {
            pw.println(token.toString());
        } catch (IOException e) {
            System.out.println("Error saving token: " + e.getMessage());
        }
    }
    public List<models.food.MealToken> getStudentTokens(String username) {
        List<models.food.MealToken> studentTokens = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("data/foods/tokens.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                models.food.MealToken token = models.food.MealToken.fromString(line);
                // Show only tokens belonging to this student that aren't expired
                if (token.getStudentId().equals(username)) {
                    studentTokens.add(token);
                }
            }
        } catch (IOException e) {
            // If file doesn't exist yet, return empty list
        }
        return studentTokens;
    }

    public StudentBalance loadStudentBalance(String username) {
        return loadBalance(username);
    }
}