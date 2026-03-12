package controllers.food;

import models.food.DailyMenu;
import models.food.MealType;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MenuManagementController {
    private final CafeteriaController cafeteriaData = new CafeteriaController();

    public List<DailyMenu> getWeeklyMenuData() {
        return cafeteriaData.getWeeklyMenu();
    }
    public String processSingleMealUpdate(String day, MealType type, String items) {
        if (items == null || items.trim().isEmpty()) {
            return "[Error] Menu items cannot be empty.";
        }
        cafeteriaData.updateSingleMeal(day, type, items);
        return "[Success] Updated " + day + " " + type + " successfully.";
    }
    public String processSpecialEvent(String dateStr, int typeChoice, String items) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            MealType type = (typeChoice == 2) ? MealType.DINNER : MealType.LUNCH;

            if (items == null || items.trim().isEmpty()) {
                return "[Error] Special menu items cannot be empty.";
            }

            cafeteriaData.scheduleSpecialMeal(date, type, items);
            return "[Success] Special event scheduled for " + date + ".";
        } catch (DateTimeParseException e) {
            return "[Error] Invalid date format. Please use YYYY-MM-DD.";
        }
    }
}