package models.facilities;

public class FridgeSlot {
    private int slotNumber;
    private String studentId;

    public FridgeSlot(int num) { this.slotNumber = num; }

    public int getSlotNumber() { return this.slotNumber; }
    public String getStudentId() { return this.studentId; }

    public void setSlotNumber(int num) { this.slotNumber = num; }
    public void setStudentId(String id) { this.studentId = id; }
}