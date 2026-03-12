package models.users;

public class StoreInCharge extends User{
    public StoreInCharge(String id, String name, String role, String passwordHash, String phoneNumber) {
        super(id, name, role, passwordHash, phoneNumber);
    }
    @Override
    public String toFileString() {
        return id + "|" + name + "|STORE_IN_CHARGE|N/A|" + passwordHash + "|" + phoneNumber;
    }

}
