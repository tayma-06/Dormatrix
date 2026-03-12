package models.schedule;

import java.time.DayOfWeek;

public class WorkerVisitEntry {

    private final String complaintId;
    private final String workerId;
    private final String studentId;
    private final String roomNo;
    private final DayOfWeek day;
    private final int slotIndex;   // 0..5 => 08-10 ... 18-20
    private final String status;   // PLANNED / DONE / CANCELLED
    private final String note;

    public WorkerVisitEntry(String complaintId,
                            String workerId,
                            String studentId,
                            String roomNo,
                            DayOfWeek day,
                            int slotIndex,
                            String status,
                            String note) {
        this.complaintId = complaintId;
        this.workerId = workerId;
        this.studentId = studentId;
        this.roomNo = roomNo;
        this.day = day;
        this.slotIndex = slotIndex;
        this.status = status;
        this.note = note;
    }

    public String getComplaintId() { return complaintId; }
    public String getWorkerId() { return workerId; }
    public String getStudentId() { return studentId; }
    public String getRoomNo() { return roomNo; }
    public DayOfWeek getDay() { return day; }
    public int getSlotIndex() { return slotIndex; }
    public String getStatus() { return status; }
    public String getNote() { return note; }
}