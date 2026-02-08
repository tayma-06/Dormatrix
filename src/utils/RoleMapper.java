package utils;

import libraries.collections.MyString;

public final class RoleMapper {
    private RoleMapper() {}

    public static MyString getRoleFromChoice(int choice) {
        return switch (choice) {
            case 1 -> new MyString("STUDENT");
            case 2 -> new MyString("HALL_ATTENDANT");
            case 3 -> new MyString("MAINTENANCE_WORKER");
            case 4 -> new MyString("STORE_IN_CHARGE");
            case 5 -> new MyString("HALL_OFFICER");
            case 6 -> new MyString("ADMIN");
            default -> new MyString("UNKNOWN");
        };
    }
}
