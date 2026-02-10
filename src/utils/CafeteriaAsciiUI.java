package utils;

import models.food.MealType;

import java.time.*;

public class CafeteriaAsciiUI {

    public static String renderSlotProgress(MealType slot) {
        LocalTime now = TimeManager.nowTime();
        DayOfWeek day = TimeManager.nowDay();

        TimeWindow w = getWindow(slot, day, TimeManager.isRamadanMode());

        if (w == null) {
            return "Status Bar: [ CLOSED ]";
        }

        if (now.isBefore(w.start) || now.isAfter(w.end)) {
            return "Status Bar: [ Not in " + slot + " window ]";
        }

        long totalSec = Duration.between(w.start, w.end).getSeconds();
        long usedSec  = Duration.between(w.start, now).getSeconds();
        long leftSec  = Duration.between(now, w.end).getSeconds();

        int width = 28;
        int filled = (int) Math.round((usedSec * 1.0 / totalSec) * width);
        filled = Math.max(0, Math.min(width, filled));

        String bar = "█".repeat(filled) + "░".repeat(width - filled);

        long minLeft = leftSec / 60;
        long secLeft = leftSec % 60;

        return String.format("%s %s-%s [%s] %dm %ds left",
                slot, w.start, w.end, bar, minLeft, secLeft);
    }

    private static TimeWindow getWindow(MealType slot, DayOfWeek day, boolean ramadan) {
        if (slot == MealType.NONE) return null;

        if (ramadan) {
            return switch (slot) {
                case SUHOOR -> new TimeWindow(LocalTime.parse("03:00"), LocalTime.parse("04:30"));
                case IFTAR  -> new TimeWindow(LocalTime.parse("18:00"), LocalTime.parse("19:15"));
                case DINNER -> new TimeWindow(LocalTime.parse("19:30"), LocalTime.parse("21:30"));
                default -> null;
            };
        }

        if (slot == MealType.BREAKFAST) {
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                return new TimeWindow(LocalTime.parse("07:00"), LocalTime.parse("10:00"));
            }
            return new TimeWindow(LocalTime.parse("07:00"), LocalTime.parse("09:30"));
        }

        return switch (slot) {
            case LUNCH -> new TimeWindow(LocalTime.parse("12:00"), LocalTime.parse("14:00"));
            case DINNER -> new TimeWindow(LocalTime.parse("19:00"), LocalTime.parse("22:00"));
            default -> null;
        };
    }

    private static class TimeWindow {
        LocalTime start;
        LocalTime end;

        TimeWindow(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }
    }
}
