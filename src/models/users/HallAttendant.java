package models.users;

public class HallAttendant extends User{

    public HallAttendant(String id, String name, String role, String passwordHash, String phoneNumber) {
        super(id, name, role, passwordHash, phoneNumber);
    }

    @Override
    public String toFileString() {
        return "";
    }
}
