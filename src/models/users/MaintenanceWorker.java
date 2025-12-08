package models.users;

public class MaintenanceWorker extends User{
    public MaintenanceWorker(String id, String name, String role, String passwordHash, String phoneNumber) {
        super(id, name, role, passwordHash, phoneNumber);
    }

    @Override
    public String toFileString() {
        return " ";
    }
}
