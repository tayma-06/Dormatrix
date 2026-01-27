package models.users;

import models.complaints.Complaints;
import libraries.collections.*;

public class Student extends User {
    private String department;
    private String roomNumber;
    private String email;
    private Complaints complaints; // Optional, loaded separately

    // 1. Fixed Constructor: simplified to match what AuthController sends
    public Student(String id, String name, String role, String passwordHash, String phoneNumber) {
        super(id, name, role, passwordHash, phoneNumber);
        this.roomNumber = "UNASSIGNED"; // Default value
        this.department = "N/A";
        this.email = "N/A";
    }

    // Getters & Setters
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
        return id + "|" + name + "|STUDENT|"  + "|" + passwordHash + "|" + phoneNumber ;
    }

    public static Student fromFileString(String fileString) {
        MyString[] parts = new MyString(fileString).split('|');
        if (parts.length < 6) {
            return null;
        }
        String id = parts[0].getValue();
        String name = parts[1].getValue();
        String role = parts[2].getValue();
        String dept = parts[3].getValue();
        String hash = parts[4].getValue();
        String phone = parts[5].getValue();
        Student s = new Student(id, name, role, hash, phone);
        s.setDepartment(dept);
        if (parts.length > 6) {
            s.setEmail(parts[6].getValue());
        }
        if (parts.length > 7) {
            s.setRoomNumber(parts[7].getValue());
        }
        return s;
    }

    public StudentPublicInfo publicInfo() {
        return new StudentPublicInfo(id, name, roomNumber);
    }
}