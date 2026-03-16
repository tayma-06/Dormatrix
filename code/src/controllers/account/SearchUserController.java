package controllers.account;

import controllers.authentication.AccountManager;
import libraries.collections.MyString;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchUserController {

    public static class SearchHit {
        private final String id;
        private final String name;
        private final String role;
        private final String rawData;

        public SearchHit(String id, String name, String role, String rawData) {
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

    public List<SearchHit> searchSuggestions(String query, int limit) {
        List<SearchHit> prefixHits = new ArrayList<>();
        List<SearchHit> otherHits = new ArrayList<>();

        String wanted = normalize(query).toLowerCase();
        if (wanted.isEmpty() || limit <= 0) {
            return prefixHits;
        }

        for (String role : ROLES) {
            collectRoleSuggestions(role, wanted, prefixHits, otherHits, limit);
            if (prefixHits.size() + otherHits.size() >= limit) {
                break;
            }
        }

        List<SearchHit> all = new ArrayList<>(prefixHits);
        for (SearchHit hit : otherHits) {
            if (all.size() >= limit) {
                break;
            }
            all.add(hit);
        }

        return all;
    }

    private void collectRoleSuggestions(
            String role,
            String wanted,
            List<SearchHit> prefixHits,
            List<SearchHit> otherHits,
            int limit
    ) {
        MyString filename = manager.getFilename(new MyString(role));
        File file = new File(filename.getValue());

        if (!file.exists()) {
            return;
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

                String id = normalize(parsed.getId());
                String name = normalize(parsed.getName());
                String roleName = normalize(parsed.getRole());

                String idLower = id.toLowerCase();
                String nameLower = name.toLowerCase();

                boolean isPrefix = idLower.startsWith(wanted);
                boolean isOtherMatch = !isPrefix &&
                        (idLower.contains(wanted) || nameLower.contains(wanted));

                if (isPrefix) {
                    prefixHits.add(new SearchHit(id, name, roleName, line));
                } else if (isOtherMatch) {
                    otherHits.add(new SearchHit(id, name, roleName, line));
                }

                if (prefixHits.size() + otherHits.size() >= limit) {
                    return;
                }
            }
        } catch (IOException ignored) {
        }
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