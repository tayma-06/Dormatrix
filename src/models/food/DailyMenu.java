package models.food;

import java.io.Serializable;

public class DailyMenu implements Serializable {
    private String day; // Monday, Tuesday...
    private MealType type;
    private String items; // e.g., "Chicken Biryani, Salad, Coke"

    public DailyMenu(String day, MealType type, String items) {
        this.day = day;
        this.type = type;
        this.items = items;
    }

    @Override
    public String toString() {
        return day + "|" + type + "|" + items;
    }

    public static DailyMenu fromString(String line) {
        String[] parts = line.split("\\|");
        return new DailyMenu(parts[0], MealType.valueOf(parts[1]), parts[2]);
    }

    // Getters
    public String getItems() { return items; }
}