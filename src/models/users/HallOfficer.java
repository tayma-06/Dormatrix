package models.users;

public class HallOfficer extends User{

    public String email;

    public HallOfficer(String id, String name, String role, String passwordHash, String phoneNumber) {
        super(id, name, role, passwordHash, phoneNumber);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toFileString() {
        return " ";
    }
}
