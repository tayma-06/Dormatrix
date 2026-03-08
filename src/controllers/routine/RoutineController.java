package controllers.routine;

import models.routine.RoutineEntry;
import repo.file.FileRoutineRepository;
import utils.ConsoleColors;
import utils.TerminalUI;
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

    /**
     * Returns the total number of terminal lines the routine table occupies.
     */
    public int tableHeight() {
        // top border + header + sep + (data row + sep) * slots - 1 last sep + bottom
        // = 1 + 1 + 1 + SLOT_LABELS.length * 2 - 1 + 1 = 3 + len*2
        return 3 + SLOT_LABELS.length * 2;
    }

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

        String box = TerminalUI.getActiveBoxColor();
        String txt = TerminalUI.getActiveTextColor();
        String hi = ConsoleColors.Accent.HIGHLIGHT;   // current day/slot accent
        String dim = ConsoleColors.Accent.MUTED;
        String BG = TerminalUI.getActiveBgColor();
        String R = ConsoleColors.RESET;
        String BOLD = ConsoleColors.BOLD;

        // Column widths (visible chars only)
        int tw = TIME_WIDTH;   // 10
        int cw = CELL_WIDTH;   // 12
        int cols = DAY_LABELS.length; // 7

        // Total visual width: │ + tw + (│ + cw) * cols + │
        int tableW = 1 + tw + cols * (1 + cw) + 1;
        int leftCol = TerminalUI.centerCol(tableW);

        // Helper to emit one full table row at the centred column
        // We use \u001B[col;G (column absolute) per line
        java.util.function.Consumer<String> row = line -> {
            System.out.print("\u001B[" + leftCol + "G" + line + R + "\n");
            System.out.flush();
        };

        // ── top border ─────────────────────────────────────────────
        row.accept(buildHBorder('┌', '┬', '┐', tw, cw, cols, BG + box));

        // ── header row ─────────────────────────────────────────────
        StringBuilder hdr = new StringBuilder(BG + box + "│" + R);
        hdr.append(BG + BOLD + txt + padCenter("TIME", tw) + R);
        for (int d = 0; d < cols; d++) {
            boolean today = (d == currentDayIndex);
            String lbl = today ? ("*" + DAY_LABELS[d] + "*") : DAY_LABELS[d];
            String color = today ? BG + BOLD + hi : BG + txt;
            hdr.append(BG + box + "│" + R + color + padCenter(lbl, cw) + R);
        }
        hdr.append(BG + box + "│" + R);
        row.accept(hdr.toString());

        // ── separator ──────────────────────────────────────────────
        row.accept(buildHBorder('├', '┼', '┤', tw, cw, cols, BG + box));

        // ── data rows ──────────────────────────────────────────────
        for (int r = 0; r < SLOT_LABELS.length; r++) {
            boolean activeSlot = (r == currentSlotIndex);
            String slotLabel = activeSlot ? (">" + SLOT_LABELS[r]) : SLOT_LABELS[r];
            String slotColor = activeSlot ? BG + BOLD + hi : BG + dim;

            StringBuilder dataRow = new StringBuilder(BG + box + "│" + R);
            dataRow.append(slotColor + padCenter(slotLabel, tw) + R);
            for (int d = 0; d < cols; d++) {
                String cell = trimToFit(grid[r][d], cw);
                boolean todayCol = (d == currentDayIndex);
                String cellColor = todayCol ? BG + txt : BG + dim;
                dataRow.append(BG + box + "│" + R + cellColor + padCenter(cell, cw) + R);
            }
            dataRow.append(BG + box + "│" + R);
            row.accept(dataRow.toString());

            if (r < SLOT_LABELS.length - 1) {
                row.accept(buildHBorder('├', '┼', '┤', tw, cw, cols, BG + box));
            }
        }

        // ── bottom border ──────────────────────────────────────────
        row.accept(buildHBorder('└', '┴', '┘', tw, cw, cols, BG + box));
    }

    /**
     * Builds a horizontal border line: left + ─*tw + (mid + ─*cw)*cols + right
     */
    private static String buildHBorder(char left, char mid, char right,
            int tw, int cw, int cols, String color) {
        StringBuilder sb = new StringBuilder(color);
        sb.append(left).append("─".repeat(tw));
        for (int i = 0; i < cols; i++) {
            sb.append(mid).append("─".repeat(cw));
        }
        sb.append(right);
        return sb.toString();
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

    private String padCenter(String text, int width) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int left = (width - text.length()) / 2;
        int right = width - text.length() - left;
        return repeat(' ', left) + text + repeat(' ', right);
    }

    private String trimToFit(String text, int width) {
        if (text == null) {
            return "";
        }
        if (text.length() <= width) {
            return text;
        }
        if (width <= 1) {
            return text.substring(0, width);
        }
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
