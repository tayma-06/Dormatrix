package controllers.routine;

import models.routine.RoutineEntry;
import repo.file.FileRoutineRepository;
import utils.TimeManager;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class RoutineController {
    private static final DayOfWeek[] DAYS = {
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
    };

    private static final String[] DAY_LABELS = {
            "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"
    };

    private static final String[] SLOT_LABELS = {
            "08-10", "10-12", "12-14", "14-16", "16-18", "18-20"
    };

    private static final int TIME_WIDTH = 10;
    private static final int CELL_WIDTH = 12;
    private static final String MASK_FILLED = "████"; // change to "" if your console handles it well

    private final FileRoutineRepository repository = new FileRoutineRepository();

    public void setSlot(String studentId, int dayChoice, int slotChoice, String content) {
        validate(dayChoice, slotChoice);
        repository.upsert(studentId, DAYS[dayChoice - 1], slotChoice - 1, content);
    }

    public void clearSlot(String studentId, int dayChoice, int slotChoice) {
        setSlot(studentId, dayChoice, slotChoice, "");
    }

    public String getSlotContent(String studentId, int dayChoice, int slotChoice) {
        validate(dayChoice, slotChoice);
        return repository.getSlotContent(studentId, DAYS[dayChoice - 1], slotChoice - 1);
    }

    public void printStudentRoutine(String studentId) {
        printCalendar(studentId, false);
    }

    public void printMaskedRoutine(String studentId) {
        printCalendar(studentId, true);
        System.out.println("Legend: BUSY = slot has a student schedule, blank = no saved item.");
    }

    public void printDayChoices() {
        for (int i = 0; i < DAYS.length; i++) {
            System.out.println((i + 1) + ". " + DAYS[i].name());
        }
    }

    public void printSlotChoices() {
        for (int i = 0; i < SLOT_LABELS.length; i++) {
            System.out.println((i + 1) + ". " + SLOT_LABELS[i]);
        }
    }

    private void printCalendar(String studentId, boolean masked) {
        String[][] grid = buildGrid(studentId, masked);
        int currentDayIndex = TimeManager.nowDay().getValue() - 1;
        int currentSlotIndex = getCurrentSlotIndex();

        printBorder('┌', '┬', '┐');
        System.out.print("│" + padCenter("TIME", TIME_WIDTH));
        for (int day = 0; day < DAY_LABELS.length; day++) {
            String label = (day == currentDayIndex) ? ("*" + DAY_LABELS[day] + "*") : DAY_LABELS[day];
            System.out.print("│" + padCenter(label, CELL_WIDTH));
        }
        System.out.println("│");
        printBorder('├', '┼', '┤');

        for (int row = 0; row < SLOT_LABELS.length; row++) {
            String rowLabel = (row == currentSlotIndex) ? (">" + SLOT_LABELS[row]) : SLOT_LABELS[row];
            System.out.print("│" + padCenter(rowLabel, TIME_WIDTH));
            for (int col = 0; col < DAY_LABELS.length; col++) {
                System.out.print("│" + padCenter(trimToFit(grid[row][col], CELL_WIDTH), CELL_WIDTH));
            }
            System.out.println("│");
            if (row < SLOT_LABELS.length - 1) {
                printBorder('├', '┼', '┤');
            }
        }
        printBorder('└', '┴', '┘');
    }

    private String[][] buildGrid(String studentId, boolean masked) {
        String[][] grid = new String[SLOT_LABELS.length][DAY_LABELS.length];
        for (int row = 0; row < SLOT_LABELS.length; row++) {
            for (int col = 0; col < DAY_LABELS.length; col++) {
                grid[row][col] = "";
            }
        }

        List<RoutineEntry> entries = repository.findByStudent(studentId);
        for (RoutineEntry entry : entries) {
            int row = entry.getSlotIndex();
            int col = entry.getDay().getValue() - 1;
            if (row >= 0 && row < SLOT_LABELS.length && col >= 0 && col < DAY_LABELS.length) {
                grid[row][col] = masked ? MASK_FILLED : entry.getContent();
            }
        }

        return grid;
    }

    private int getCurrentSlotIndex() {
        LocalTime now = TimeManager.nowTime();
        LocalTime[] starts = {
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                LocalTime.of(14, 0),
                LocalTime.of(16, 0),
                LocalTime.of(18, 0)
        };
        LocalTime[] ends = {
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                LocalTime.of(14, 0),
                LocalTime.of(16, 0),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0)
        };

        for (int i = 0; i < starts.length; i++) {
            if (!now.isBefore(starts[i]) && now.isBefore(ends[i])) {
                return i;
            }
        }
        return -1;
    }

    private void validate(int dayChoice, int slotChoice) {
        if (dayChoice < 1 || dayChoice > DAYS.length) {
            throw new IllegalArgumentException("Day must be between 1 and 7.");
        }
        if (slotChoice < 1 || slotChoice > SLOT_LABELS.length) {
            throw new IllegalArgumentException("Slot must be between 1 and 6.");
        }
    }

    private void printBorder(char left, char middle, char right) {
        System.out.print(left);
        System.out.print(repeat('─', TIME_WIDTH));
        for (int i = 0; i < DAY_LABELS.length; i++) {
            System.out.print(middle);
            System.out.print(repeat('─', CELL_WIDTH));
        }
        System.out.println(right);
    }

    private String padCenter(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) return text.substring(0, width);
        int left = (width - text.length()) / 2;
        int right = width - text.length() - left;
        return repeat(' ', left) + text + repeat(' ', right);
    }

    private String trimToFit(String text, int width) {
        if (text == null) return "";
        if (text.length() <= width) return text;
        if (width <= 1) return text.substring(0, width);
        return text.substring(0, width - 1) + "…";
    }

    private String repeat(char ch, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }
}