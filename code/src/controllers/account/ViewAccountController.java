package controllers.account;

import controllers.authentication.AccountManager;
import libraries.collections.MyString;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ViewAccountController {

    public static class AccountSummary {
        private final String id;
        private final String name;
        private final String role;
        private final String rawData;

        public AccountSummary(String id, String name, String role, String rawData) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.rawData = rawData;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getRole() {
            return role;
        }

        public String getRawData() {
            return rawData;
        }
    }

    private final AccountManager manager;

    public ViewAccountController(AccountManager manager) {
        this.manager = manager;
    }

    public List<AccountSummary> getAccountsByChoice(int choice) {
        if (choice == 8) {
            return getAllAccounts();
        }

        MyString role = getRoleFromChoice(choice);
        if ("UNKNOWN".equals(role.getValue())) {
            return new ArrayList<>();
        }

        return readRoleFile(role.getValue());
    }

    public String formatAccountDetails(String rawData) {
        return AccountRecordParser.formatDetails(rawData);
    }

    private List<AccountSummary> getAllAccounts() {
        String[] roles = {
                "STUDENT",
                "HALL_ATTENDANT",
                "MAINTENANCE_WORKER",
                "STORE_IN_CHARGE",
                "HALL_OFFICER",
                "ADMIN",
                "CAFETERIA_MANAGER"
        };

        List<AccountSummary> all = new ArrayList<>();
        for (String role : roles) {
            all.addAll(readRoleFile(role));
        }
        return all;
    }

    private List<AccountSummary> readRoleFile(String role) {
        List<AccountSummary> list = new ArrayList<>();

        MyString filename = manager.getFilename(new MyString(role));
        File file = new File(filename.getValue());

        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                AccountRecordParser.ParsedAccount parsed = AccountRecordParser.parse(line);
                if (parsed == null) {
                    continue;
                }

                list.add(new AccountSummary(
                        parsed.getId(),
                        parsed.getName(),
                        parsed.getRole(),
                        line
                ));
            }
        } catch (IOException ignored) {
        }

        return list;
    }

    private MyString getRoleFromChoice(int choice) {
        switch (choice) {
            case 1: return new MyString("STUDENT");
            case 2: return new MyString("HALL_ATTENDANT");
            case 3: return new MyString("MAINTENANCE_WORKER");
            case 4: return new MyString("STORE_IN_CHARGE");
            case 5: return new MyString("HALL_OFFICER");
            case 6: return new MyString("ADMIN");
            case 7: return new MyString("CAFETERIA_MANAGER");
            default: return new MyString("UNKNOWN");
        }
    }
}