package controllers.account;

public final class AccountRecordParser {

    public static class ParsedAccount {
        private final String id;
        private final String name;
        private final String role;
        private final String phone;
        private final String email;
        private final String department;
        private final String field;
        private final String room;

        public ParsedAccount(
                String id,
                String name,
                String role,
                String phone,
                String email,
                String department,
                String field,
                String room
        ) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.phone = phone;
            this.email = email;
            this.department = department;
            this.field = field;
            this.room = room;
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

        public String getPhone() {
            return phone;
        }

        public String getEmail() {
            return email;
        }

        public String getDepartment() {
            return department;
        }

        public String getField() {
            return field;
        }

        public String getRoom() {
            return room;
        }
    }

    private AccountRecordParser() {
    }

    public static ParsedAccount parse(String rawData) {
        if (rawData == null || rawData.trim().isEmpty() || !rawData.contains("|")) {
            return null;
        }

        String[] parts = rawData.split("\\|", -1);

        String id = val(parts, 0);
        String name = val(parts, 1);
        String role = val(parts, 2);

        switch (role) {
            case "STUDENT":
                return new ParsedAccount(
                        id,
                        name,
                        role,
                        val(parts, 5),
                        val(parts, 6),
                        val(parts, 3),
                        null,
                        val(parts, 7)
                );

            case "HALL_ATTENDANT":
                return new ParsedAccount(
                        id,
                        name,
                        role,
                        val(parts, 5),
                        val(parts, 6),
                        null,
                        null,
                        null
                );

            case "HALL_OFFICER":
                return new ParsedAccount(
                        id,
                        name,
                        role,
                        val(parts, 5),
                        val(parts, 6),
                        null,
                        null,
                        null
                );

            case "MAINTENANCE_WORKER":
                return new ParsedAccount(
                        id,
                        name,
                        role,
                        val(parts, 5),
                        null,
                        null,
                        val(parts, 6),
                        null
                );

            case "STORE_IN_CHARGE":
                return new ParsedAccount(
                        id,
                        name,
                        role,
                        val(parts, 5),
                        null,
                        null,
                        null,
                        null
                );

            case "ADMIN":
                return new ParsedAccount(
                        id,
                        name,
                        role,
                        val(parts, 5),
                        null,
                        null,
                        null,
                        null
                );

            case "CAFETERIA_MANAGER":
                return new ParsedAccount(
                        id,
                        name,
                        role,
                        val(parts, 5),
                        null,
                        null,
                        null,
                        null
                );

            default:
                return new ParsedAccount(
                        id,
                        name,
                        role,
                        val(parts, 5),
                        val(parts, 6),
                        val(parts, 3),
                        null,
                        val(parts, 7)
                );
        }
    }

    public static String formatDetails(String rawData) {
        ParsedAccount account = parse(rawData);
        if (account == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ID         : ").append(account.getId()).append("\n");
        sb.append("Name       : ").append(account.getName()).append("\n");
        sb.append("Role       : ").append(pretty(account.getRole())).append("\n");
        sb.append("Phone      : ").append(account.getPhone()).append("\n");

        if (show(account.getEmail())) {
            sb.append("Email      : ").append(account.getEmail()).append("\n");
        }
        if (show(account.getDepartment())) {
            sb.append("Department : ").append(account.getDepartment()).append("\n");
        }
        if (show(account.getField())) {
            sb.append("Field      : ").append(pretty(account.getField())).append("\n");
        }
        if (show(account.getRoom())) {
            sb.append("Room       : ").append(account.getRoom()).append("\n");
        }

        return sb.toString().trim();
    }

    private static boolean show(String value) {
        return value != null && !value.isBlank() && !"N/A".equalsIgnoreCase(value);
    }

    private static String val(String[] parts, int index) {
        if (index < 0 || index >= parts.length) {
            return "N/A";
        }

        String value = parts[index] == null ? "" : parts[index].trim();
        if (value.isEmpty()) {
            return "N/A";
        }

        return value;
    }

    private static String pretty(String value) {
        if (value == null) {
            return "N/A";
        }
        return value.replace('_', ' ');
    }
}