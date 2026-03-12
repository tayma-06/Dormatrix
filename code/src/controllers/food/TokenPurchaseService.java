package controllers.food;

import java.time.LocalDate;
import models.food.MealType;

public class TokenPurchaseService {

    private static TokenPurchaseService instance;
    private final CafeteriaController cafeteriaController;

    private TokenPurchaseService() {
        this.cafeteriaController = new CafeteriaController();
    }

    public static synchronized TokenPurchaseService getInstance() {
        if (instance == null) {
            instance = new TokenPurchaseService();
        }
        return instance;
    }

    public void start() {
        // No-op: multithreading removed
    }

    public String purchaseToken(String username) {
        return cafeteriaController.purchaseToken(username);
    }

    public String purchaseTokenForDay(String username, LocalDate date, MealType mealType) {
        return cafeteriaController.purchaseTokenForDay(username, date, mealType);
    }

    public void shutdown() {
        // No-op: multithreading removed
    }
}
