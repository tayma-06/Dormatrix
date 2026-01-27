package controllers.authentication;

import models.users.*;
import libraries.collections.MyString;
import java.io.*;

public class AccountManager {
    private static final MyString USER_DATA_PATH = new MyString("data/users/");
    public boolean userExists(MyString userId, MyString role) {
        MyString filename = getFilename(role);
        File file = new File(filename.getValue());
        if (!file.exists()) return false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                MyString[] parts = new MyString(line).split('|');
                if (parts.length > 0 && parts[0].getValue().trim().equals(userId.getValue().trim())) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }
    public boolean registerUser(User user, MyString role) {
        MyString filename = getFilename(role);
        File dir = new File(USER_DATA_PATH.getValue());
        if (!dir.exists()) dir.mkdirs();

        try (FileWriter fw = new FileWriter(filename.getValue(), true)) {
            fw.write(user.toFileString() + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    public boolean deleteUser(MyString userId, MyString role) {
        MyString filename = getFilename(role);
        File file = new File(filename.getValue());
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            boolean found = false;
            while ((line = reader.readLine()) != null) {
                MyString mLine = new MyString(line);
                MyString[] parts = mLine.split('|');
                if (parts.length > 0 && parts[0].getValue().trim().equals(userId.getValue().trim())) {
                    found = true;
                    continue;
                }
                content.append(line).append("\n");
            }
            reader.close();

            if (found) {
                PrintWriter writer = new PrintWriter(new FileWriter(file));
                writer.print(content.toString());
                writer.close();
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public String findUserDetails(MyString userId) {
        MyString[] roles = {
                new MyString("STUDENT"), new MyString("HALL_ATTENDANT"),
                new MyString("MAINTENANCE_WORKER"), new MyString("STORE_IN_CHARGE"),
                new MyString("HALL_OFFICER"), new MyString("ADMIN")
        };

        for (MyString role : roles) {
            MyString filename = getFilename(role);
            File file = new File(filename.getValue());
            if (!file.exists()) continue;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    MyString[] parts = new MyString(line).split('|');
                    if (parts.length > 0 && parts[0].getValue().trim().equals(userId.getValue().trim())) {
                        return "Found: " + parts[1].getValue() + " (" + role.getValue() + ")";
                    }
                }
            } catch (IOException e) {}
        }
        return null;
    }

    public MyString getFilename(MyString role) {
        return USER_DATA_PATH.concat(role.toLowerCase()).concat(new MyString("s.txt"));
    }
}