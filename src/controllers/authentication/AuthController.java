package controllers.authentication;

import models.users.*;
import models.enums.WorkerField;
import libraries.hashing.HashFunction;
import libraries.collections.MyString;
import java.io.*;

public class AuthController {

    private static final MyString DATA_DIR = new MyString("data/users/");

    private MyString getFilePath(MyString role) {
        if (role.getValue().equals("ADMIN")) {
            return DATA_DIR.concat(new MyString("admin.txt"));
        }
        return DATA_DIR.concat(role.toLowerCase()).concat(new MyString("s.txt"));
    }

    public boolean authenticateUser(MyString username, MyString password, MyString role) {
        try {
            File file = new File(getFilePath(role).getValue());
            if (!file.exists()) return false;

            MyString hashedInput = HashFunction.hashPassword(password);

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    MyString[] parts = new MyString(line).split('|');
                    if (parts.length > 4 && parts[0].getValue().trim().equals(username.getValue().trim())) {
                        if (parts[4].getValue().equals(hashedInput.getValue())) {
                            return true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Auth Error: " + e.getMessage());
        }
        return false;
    }

    public void createDefaultAdmin() {
        File dir = new File(DATA_DIR.getValue());
        if (!dir.exists()) dir.mkdirs();

        File adminFile = new File(getFilePath(new MyString("ADMIN")).getValue());
        if (adminFile.exists()) return;

        MyString u = ConfigLoader.getAdminUsername();
        MyString p = ConfigLoader.getAdminPassword();
        MyString n = ConfigLoader.getAdminName();
        MyString ph = ConfigLoader.getAdminPhone();

        if (u == null || p == null || n == null || ph == null) {
            System.err.println("Setup Failed: specific keys missing in config/admin.config");
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(adminFile))) {
            MyString hashed = HashFunction.hashPassword(p);
            MyString record = u.concat(new MyString("|"))
                    .concat(n).concat(new MyString("|"))
                    .concat(new MyString("ADMIN|N/A|"))
                    .concat(hashed).concat(new MyString("|"))
                    .concat(ph);
            writer.println(record.getValue());
            System.out.println("Default admin initialized from config file.");
        } catch (IOException e) {
            System.err.println("Admin Init Error: " + e.getMessage());
        }
    }

    public User getUserByUsername(MyString username, MyString role) {
        try {
            File file = new File(getFilePath(role).getValue());
            if (!file.exists()) return null;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    MyString[] parts = new MyString(line).split('|');
                    if (parts.length > 0 && parts[0].getValue().trim().equals(username.getValue().trim())) {
                        return createUserFromParts(parts, role.getValue());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Read Error: " + e.getMessage());
        }
        return null;
    }

    private User createUserFromParts(MyString[] parts, String role) {
        String id = parts[0].getValue();
        String name = parts[1].getValue();
        String hash = parts.length > 4 ? parts[4].getValue() : "";
        String phone = parts.length > 5 ? parts[5].getValue() : "";

        switch (role) {
            case "STUDENT":
                String dept = parts.length > 3 ? parts[3].getValue() : "N/A";
                String email = parts.length > 6 ? parts[6].getValue() : "N/A";

                Student s = new Student(id, name, role, hash, phone, email);
                s.setDepartment(dept);

                if (parts.length > 7) {
                    s.setRoomNumber(parts[7].getValue());
                }
                return s;

            case "HALL_ATTENDANT":
                String haEmail = parts.length > 6 ? parts[6].getValue() : "N/A";
                return new HallAttendant(id, name, role, hash, phone, haEmail);

            case "MAINTENANCE_WORKER":
                String fieldStr = parts.length > 6 ? parts[6].getValue() : "ELECTRICIAN";
                WorkerField field;
                try {
                    field = WorkerField.valueOf(fieldStr);
                } catch (IllegalArgumentException e) {
                    field = WorkerField.ELECTRICIAN;
                }
                return new MaintenanceWorker(id, name, role, hash, phone, field);

            case "STORE_IN_CHARGE":
                return new StoreInCharge(id, name, role, hash, phone);

            case "HALL_OFFICER":
                String hoEmail = parts.length > 6 ? parts[6].getValue() : "N/A";
                return new HallOfficer(id, name, role, hash, phone, hoEmail);

            case "ADMIN":
                return new SystemAdmin(id, name, role, hash, phone);

            default:
                return null;
        }
    }
// Will think about implementing these later on
//    public boolean changePassword(MyString username, MyString role, MyString oldPass, MyString newPass) {
//        File file = new File(getFilePath(role).getValue());
//        if (!file.exists()) return false;
//        boolean success = false;
//        StringBuilder sb = new StringBuilder();
//        MyString oldHash = HashFunction.hashPassword(oldPass);
//        MyString newHash = HashFunction.hashPassword(newPass);
//        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                MyString mLine = new MyString(line);
//                MyString[] parts = mLine.split('|');
//                if (parts.length > 4 && parts[0].trim().equals(username.trim())) {
//                    if (parts[4].equals(oldHash)) {
//                        parts[4] = newHash;
//                        line = reconstructLine(parts);
//                        success = true;
//                    } else {
//                        return false;
//                    }
//                }
//                sb.append(line).append("\n");
//            }
//        } catch (IOException e) { return false; }
//
//        if (success) writeToFile(file, sb.toString());
//        return success;
//    }
//    public boolean updateUserInfo(MyString username, MyString role, MyString newName, MyString newPhone) {
//        File file = new File(getFilePath(role).getValue());
//        if (!file.exists()) return false;
//        boolean found = false;
//        StringBuilder sb = new StringBuilder();
//
//        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                MyString[] parts = new MyString(line).split('|');
//                if (parts.length > 0 && parts[0].trim().equals(username.trim())) {
//                    parts[1] = newName;
//                    if (parts.length > 5) parts[5] = newPhone;
//                    line = reconstructLine(parts);
//                    found = true;
//                }
//                sb.append(line).append("\n");
//            }
//        } catch (IOException e) { return false; }
//
//        if (found) writeToFile(file, sb.toString());
//        return found;
//    }
//    private String reconstructLine(MyString[] parts) {
//        MyString res = new MyString("");
//        for (int i = 0; i < parts.length; i++) {
//            res = res.concat(parts[i]);
//            if (i < parts.length - 1) res = res.concat(new MyString("|"));
//        }
//        return res.getValue();
//    }
//    private void writeToFile(File file, String content) {
//        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
//            writer.print(content);
//        } catch (IOException e) {
//            System.err.println("Write Error: " + e.getMessage());
//        }
//    }
}