package controllers.routine;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import models.routine.StudentRoutineEntry;
import models.users.StudentPublicInfo;
import repo.file.FileStudentDirectoryRepository;
import repo.file.FileStudentRoutineRepository;
import utils.TimeManager;

import java.time.DayOfWeek;

public class RoutineController {

    public static final String[] FULL_SLOT_LABELS = {
            "00-02", "02-04", "04-06", "06-08",
            "08-10", "10-12", "12-14", "14-16",
            "16-18", "18-20", "20-22", "22-24"
    };

    public static final String[] ATTENDANT_SLOT_LABELS = {
            "08-10", "10-12", "12-14", "14-16", "16-18", "18-20"
    };

    private static final int TIME_COL_WIDTH = 10;
    private static final int CELL_WIDTH = 12;

    private final FileStudentRoutineRepository routineRepo = new FileStudentRoutineRepository();
    private final FileStudentDirectoryRepository studentRepo = new FileStudentDirectoryRepository();

    public MyOptional<String> resolveStudentId(String dashboardToken) {
        return studentRepo.resolveStudentId(dashboardToken);
    }

    public MyArrayList<StudentPublicInfo> findStudentsByRoom(String roomNumber) {
        return studentRepo.findByRoom(roomNumber);
    }

    public MyOptional<StudentPublicInfo> findStudentPublicInfo(String studentId) {
        return studentRepo.findPublicInfoById(studentId);
    }

    public boolean putSlot(String dashboardToken, DayOfWeek day, int fullSlotIndex, String content) {
        MyOptional<String> studentIdOpt = resolveStudentId(dashboardToken);
        if (studentIdOpt.isEmpty()) return false;
        return putSlotByStudentId(studentIdOpt.get(), day, fullSlotIndex, content);
    }

    public boolean putSlotByStudentId(String studentId, DayOfWeek day, int fullSlotIndex, String content) {
        if (!isValidFullSlot(fullSlotIndex)) return false;

        if (content == null || content.trim().isEmpty()) {
            routineRepo.deleteSlot(studentId, day, fullSlotIndex);
            return true;
        }

        routineRepo.upsert(new StudentRoutineEntry(studentId, day, fullSlotIndex, content.trim()));
        return true;
    }

    public boolean clearSlot(String dashboardToken, DayOfWeek day, int fullSlotIndex) {
        MyOptional<String> studentIdOpt = resolveStudentId(dashboardToken);
        if (studentIdOpt.isEmpty()) return false;
        routineRepo.deleteSlot(studentIdOpt.get(), day, fullSlotIndex);
        return true;
    }

    public boolean clearSlotByStudentId(String studentId, DayOfWeek day, int fullSlotIndex) {
        if (!isValidFullSlot(fullSlotIndex)) return false;
        routineRepo.deleteSlot(studentId, day, fullSlotIndex);
        return true;
    }

    public String renderStudentRoutine(String dashboardToken) {
        MyOptional<String> studentIdOpt = resolveStudentId(dashboardToken);
        if (studentIdOpt.isEmpty()) {
            return "Could not resolve student identity for routine view.";
        }
        return renderStudentRoutineByStudentId(studentIdOpt.get());
    }

    public String renderStudentRoutineByStudentId(String studentId) {
        String[][] cells = new String[12][7];
        fillCells(cells, studentId, true);

        String title = "STUDENT ROUTINE (24 HOURS)";
        MyOptional<StudentPublicInfo> infoOpt = studentRepo.findPublicInfoById(studentId);
        if (infoOpt.isPresent()) {
            StudentPublicInfo info = infoOpt.get();
            title += " - " + info.getName() + " / Room " + info.getRoomNo();
        }

        return buildTable(title, FULL_SLOT_LABELS, cells, true);
    }

    public String renderMaskedRoutineForStudent(String studentId) {
        String[][] cells = new String[6][7];
        fillAttendantCells(cells, studentId);

        String title = "STUDENT ROUTINE (MASKED 08-20)";
        MyOptional<StudentPublicInfo> infoOpt = studentRepo.findPublicInfoById(studentId);
        if (infoOpt.isPresent()) {
            StudentPublicInfo info = infoOpt.get();
            title += " - " + info.getName() + " / Room " + info.getRoomNo();
        }

        return buildTable(title, ATTENDANT_SLOT_LABELS, cells, false);
    }

    public boolean hasExplicitEntry(String studentId, DayOfWeek day, int fullSlotIndex) {
        MyOptional<StudentRoutineEntry> entry = routineRepo.findOne(studentId, day, fullSlotIndex);
        return entry.isPresent() && entry.get().hasContent();
    }

    public boolean isBusyForAttendantWindow(String studentId, DayOfWeek day, int attendantSlotIndex) {
        int fullSlotIndex = attendantSlotIndex + 4;
        return hasExplicitEntry(studentId, day, fullSlotIndex);
    }

    public boolean isPrivateByDefaultNightSlot(int fullSlotIndex) {
        return fullSlotIndex == 0 || fullSlotIndex == 1 || fullSlotIndex == 10 || fullSlotIndex == 11;
    }

