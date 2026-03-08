package models.routine;

import java.time.DayOfWeek;

public class StudentRoutineEntry {
    private final String studentId;
    private final DayOfWeek day;
    private final int slotIndex;
    private final String content;

    public StudentRoutineEntry(String studentId, DayOfWeek day, int slotIndex, String content) {
        this.studentId = studentId;
        this.day = day;
        this.slotIndex = slotIndex;
        this.content = content;
    }

    public String getStudentId() { return studentId; }
    public DayOfWeek getDay() { return day; }
    public int getSlotIndex() { return slotIndex; }
    public String getContent() { return content; }

    public boolean hasContent() {
        return content != null && !content.trim().isEmpty();
    }
}