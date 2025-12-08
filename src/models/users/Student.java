package models.users;

public class Student extends User{
    private String department;
    private String batch;
    private String roomNumber; // initially "UNASSIGNED"
    private String email;

    public Student(String id, String name, String role, String passwordHash, String phoneNumber) {
        super(id, name, role, passwordHash, phoneNumber);
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    // ----- Additional Getters & Setters -----
    public String getDepartment() { return department; }
    public String getBatch() { return batch; }
    public String getRoomNumber() { return roomNumber; }

    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    @Override
    public String toFileString() {
        return id + "|" + name + "|STUDENT|" + department + "|" + batch + "|" + roomNumber + "|" + passwordHash;
    }

}
