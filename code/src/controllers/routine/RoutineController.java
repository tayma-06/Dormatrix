package controllers.routine;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import models.routine.StudentRoutineEntry;
import models.users.Student;
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
        if (studentIdOpt.isEmpty()) {
            return false;
        }
        return putSlotByStudentId(studentIdOpt.get(), day, fullSlotIndex, content);
    }

    public boolean putSlotByStudentId(String studentId, DayOfWeek day, int fullSlotIndex, String content) {
        if (!isValidFullSlot(fullSlotIndex)) {
            return false;
        }

        if (content == null || content.trim().isEmpty()) {
            routineRepo.deleteSlot(studentId, day, fullSlotIndex);
            return true;
        }

        routineRepo.upsert(new StudentRoutineEntry(studentId, day, fullSlotIndex, content.trim()));
        return true;
    }

    public boolean clearSlot(String dashboardToken, DayOfWeek day, int fullSlotIndex) {
        MyOptional<String> studentIdOpt = resolveStudentId(dashboardToken);
        if (studentIdOpt.isEmpty()) {
            return false;
        }
        routineRepo.deleteSlot(studentIdOpt.get(), day, fullSlotIndex);
        return true;
    }

    public boolean clearSlotByStudentId(String studentId, DayOfWeek day, int fullSlotIndex) {
        if (!isValidFullSlot(fullSlotIndex)) {
            return false;
        }
        routineRepo.deleteSlot(studentId, day, fullSlotIndex);
        return true;
    }

    public boolean writeComplaintVisit(String studentId, DayOfWeek day, int attendantSlotIndex, String complaintId, String label) {
        if (attendantSlotIndex < 0 || attendantSlotIndex >= ATTENDANT_SLOT_LABELS.length) {
            return false;
        }

        int fullSlotIndex = attendantToFull(attendantSlotIndex);
        String safeLabel = (label == null || label.trim().isEmpty()) ? "Complaint Visit" : label.trim();
        String content = safeLabel + " " + complaintVisitToken(complaintId);
        return putSlotByStudentId(studentId, day, fullSlotIndex, content);
    }

    public boolean clearComplaintVisitIfPresent(String studentId, DayOfWeek day, int attendantSlotIndex, String complaintId) {
        if (attendantSlotIndex < 0 || attendantSlotIndex >= ATTENDANT_SLOT_LABELS.length) {
            return false;
        }

        int fullSlotIndex = attendantToFull(attendantSlotIndex);
        MyOptional<StudentRoutineEntry> entryOpt = routineRepo.findOne(studentId, day, fullSlotIndex);

        if (entryOpt.isEmpty()) {
            return false;
        }

        StudentRoutineEntry entry = entryOpt.get();
        String content = entry.getContent();
        if (content == null) {
            return false;
        }

        if (complaintId == null || complaintId.trim().isEmpty()) {
            routineRepo.deleteSlot(studentId, day, fullSlotIndex);
            return true;
        }

        if (content.contains(complaintVisitToken(complaintId))) {
            routineRepo.deleteSlot(studentId, day, fullSlotIndex);
            return true;
        }

        return false;
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

        return buildTable(title, ATTENDANT_SLOT_LABELS, cells, false) + buildStudentContactBlock(studentId);
    }

    public boolean hasExplicitEntry(String studentId, DayOfWeek day, int fullSlotIndex) {
        MyOptional<StudentRoutineEntry> entry = routineRepo.findOne(studentId, day, fullSlotIndex);
        return entry.isPresent() && entry.get().hasContent();
    }

    public boolean isBusyForAttendantWindow(String studentId, DayOfWeek day, int attendantSlotIndex) {
        int fullSlotIndex = attendantToFull(attendantSlotIndex);
        return hasExplicitEntry(studentId, day, fullSlotIndex);
    }

    public boolean isBusyForAttendantWindowExceptComplaint(String studentId,
            DayOfWeek day,
            int attendantSlotIndex,
            String complaintId) {
        if (attendantSlotIndex < 0 || attendantSlotIndex >= ATTENDANT_SLOT_LABELS.length) {
            return true;
        }

        int fullSlotIndex = attendantToFull(attendantSlotIndex);
        MyOptional<StudentRoutineEntry> entryOpt = routineRepo.findOne(studentId, day, fullSlotIndex);

        if (entryOpt.isEmpty()) {
            return false;
        }

        StudentRoutineEntry entry = entryOpt.get();
        if (!entry.hasContent()) {
            return false;
        }

        if (complaintId != null && entry.getContent() != null
                && entry.getContent().contains(complaintVisitToken(complaintId))) {
            return false;
        }

        return true;
    }

    public boolean isPrivateByDefaultNightSlot(int fullSlotIndex) {
        return fullSlotIndex == 0 || fullSlotIndex == 1 || fullSlotIndex == 10 || fullSlotIndex == 11;
    }

    public boolean isStudentBusy24(String studentId, DayOfWeek day, int fullSlotIndex) {
        if (hasExplicitEntry(studentId, day, fullSlotIndex)) {
            return true;
        }
        return isPrivateByDefaultNightSlot(fullSlotIndex);
    }

    public String renderStudentRoutineWithHighlight(String studentId, int hlRow, int hlCol) {
        String[][] cells = new String[12][7];
        fillCells(cells, studentId, true);

        String title = "STUDENT ROUTINE (24 HOURS)";
        MyOptional<StudentPublicInfo> infoOpt = studentRepo.findPublicInfoById(studentId);
        if (infoOpt.isPresent()) {
            StudentPublicInfo info = infoOpt.get();
            title += " - " + info.getName() + " / Room " + info.getRoomNo();
        }
        return buildTableWithHighlight(title, FULL_SLOT_LABELS, cells, hlRow, hlCol);
    }

    private String buildTableWithHighlight(String title, String[] rowLabels,
                                           String[][] cells, int hlRow, int hlCol) {
        String HL_BG   = "\u001B[48;2;160;130;0m";
        String HL_FG   = "\u001B[38;2;255;255;120m";
        String HL_BOLD = "\u001B[1m";
        String RST     = "\u001B[0m";

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
                if (r == hlRow && c == hlCol) {
                    // highlighted cell — clip to CELL_WIDTH-2, pad to CELL_WIDTH
                    String clipped = cell.length() > CELL_WIDTH - 2
                            ? cell.substring(0, CELL_WIDTH - 3) + "…"
                            : cell;
                    String padded = clipped + repeat(" ", CELL_WIDTH - clipped.length());
                    sb.append("│").append(HL_BG).append(HL_FG).append(HL_BOLD)
                            .append(padded).append(RST);
                } else {
                    sb.append("│").append(pad(clip(cell, CELL_WIDTH - 2), CELL_WIDTH));
                }
            }
            sb.append("│\n");
            if (r < rowLabels.length - 1) sb.append(buildBorder("├", "┼", "┤")).append("\n");
        }

        sb.append(buildBorder("└", "┴", "┘")).append("\n");
        sb.append("Note: 20:00-04:00 remains private/busy by default for complaint scheduling.\n");
        return sb.toString();
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

    private String buildStudentContactBlock(String studentId) {
        MyOptional<Student> studentOpt = studentRepo.findById(studentId);
        if (studentOpt.isEmpty()) {
            return "";
        }

        Student s = studentOpt.get();
        StringBuilder sb = new StringBuilder();
        sb.append("Student Contact\n");
        sb.append("--------------\n");
        sb.append("Name  : ").append(valueOrDash(s.getName())).append("\n");
        sb.append("Room  : ").append(valueOrDash(s.getRoomNumber())).append("\n");
        sb.append("Phone : ").append(valueOrDash(s.getPhoneNumber())).append("\n");
        sb.append("Email : ").append(valueOrDash(s.getEmail())).append("\n");
        return sb.toString();
    }

    private String buildTable(String title, String[] rowLabels, String[][] cells, boolean showRealContents) {
        StringBuilder sb = new StringBuilder();

        sb.append(title).append("\n");
        sb.append(buildBorder("┌", "┬", "┐")).append("\n");
        sb.append("│").append(center("TIME", TIME_COL_WIDTH));

        for (int i = 0; i < 7; i++) {
            DayOfWeek day = columnToDay(i);
            String label = dayShort(day);
            if (day == TimeManager.nowDay()) {
                label = "*" + label + "*";
            }
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
            sb.append("Only 08:00-20:00 is shown to attendant. 20:00-04:00 stays private/busy by default.\n");
        } else {
            sb.append("Note: 20:00-04:00 remains private/busy by default for complaint scheduling.\n");
        }

        return sb.toString();
    }

    private String complaintVisitToken(String complaintId) {
        String safe = complaintId == null ? "" : complaintId.trim();
        return "[" + safe + "]";
    }

    private int attendantToFull(int attendantSlotIndex) {
        return attendantSlotIndex + 4;
    }

    private String valueOrDash(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "(not set)";
        }
        return value.trim();
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
            case MONDAY:
                return "MON";
            case TUESDAY:
                return "TUE";
            case WEDNESDAY:
                return "WED";
            case THURSDAY:
                return "THU";
            case FRIDAY:
                return "FRI";
            case SATURDAY:
                return "SAT";
            default:
                return "SUN";
        }
    }

    private int dayToColumn(DayOfWeek day) {
        switch (day) {
            case MONDAY:
                return 0;
            case TUESDAY:
                return 1;
            case WEDNESDAY:
                return 2;
            case THURSDAY:
                return 3;
            case FRIDAY:
                return 4;
            case SATURDAY:
                return 5;
            default:
                return 6;
        }
    }

    private DayOfWeek columnToDay(int column) {
        switch (column) {
            case 0:
                return DayOfWeek.MONDAY;
            case 1:
                return DayOfWeek.TUESDAY;
            case 2:
                return DayOfWeek.WEDNESDAY;
            case 3:
                return DayOfWeek.THURSDAY;
            case 4:
                return DayOfWeek.FRIDAY;
            case 5:
                return DayOfWeek.SATURDAY;
            default:
                return DayOfWeek.SUNDAY;
        }
    }

    private boolean isValidFullSlot(int fullSlotIndex) {
        return fullSlotIndex >= 0 && fullSlotIndex < FULL_SLOT_LABELS.length;
    }

    private String clip(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        if (max <= 1) {
            return s.substring(0, max);
        }
        return s.substring(0, max - 1) + "…";
    }

    private String center(String s, int width) {
        if (s == null) {
            s = "";
        }
        if (s.length() > width) {
            return s.substring(0, width);
        }

        int left = (width - s.length()) / 2;
        int right = width - s.length() - left;
        return repeat(" ", left) + s + repeat(" ", right);
    }

    private String pad(String s, int width) {
        if (s == null) {
            s = "";
        }
        if (s.length() >= width) {
            return s.substring(0, width);
        }
        return s + repeat(" ", width - s.length());
    }

    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    // ── convenience methods used by CLI ──
    public void printStudentRoutine(String studentId) {
        System.out.println(renderStudentRoutineByStudentId(studentId));
    }

    public void printMaskedRoutine(String studentId) {
        System.out.println(renderMaskedRoutineForStudent(studentId));
    }

    public void printDayChoices() {
        DayOfWeek[] days = {
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        };
        for (int i = 0; i < days.length; i++) {
            System.out.println("  " + (i + 1) + ". " + dayShort(days[i]));
        }
    }

    public void printSlotChoices() {
        for (int i = 0; i < FULL_SLOT_LABELS.length; i++) {
            System.out.println("  " + (i + 1) + ". " + FULL_SLOT_LABELS[i]);
        }
    }

    public void setSlot(String studentId, int day, int slot, String content) {
        DayOfWeek dow = columnToDay(day - 1);
        putSlotByStudentId(studentId, dow, slot - 1, content);
    }

    public void clearSlot(String studentId, int day, int slot) {
        DayOfWeek dow = columnToDay(day - 1);
        clearSlotByStudentId(studentId, dow, slot - 1);
    }

    public String getSlotContent(String studentId, int day, int slot) {
        DayOfWeek dow = columnToDay(day - 1);
        MyOptional<StudentRoutineEntry> entry = routineRepo.findOne(studentId, dow, slot - 1);
        if (entry.isEmpty()) {
            return null;
        }
        return entry.get().getContent();
    }
}
