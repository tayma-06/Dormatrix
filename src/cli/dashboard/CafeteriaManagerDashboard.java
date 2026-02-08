package cli.dashboard;

import controllers.food.CafeteriaController;
import models.food.DailyMenu;
import models.food.MealType;
import utils.FastInput;
import java.util.ArrayList;
import java.util.List;

public class CafeteriaManagerDashboard implements Dashboard {
    private final CafeteriaController controller = new CafeteriaController();

    @Override
    public void show(String username) {
        while (true) {
            System.out.println("\n--- Cafeteria Manager Dashboard ---");
            System.out.println("1. Update Weekly Menu");
            System.out.println("2. Toggle Ramadan Mode");
            System.out.println("3. Toggle Feast/Exam Mode");
            System.out.println("0. Logout");
            System.out.print("Choice: ");

            int choice = FastInput.readInt();
            if (choice == 0) break;

            switch (choice) {
                case 1 -> updateWeeklyMenu();
                case 2 -> {
                    System.out.print("Enable Ramadan Mode? (true/false): ");
                    controller.setSystemMode(FastInput.readBoolean());
                }
                default -> System.out.println("Feature coming soon!");
            }
        }
    }

    private void updateWeeklyMenu() {
        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
        MealType[] meals = {MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER};
        List<DailyMenu> newMenu = new ArrayList<>();

        for (String day : days) {
            System.out.println("\nSetting menu for " + day + ":");
            for (MealType meal : meals) {
                System.out.print("Enter items for " + meal + ": ");
                String items = FastInput.readNonEmptyLine();
                newMenu.add(new DailyMenu(day, meal, items));
            }
        }
        controller.saveMenu(newMenu);
        System.out.println("Weekly menu updated successfully!");
    }
}