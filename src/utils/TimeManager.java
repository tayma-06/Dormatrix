package utils;

import models.food.MealType;

import java.time.*;

public class TimeManager {
    private static boolean isRamadanMode = false;
    private static boolean demoMode = false;
    private static final LocalTime DEMO_START_TIME = LocalTime.of(7, 0);
    private static final LocalTime DEMO_END_TIME = LocalTime.of(22, 0);
    private static final long DEMO_SIM_SECONDS_TOTAL =
            Duration.between(DEMO_START_TIME, DEMO_END_TIME).getSeconds(); // 54000
    private static final long DEMO_REAL_SECONDS_TOTAL = 60;
    private static final long DEMO_SIM_SECONDS_PER_REAL_SECOND =
            DEMO_SIM_SECONDS_TOTAL / DEMO_REAL_SECONDS_TOTAL; // 900

    private static long demoStartRealMillis = 0;
    private static LocalDate demoDate = LocalDate.now(); // Keep the same day in demo

    public static void setDemoMode(boolean active) {
        demoMode = active;
        if (demoMode) {
            demoStartRealMillis = System.currentTimeMillis();
            demoDate = LocalDate.now();
        }
    }

    public static boolean isDemoMode() {
        return demoMode;
    }

    public static void setRamadanMode(boolean active) {
        isRamadanMode = active;
    }

    public static boolean isRamadanMode() {
        return isRamadanMode;
    }

    public static LocalTime nowTime() {
        if (!demoMode) return LocalTime.now();

        long elapsedRealSec = (System.currentTimeMillis() - demoStartRealMillis) / 1000;
        long simSec = elapsedRealSec * DEMO_SIM_SECONDS_PER_REAL_SECOND;
        simSec = simSec % DEMO_SIM_SECONDS_TOTAL;
        return DEMO_START_TIME.plusSeconds(simSec);
    }

    public static LocalDate nowDate() {
        return demoMode ? demoDate : LocalDate.now();
    }

    public static DayOfWeek nowDay() {
        return nowDate().getDayOfWeek();
    }

    public static MealType getCurrentMealSlot() {
        LocalTime now = nowTime();
        DayOfWeek today = nowDay();

        if (isRamadanMode) {
            return getRamadanSlot(now);
        }

        if (isBetween(now, "07:00", "09:00")) return MealType.BREAKFAST;
        if (isBetween(now, "12:00", "14:00")) return MealType.LUNCH;
        if (isBetween(now, "19:00", "21:00")) return MealType.DINNER;

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
}
