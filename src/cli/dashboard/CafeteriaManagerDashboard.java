package cli.dashboard;

import controllers.food.*;
import models.food.DailyMenu;
import models.food.MealType;
import utils.FastInput;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CafeteriaManagerDashboard implements Dashboard {
    private final CafeteriaController controller = new CafeteriaController();
    private final MealTokenController tokenController = new MealTokenController();

    @Override
    public void show(String username) {
        while (true) {
            System.out.println("\n--- CAFETERIA MANAGER PANEL ---");
            System.out.println("1. Update Weekly Menu (General)");
            System.out.println("2. Schedule Special Event (Calendar)");
            System.out.println("3. Verify Student Token");
            System.out.println("4. Toggle Ramadan Mode");
            System.out.println("0. Logout");
            System.out.print("Selection: ");

            int choice = FastInput.readInt();
            if (choice == 0) break;

            switch (choice) {
                case 1 -> updateWeeklyMenu();
                case 2 -> scheduleCalendar();
                case 3 -> verifyToken();
                case 4 -> toggleRamadan();
                default -> System.out.println("Invalid Selection!");
            }
        }
    }

    private void updateWeeklyMenu() {
        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
        MealType[] types = {MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER};
        List<DailyMenu> newMenu = new ArrayList<>();

        System.out.println("\n--- Set Weekly Menu ---");
        for (String day : days) {
            System.out.println(">> " + day);
            for (MealType type : types) {
                System.out.print("   " + type + " items: ");
                String items = FastInput.readNonEmptyLine();
                newMenu.add(new DailyMenu(day, type, items));
            }
        }
        controller.saveMenu(newMenu);
        System.out.println("Success: Weekly menu updated!");
    }

    private void scheduleCalendar() {
        try {
            System.out.print("Enter Date (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(FastInput.readNonEmptyLine());

            System.out.println("Select Meal Type: 1.LUNCH 2.DINNER 3.FEAST");
            int typeChoice = FastInput.readInt();
            MealType type = switch (typeChoice) {
                case 1 -> MealType.LUNCH;
                case 2 -> MealType.DINNER;
                default -> MealType.LUNCH;
            };

            System.out.print("Enter Special Menu Items: ");
            String items = FastInput.readNonEmptyLine();

            controller.scheduleSpecialMeal(date, type, items);
            System.out.println("Success: Event added to calendar.");
        } catch (DateTimeParseException e) {
            System.out.println("Error: Invalid date format.");
        }
    }

    private void verifyToken() {
        System.out.print("Enter Token ID: ");
        String id = FastInput.readNonEmptyLine();
        System.out.println(">> " + tokenController.verifyAndUseToken(id));
    }

    private void toggleRamadan() {
        System.out.print("Enable Ramadan Mode? (true/false): ");
        boolean isRamadan = FastInput.readBoolean();
        controller.setSystemMode(isRamadan);
        System.out.println("System Mode updated.");
    }
}