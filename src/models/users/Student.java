package models.users;
import libraries.collections.*;
public class Student extends User{
    private String department;
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
    public String getRoomNumber() { return roomNumber; }

    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public void setEmail(String email) {
        this.email = email;
    }

    // creates a public info suitable for complaint storage
    public StudentPublicInfo publicInfo()
    {
        return new StudentPublicInfo(id, name, roomNumber);
    }
    @Override
    public String toFileString() {
        return id + "|" + name + "|STUDENT|" + department + "|" + passwordHash + "|" + phoneNumber + "|" + email;
    }

    // public String toComplaintFileString()
    // {
    //     return id+"|"+name+"|"+roomNumber;
    // }

    public static Student fromFileString(String fileString)
    {
        MyString[] parts = new MyString(fileString).split('|');
        if(parts.length >= 7)
        {
            return new Student(
                parts[0].getValue(),
                parts[1].getValue(),
                parts[2].getValue(),
                parts[4].getValue(),
                parts[5].getValue()
            );
        }
        return null;
    } 
    
}
