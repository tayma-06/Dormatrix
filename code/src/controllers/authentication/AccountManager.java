package controllers.authentication;

import libraries.collections.MyString;
import models.users.User;

import java.io.*;

public class AccountManager {

    private static final MyString USER_DATA_PATH = new MyString("data/users/");

    public boolean userExists(MyString userId, MyString role) {
        if (userId == null || role == null || userId.getValue().trim().isEmpty()) {
            return false;
        }

        MyString filename = getFilename(role);
        File file = new File(filename.getValue());
        if (!file.exists()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                MyString[] parts = new MyString(line).split('|');
                if (parts.length > 0
                        && parts[0].getValue().trim().equals(userId.getValue().trim())) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    public boolean registerUser(User user, MyString role) {
        if (user == null || role == null) {
            return false;
        }

        MyString filename = getFilename(role);
        File dir = new File(USER_DATA_PATH.getValue());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (FileWriter fw = new FileWriter(filename.getValue(), true)) {
            fw.write(user.toFileString());
            fw.write(System.lineSeparator());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean deleteUser(MyString userId, MyString role) {
        if (userId == null || role == null || userId.getValue().trim().isEmpty()) {
            return false;
        }

        MyString filename = getFilename(role);
        File file = new File(filename.getValue());

        if (!file.exists()) {
            return false;
        }

        StringBuilder content = new StringBuilder();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                MyString[] parts = new MyString(line).split('|');

                if (parts.length > 0
                        && parts[0].getValue().trim().equals(userId.getValue().trim())) {
                    found = true;
                    continue;
                }

                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            return false;
        }

        if (!found) {
            return false;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.print(content.toString());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String findUserDetails(MyString userId) {
        if (userId == null || userId.getValue().trim().isEmpty()) {
            return null;
        }

        MyString[] roles = {
                new MyString("STUDENT"),
                new MyString("HALL_ATTENDANT"),
                new MyString("MAINTENANCE_WORKER"),
                new MyString("STORE_IN_CHARGE"),
                new MyString("HALL_OFFICER"),
                new MyString("ADMIN"),
                new MyString("CAFETERIA_MANAGER")
        };

        for (MyString role : roles) {
            MyString filename = getFilename(role);
            File file = new File(filename.getValue());

            if (!file.exists()) {
                continue;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    MyString[] parts = new MyString(line).split('|');

                    if (parts.length > 1
                            && parts[0].getValue().trim().equals(userId.getValue().trim())) {
                        return "Found: " + parts[1].getValue() + " (" + normalizeRole(role).getValue() + ")";
                    }
                }
            } catch (IOException ignored) {
            }
        }

        return null;
    }

    public MyString getFilename(MyString role) {
        String roleStr = normalizeRole(role).getValue();

        return switch (roleStr) {
            case "ADMIN" -> USER_DATA_PATH.concat(new MyString("admin.txt"));
            case "STUDENT" -> USER_DATA_PATH.concat(new MyString("students.txt"));
            case "HALL_ATTENDANT" -> USER_DATA_PATH.concat(new MyString("hall_attendants.txt"));
            case "MAINTENANCE_WORKER" -> USER_DATA_PATH.concat(new MyString("maintenance_workers.txt"));
            case "STORE_IN_CHARGE" -> USER_DATA_PATH.concat(new MyString("store_in_charges.txt"));
            case "HALL_OFFICER" -> USER_DATA_PATH.concat(new MyString("hall_officers.txt"));
            case "CAFETERIA_MANAGER" -> USER_DATA_PATH.concat(new MyString("cafeteria_managers.txt"));
            default -> USER_DATA_PATH
                    .concat(new MyString(roleStr.toLowerCase()))
                    .concat(new MyString("s.txt"));
        };
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }

        String r = role.trim().toUpperCase()
                .replace('-', '_')
                .replace(' ', '_');

        return switch (r) {
            case "ATTENDANT", "HALL_ATTENDANT" -> "HALL_ATTENDANT";
            case "HALL_OFFICE", "HALL_OFFICER" -> "HALL_OFFICER";
            case "STOREINCHARGE", "STORE_IN_CHARGE" -> "STORE_IN_CHARGE";
            case "MAINTENANCEWORKER", "MAINTENANCE_WORKER" -> "MAINTENANCE_WORKER";
            case "CAFETERIAMANAGER", "CAFETERIA_MANAGER" -> "CAFETERIA_MANAGER";
            case "STUDENT" -> "STUDENT";
            case "ADMIN" -> "ADMIN";
            default -> r;
        };
    }

    private MyString normalizeRole(MyString role) {
        return new MyString(normalizeRole(role == null ? "" : role.getValue()));
    }
}