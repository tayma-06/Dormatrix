package models.food;

import java.io.Serializable;

public class DailyMenu implements Serializable {
    private String day;
    private MealType type;
    private String items;
    private boolean isRamadan;

    public DailyMenu(String day, MealType type, boolean isRamadan, String items) {
        this.day = day;
        this.type = type;
        this.items = items;
        this.isRamadan = isRamadan;
    }

    public String getDay() { return day; }
    public MealType getType() { return type; }
    public String getItems() { return items; }
    public boolean isRamadan() { return isRamadan; }

    @Override
    public String toString() {
        return day + "|" + type + "|" + isRamadan + "|" + items;
    }

    public static DailyMenu fromString(String line) {
        String[] parts = line.split("\\|", 4);
        if (parts.length < 3) return null;

        boolean isRam = Boolean.parseBoolean(parts[2]);
        String items = (parts.length > 3) ? parts[3] : "";

        return new DailyMenu(parts[0], MealType.valueOf(parts[1]), isRam, items);
    }
}