package models.facilities;

public class StudyRoomSeat {
    private int seatNumber;
    private String studentId;
    private long bookingTime;

    public StudyRoomSeat(int seatNumber) {
        this.seatNumber = seatNumber;
        this.studentId = null;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String id) { this.studentId = id; }
}