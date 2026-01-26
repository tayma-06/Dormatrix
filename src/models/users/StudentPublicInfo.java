package models.users;

public class StudentPublicInfo {
    private final String studentId;
    private final String name;
    private final String roomNo;

    public StudentPublicInfo(String studentId, String name, String roomNo){
        this.studentId = studentId;
        this.name = name;
        this.roomNo = roomNo;
    }

    public String getStudentId(){ return studentId; }
    public String getName(){ return name; }
    public String getRoomNo(){ return roomNo; }
}
