package controllers.food;

import models.food.MealType;
import models.store.StudentBalance;
import java.time.LocalDate;

public class TokenPurchaseController {

    private final CafeteriaController cafeteriaData = new CafeteriaController();
    private final TokenPurchaseService purchaseService = TokenPurchaseService.getInstance();

    public String getMenuForTime(LocalDate date, String dayOfWeek, MealType type) {
        return cafeteriaData.getMenuForTime(date, dayOfWeek, type);
    }

    public StudentBalance getStudentBalance(String username) {
        return cafeteriaData.loadStudentBalance(username);
    }

    public String processTokenPurchase(String username) {
        return purchaseService.purchaseToken(username);
    }

    public String processTokenPurchaseForDay(String username, LocalDate date, MealType mealType) {
        return purchaseService.purchaseTokenForDay(username, date, mealType);
    }
}
