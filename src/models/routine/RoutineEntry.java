package models.routine;

import java.time.DayOfWeek;

public class RoutineEntry {

    private final String studentId;
    private final DayOfWeek day;
    private final int slotIndex;
    private final String content;

    public RoutineEntry(String studentId, DayOfWeek day, int slotIndex, String content) {
        this.studentId = studentId;
        this.day = day;
        this.slotIndex = slotIndex;
        this.content = (content == null) ? null : content.replace("|", "/");
    }

    public String getStudentId() {
        return studentId;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public String getContent() {
        return content;
    }

    public String toFileString() {
        return studentId + "|" + day.name() + "|" + slotIndex + "|" + content;
    }

    public static RoutineEntry fromFileString(String line) {
        if (line == null || line.isEmpty()) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length < 4) {
            return null;
        }
        try {
            String studentId = parts[0];
            DayOfWeek day = DayOfWeek.valueOf(parts[1]);
            int slotIndex = Integer.parseInt(parts[2]);
            String content = parts[3];
            return new RoutineEntry(studentId, day, slotIndex, content);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
