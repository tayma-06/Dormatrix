package models.users;

import models.complaints.Complaints;
import libraries.collections.*;

public class Student extends User {
    private String department;
    private String roomNumber;
    private String email;
    private Complaints complaints;

    // Constructor used when Admin creates a new student (Room/Dept might be default)
    public Student(String id, String name, String role, String passwordHash, String phoneNumber, String email) {
        super(id, name, role, passwordHash, phoneNumber);
        this.email = email;
        this.department = "N/A";
        this.roomNumber = "UNASSIGNED";
    }

    public void setDepartment(String department) { this.department = department; }
    public String getDepartment() { return department; }

    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getRoomNumber() { return roomNumber; }

    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return email; }

    public void setComplaints(Complaints complaints) { this.complaints = complaints; }
    public Complaints getComplaints() { return complaints; }

    @Override
    public String toFileString() {
        // Format: ID|Name|STUDENT|Dept|Hash|Phone|Email|Room
        return id + "|" +
                name + "|" +
                "STUDENT" + "|" +
                department + "|" +
                passwordHash + "|" +
                phoneNumber + "|" +
                email + "|" +
                roomNumber;
    }

    public static Student fromFileString(String fileString) {
        MyString[] parts = new MyString(fileString).split('|');

        // We need at least 7 parts (ID, Name, Role, Dept, Hash, Phone, Email)
        if (parts.length < 7) {
            return null;
        }

        String id = parts[0].getValue();
        String name = parts[1].getValue();
        String role = parts[2].getValue();
        String dept = parts[3].getValue();
        String hash = parts[4].getValue();
        String phone = parts[5].getValue();
        String email = parts[6].getValue();
        Student s = new Student(id, name, role, hash, phone, email);
        s.setDepartment(dept);
        if (parts.length > 7) {
            s.setRoomNumber(parts[7].getValue());
        }

        return s;
    }

    public StudentPublicInfo publicInfo() {
        return new StudentPublicInfo(id, name, roomNumber);
    }
}