    public boolean isStudentBusy24(String studentId, DayOfWeek day, int fullSlotIndex) {
        if (hasExplicitEntry(studentId, day, fullSlotIndex)) return true;
        return isPrivateByDefaultNightSlot(fullSlotIndex);
    }

    private void fillCells(String[][] cells, String studentId, boolean studentView) {
        MyArrayList<StudentRoutineEntry> entries = routineRepo.findByStudentId(studentId);

        for (int r = 0; r < cells.length; r++) {
            for (int c = 0; c < cells[r].length; c++) {
                cells[r][c] = "";
            }
        }

        for (int i = 0; i < entries.size(); i++) {
            StudentRoutineEntry entry = entries.get(i);
            int row = entry.getSlotIndex();
            int col = dayToColumn(entry.getDay());

            if (row >= 0 && row < cells.length && col >= 0 && col < 7) {
                cells[row][col] = studentView ? entry.getContent() : "BUSY";
            }
        }
    }

    private void fillAttendantCells(String[][] cells, String studentId) {
        for (int r = 0; r < cells.length; r++) {
            for (int c = 0; c < cells[r].length; c++) {
                DayOfWeek day = columnToDay(c);
                boolean busy = isBusyForAttendantWindow(studentId, day, r);
                cells[r][c] = busy ? "BUSY" : "";
            }
        }
    }

    private String buildTable(String title, String[] rowLabels, String[][] cells, boolean showRealContents) {
        StringBuilder sb = new StringBuilder();

        sb.append(title).append("\n");
        sb.append(buildBorder("┌", "┬", "┐")).append("\n");
        sb.append("│").append(center("TIME", TIME_COL_WIDTH));

        for (int i = 0; i < 7; i++) {
            DayOfWeek day = columnToDay(i);
            String label = dayShort(day);
            if (day == TimeManager.nowDay()) label = "*" + label + "*";
            sb.append("│").append(center(label, CELL_WIDTH));
        }

        sb.append("│\n");
        sb.append(buildBorder("├", "┼", "┤")).append("\n");

        for (int r = 0; r < rowLabels.length; r++) {
            sb.append("│").append(center(rowLabels[r], TIME_COL_WIDTH));
            for (int c = 0; c < 7; c++) {
                String cell = cells[r][c] == null ? "" : cells[r][c];
                sb.append("│").append(pad(clip(cell, CELL_WIDTH - 2), CELL_WIDTH));
            }
            sb.append("│\n");

            if (r < rowLabels.length - 1) {
                sb.append(buildBorder("├", "┼", "┤")).append("\n");
            }
        }

        sb.append(buildBorder("└", "┴", "┘")).append("\n");

        if (!showRealContents) {
            sb.append("Legend: BUSY = student already has something scheduled in that 2-hour slot.\n");
        } else {
            sb.append("Night privacy default for attendant scheduling: 20:00-04:00 is treated as private/busy.\n");
        }

        return sb.toString();
    }

    private String buildBorder(String left, String mid, String right) {
        StringBuilder sb = new StringBuilder();
        sb.append(left).append(repeat("─", TIME_COL_WIDTH));
        for (int i = 0; i < 7; i++) {
            sb.append(mid).append(repeat("─", CELL_WIDTH));
        }
        sb.append(right);
        return sb.toString();
    }

    private String dayShort(DayOfWeek day) {
        switch (day) {
            case MONDAY: return "MON";
            case TUESDAY: return "TUE";
            case WEDNESDAY: return "WED";
            case THURSDAY: return "THU";
            case FRIDAY: return "FRI";
            case SATURDAY: return "SAT";
            default: return "SUN";
        }
    }

    private int dayToColumn(DayOfWeek day) {
        switch (day) {
            case MONDAY: return 0;
            case TUESDAY: return 1;
            case WEDNESDAY: return 2;
            case THURSDAY: return 3;
            case FRIDAY: return 4;
            case SATURDAY: return 5;
            default: return 6;
        }
    }

    private DayOfWeek columnToDay(int column) {
        switch (column) {
            case 0: return DayOfWeek.MONDAY;
            case 1: return DayOfWeek.TUESDAY;
            case 2: return DayOfWeek.WEDNESDAY;
            case 3: return DayOfWeek.THURSDAY;
            case 4: return DayOfWeek.FRIDAY;
            case 5: return DayOfWeek.SATURDAY;
            default: return DayOfWeek.SUNDAY;
        }
    }

    private boolean isValidFullSlot(int fullSlotIndex) {
        return fullSlotIndex >= 0 && fullSlotIndex < FULL_SLOT_LABELS.length;
    }

    private String clip(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        if (max <= 1) return s.substring(0, max);
        return s.substring(0, max - 1) + "…";
    }

    private String center(String s, int width) {
        if (s == null) s = "";
        if (s.length() > width) return s.substring(0, width);

        int left = (width - s.length()) / 2;
        int right = width - s.length() - left;
        return repeat(" ", left) + s + repeat(" ", right);
    }

    private String pad(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        return s + repeat(" ", width - s.length());
    }

    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }
}