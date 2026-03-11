package controllers.account;

import controllers.authentication.AccountManager;
import libraries.collections.MyString;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SearchUserController {

    private final AccountManager manager;

    private static final String[] ROLES = {
            "STUDENT",
            "HALL_ATTENDANT",
            "MAINTENANCE_WORKER",
            "STORE_IN_CHARGE",
            "HALL_OFFICER",
            "ADMIN",
            "CAFETERIA_MANAGER"
    };

    public SearchUserController(AccountManager manager) {
        this.manager = manager;
    }

    public String searchById(String id) {
        String wanted = normalize(id);
        if (wanted.isEmpty()) {
            return null;
        }

        for (String role : ROLES) {
            String found = searchRoleFile(role, wanted);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    private String searchRoleFile(String role, String wantedId) {
        MyString filename = manager.getFilename(new MyString(role));
        File file = new File(filename.getValue());

        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\|");
                if (parts.length == 0) {
                    continue;
                }

                String fileId = normalize(parts[0]);
                if (wantedId.equals(fileId)) {
                    return line;
                }
            }
        } catch (IOException ignored) {
        }

        return null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}