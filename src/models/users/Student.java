package models.users;
<<<<<<< HEAD

import models.complaints.Complaints;

=======
import libraries.collections.*;
>>>>>>> 70745f37fad2f8c036747b770d235781ecc8f6a1
public class Student extends User{
    private String department;
    private String roomNumber; // initially "UNASSIGNED"
    private String email;
    Complaints complaints;

    public Student(String id, String name, String role, String passwordHash, String phoneNumber, String email, Complaints complaints) {
        super(id, name, role, passwordHash, phoneNumber);
        this.complaints = complaints;
        this.email = email;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public Complaints getComplaints() {
        return complaints;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setComplaints(Complaints complaints) {
        this.complaints = complaints;
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
