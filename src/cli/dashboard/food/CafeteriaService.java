package cli.dashboard.food;

import controllers.food.CafeteriaController;
import models.food.DailyMenu;
import models.food.MealType;
import utils.FastInput;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CafeteriaService {
    private final CafeteriaController controller = new CafeteriaController();

    public void updateWeeklyMenu() {
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

    public void scheduleSpecialEvent() {
        try {
            System.out.print("Enter Date (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(FastInput.readNonEmptyLine());

            System.out.println("Select Meal Type: 1.LUNCH 2.DINNER");
            System.out.print("Choice: ");
            int typeChoice = FastInput.readInt();
            MealType type = (typeChoice == 2) ? MealType.DINNER : MealType.LUNCH;

            System.out.print("Enter Special Menu Items: ");
            String items = FastInput.readNonEmptyLine();

            controller.scheduleSpecialMeal(date, type, items);
            System.out.println("Success: Event added to calendar.");
        } catch (DateTimeParseException e) {
            System.out.println("Error: Invalid date format.");
        }
    }
}