package utils;

import models.food.MealType;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class TimeManager {
    private static boolean isRamadanMode = false;

    public static MealType getCurrentMealSlot() {
        LocalTime now = LocalTime.now();
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        if (isRamadanMode) {
            return getRamadanSlot(now);
        }
        if (today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY) {
            if (isBetween(now, "07:00", "10:00")) return MealType.BREAKFAST;
        } else {
            if (isBetween(now, "07:00", "09:30")) return MealType.BREAKFAST;
        }
        if (isBetween(now, "12:00", "14:00")) return MealType.LUNCH;
        if (isBetween(now, "19:00", "22:00")) return MealType.DINNER;

        return MealType.NONE;
    }

    private static MealType getRamadanSlot(LocalTime now) {
        if (isBetween(now, "03:00", "04:30")) return MealType.SUHOOR;
        if (isBetween(now, "18:00", "19:15")) return MealType.IFTAR;
        if (isBetween(now, "19:30", "21:30")) return MealType.DINNER;
        return MealType.NONE;
    }

    private static boolean isBetween(LocalTime now, String start, String end) {
        return !now.isBefore(LocalTime.parse(start)) && !now.isAfter(LocalTime.parse(end));
    }

    public static void setRamadanMode(boolean active) {
        isRamadanMode = active;
    }
}