package models.food;

import java.io.Serializable;

public class DailyMenu implements Serializable {
    private String day;
    private MealType type;
    private String items;

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
        String[] parts = line.split("\\|", 3);
        return new DailyMenu(parts[0], MealType.valueOf(parts[1]), parts.length > 2 ? parts[2] : "");
    }

    public String getItems() { return items; }
}
