package models.facilities;

public class LaundrySlot {
    private int slotIndex; // 0 to 5
    private String studentId;
    private boolean isMaintenanceIssue = false;

    public LaundrySlot(int index) { this.slotIndex = index; }

    public void setMaintenanceIssue(boolean issue) { this.isMaintenanceIssue = issue; }
    public boolean hasIssue() { return isMaintenanceIssue; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String id) { this.studentId = id; }
}