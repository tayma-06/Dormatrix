package models.users;

import models.complaints.Complaints;

public class Student extends User{
    private String department;
    private String batch;
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
    public String getBatch() { return batch; }
    public String getRoomNumber() { return roomNumber; }

    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    @Override
    public String toFileString() {
        return id + "|" + name + "|STUDENT|" + department + "|" + batch + "|" + roomNumber + "|" + passwordHash;
    }

}
