package controllers.food;

import models.food.MealType;
import models.store.StudentBalance;
import java.time.LocalDate;

public class TokenPurchaseController {
    private final CafeteriaController cafeteriaData = new CafeteriaController();
    public String getMenuForTime(LocalDate date, String dayOfWeek, MealType type) {
        return cafeteriaData.getMenuForTime(date, dayOfWeek, type);
    }
    public StudentBalance getStudentBalance(String username) {
        return cafeteriaData.loadStudentBalance(username);
    }
    public String processTokenPurchase(String username) {
        return cafeteriaData.purchaseToken(username);
    }
}