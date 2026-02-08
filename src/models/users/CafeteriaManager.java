package models.users;

public class CafeteriaManager extends User {
    public CafeteriaManager(String id, String name, String role, String passwordHash, String phoneNumber) {
        super(id, name, role, passwordHash, phoneNumber);
    }

    @Override
    public String toFileString() {
        return String.join("|", id, name, role, "N/A", passwordHash, phoneNumber);
    }
}