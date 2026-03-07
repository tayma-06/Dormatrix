package models.routine;

import java.time.DayOfWeek;

public class RoutineEntry {
    private final String studentId;
    private final DayOfWeek day;
    private final int slotIndex;
    private final String content;

    public RoutineEntry(String studentId, DayOfWeek day, int slotIndex, String content) {
        this.studentId = clean(studentId);
        this.day = day;
        this.slotIndex = slotIndex;
        this.content = clean(content);
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
        if (line == null || line.trim().isEmpty()) return null;

        String[] parts = line.split("\\|", 4);
        if (parts.length < 4) return null;

        try {
            String studentId = parts[0].trim();
            DayOfWeek day = DayOfWeek.valueOf(parts[1].trim());
            int slotIndex = Integer.parseInt(parts[2].trim());
            String content = parts[3].trim();
            return new RoutineEntry(studentId, day, slotIndex, content);
        } catch (Exception e) {
            return null;
        }
    }

    private static String clean(String value) {
        if (value == null) return "";
        return value.replace("\r", " ")
                .replace("\n", " ")
                .replace("|", "/")
                .trim();
    }
}
