package models.users;

public class HallAttendant extends User{

    private String email;

    public HallAttendant(String id, String name, String role, String passwordHash, String phoneNumber, String email) {
        super(id, name, role, passwordHash, phoneNumber);
        this.email = email;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toFileString() {
        return id + "|" + name + "|HALL_ATTENDANT|"  + "|" + passwordHash + "|" + phoneNumber + "|" + email;
    }
}
