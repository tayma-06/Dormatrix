package models.users;
public class SystemAdmin extends User{


    public SystemAdmin(String id, String name, String role, String passwordHash, String phoneNumber) {
        super(id, name, role, passwordHash, phoneNumber);
    }

    @Override
    public String toFileString() {
        return id + "|" + name + "|ADMIN|"  + "|" + passwordHash + "|" + phoneNumber;
    }
}
