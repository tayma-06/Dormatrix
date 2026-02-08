import java.io.*;
import java.nio.file.*;

public class MainSeeder {

    public static void main(String[] args) {
        System.out.println("--- DORMITORY SYSTEM DATA SEEDER ---");

        // 3. Financials
        // Format: id,balance
        seedFile("data/inventories/balances.txt",
                "200041101,500.0\n200041102,1250.5");

        // 4. Cafeteria Configuration & Menus
        // Format: DAY|MEALTYPE|ITEMS
        seedFile("data/foods/weekly_menu.txt",
                "MONDAY|LUNCH|Rice, Chicken Curry, Dal\n" +
                        "MONDAY|DINNER|Rice, Fish Fry, Veg\n" +
                        "TUESDAY|LUNCH|Rice, Beef Bhuna, Salad\n" +
                        "TUESDAY|DINNER|Rice, Egg Curry, Spinach");

        // Format: YYYY-MM-DD|MEALTYPE|ITEMS
        seedFile("data/foods/calendar_schedule.txt",
                "2026-02-14|LUNCH|Special Feast Biryani & Borhani");

        seedFile("data/foods/config.txt", "RAMADAN=false");
    }

    private static void createFolders() {
        String[] paths = {"data/users", "data/foods", "data/inventories"};
        for (String p : paths) {
            File dir = new File(p);
            if (!dir.exists()) {
                dir.mkdirs();
                System.out.println("Created directory: " + p);
            }
        }
    }

    private static void seedFile(String fileName, String content) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println(content);
            System.out.println("Seeded: " + fileName);
        } catch (IOException e) {
            System.err.println("Error seeding " + fileName + ": " + e.getMessage());
        }
    }
}