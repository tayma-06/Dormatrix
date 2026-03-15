package controllers.authentication;

import libraries.collections.MyString;
import libraries.hashing.HashFunction;
import models.enums.WorkerField;
import models.users.*;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import utils.TerminalUI;

import java.io.*;
import java.util.Scanner;

public class AuthController {

    private static final MyString DATA_DIR = new MyString("data/users/");

    private static Terminal terminal = null;
    private static LineReader lineReader = null;
    private static boolean jlineAvailable = true;
    private static boolean ownsTerminal = false;

    private static void initJLine() {
        if (!jlineAvailable) return;
        if (terminal != null && lineReader != null) return;

        java.util.logging.Logger.getLogger("org.jline").setLevel(java.util.logging.Level.OFF);

        try {
            Terminal shared = TerminalUI.getJLineTerminal();
            if (shared != null) {
                terminal = shared;
                lineReader = LineReaderBuilder.builder().terminal(terminal).build();
                ownsTerminal = false;
                return;
            }

            Terminal t = TerminalBuilder.builder().system(true).build();
            if (org.jline.terminal.Terminal.TYPE_DUMB.equals(t.getType())
                    || org.jline.terminal.Terminal.TYPE_DUMB_COLOR.equals(t.getType())) {
                t.close();
                jlineAvailable = false;
                return;
            }

            terminal = t;
            lineReader = LineReaderBuilder.builder().terminal(terminal).build();
            ownsTerminal = true;

        } catch (IOException | IllegalStateException e) {
            jlineAvailable = false;
        }
    }

    public static MyString readInput(String prompt) {
        initJLine();

        if (jlineAvailable && lineReader != null) {
            try {
                String input = lineReader.readLine(prompt);
                return new MyString(input == null ? "" : input.trim());
            } catch (UserInterruptException | EndOfFileException e) {
                System.out.println("\nInput cancelled.");
                return new MyString("");
            }
        }

        System.out.print(prompt);
        Scanner sc = new Scanner(System.in);
        return new MyString(sc.hasNextLine() ? sc.nextLine().trim() : "");
    }

    public static MyString readPassword(String prompt) {
        initJLine();

        if (jlineAvailable && lineReader != null) {
            try {
                String password = lineReader.readLine(prompt, '*');
                return new MyString(password == null ? "" : password);
            } catch (UserInterruptException | EndOfFileException e) {
                System.out.println("\nInput cancelled.");
                return new MyString("");
            }
        }

        Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword(prompt);
            return new MyString(chars == null ? "" : new String(chars));
        }

        System.out.print(prompt + "(warning: input visible) ");
        Scanner sc = new Scanner(System.in);
        return new MyString(sc.hasNextLine() ? sc.nextLine() : "");
    }

    public static void closeTerminal() {
        if (terminal != null) {
            try {
                if (ownsTerminal) {
                    terminal.close();
                }
            } catch (IOException e) {
                System.err.println("[AuthController] Error closing terminal: " + e.getMessage());
            } finally {
                terminal = null;
                lineReader = null;
                ownsTerminal = false;
            }
        }
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

    private MyString getFilePath(MyString role) {
        String roleStr = normalizeRole(role).getValue();

        return switch (roleStr) {
            case "ADMIN" -> DATA_DIR.concat(new MyString("admin.txt"));
            case "STUDENT" -> DATA_DIR.concat(new MyString("students.txt"));
            case "HALL_ATTENDANT" -> DATA_DIR.concat(new MyString("hall_attendants.txt"));
            case "MAINTENANCE_WORKER" -> DATA_DIR.concat(new MyString("maintenance_workers.txt"));
            case "STORE_IN_CHARGE" -> DATA_DIR.concat(new MyString("store_in_charges.txt"));
            case "HALL_OFFICER" -> DATA_DIR.concat(new MyString("hall_officers.txt"));
            case "CAFETERIA_MANAGER" -> DATA_DIR.concat(new MyString("cafeteria_managers.txt"));
            default -> DATA_DIR.concat(new MyString(roleStr.toLowerCase())).concat(new MyString("s.txt"));
        };
    }

    public boolean authenticateUser(MyString userId, MyString password, MyString role) {
        try {
            MyString normalizedRole = normalizeRole(role);
            File file = new File(getFilePath(normalizedRole).getValue());
            if (!file.exists()) return false;

            MyString hashedInput = HashFunction.hashPassword(password);

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    MyString[] parts = new MyString(line).split('|');

                    if (parts.length > 4
                            && parts[0].getValue().trim().equals(userId.getValue().trim())) {
                        return parts[4].getValue().equals(hashedInput.getValue());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Auth Error: " + e.getMessage());
        }
        return false;
    }

    public boolean verifyPassword(MyString userId, MyString password, MyString role) {
        return authenticateUser(userId, password, role);
    }

    public MyString resolveUserId(MyString userIdOrName, MyString role) {
        if (userIdOrName == null || userIdOrName.getValue().trim().isEmpty()) {
            return new MyString("");
        }

        try {
            MyString normalizedRole = normalizeRole(role);
            File file = new File(getFilePath(normalizedRole).getValue());
            if (!file.exists()) return new MyString("");

            String needle = userIdOrName.getValue().trim();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                MyString firstNameMatch = null;

                while ((line = reader.readLine()) != null) {
                    MyString[] parts = new MyString(line).split('|');
                    if (parts.length < 2) {
                        continue;
                    }

                    String id = parts[0].getValue().trim();
                    String name = parts[1].getValue().trim();

                    if (id.equals(needle)) {
                        return new MyString(id);
                    }

                    if (firstNameMatch == null && name.equals(needle)) {
                        firstNameMatch = new MyString(id);
                    }
                }

                return firstNameMatch == null ? new MyString("") : firstNameMatch;
            }
        } catch (IOException e) {
            System.err.println("Resolve User ID Error: " + e.getMessage());
            return new MyString("");
        }
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

    public User getUserById(MyString userId, MyString role) {
        try {
            MyString normalizedRole = normalizeRole(role);
            File file = new File(getFilePath(normalizedRole).getValue());
            if (!file.exists()) return null;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    MyString[] parts = new MyString(line).split('|');
                    if (parts.length > 0
                            && parts[0].getValue().trim().equals(userId.getValue().trim())) {
                        return createUserFromParts(parts, normalizedRole.getValue());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Read Error: " + e.getMessage());
        }
        return null;
    }

    public User getUserByUsername(MyString username, MyString role) {
        MyString resolvedId = resolveUserId(username, role);
        if (resolvedId.getValue().isEmpty()) {
            return null;
        }
        return getUserById(resolvedId, role);
    }

    public boolean changePassword(MyString userId, MyString role, MyString oldPass, MyString newPass) {
        MyString normalizedRole = normalizeRole(role);

        if (!authenticateUser(userId, oldPass, normalizedRole)) {
            return false;
        }

        File file = new File(getFilePath(normalizedRole).getValue());
        if (!file.exists()) return false;

        MyString newHash = HashFunction.hashPassword(newPass);

        boolean found = false;
        boolean updated = false;
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                MyString[] parts = new MyString(line).split('|');

                if (parts.length > 4
                        && parts[0].getValue().trim().equals(userId.getValue().trim())) {
                    found = true;
                    parts[4] = newHash;
                    line = reconstructLine(parts);
                    updated = true;
                }

                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("Password Change Error: " + e.getMessage());
            return false;
        }

        if (!found || !updated) {
            return false;
        }

        writeToFile(file, sb.toString());
        return true;
    }

    public boolean updatePhoneNumber(MyString userId, MyString role, MyString newPhone) {
        MyString normalizedRole = normalizeRole(role);

        File file = new File(getFilePath(normalizedRole).getValue());
        if (!file.exists()) return false;

        boolean found = false;
        boolean updated = false;
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                MyString[] parts = new MyString(line).split('|');

                if (parts.length > 5
                        && parts[0].getValue().trim().equals(userId.getValue().trim())) {
                    found = true;
                    parts[5] = newPhone;
                    line = reconstructLine(parts);
                    updated = true;
                }

                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("Phone Update Error: " + e.getMessage());
            return false;
        }

        if (!found || !updated) {
            return false;
        }

        writeToFile(file, sb.toString());
        return true;
    }

    private User createUserFromParts(MyString[] parts, String role) {
        String normalizedRole = normalizeRole(role);

        String id = parts[0].getValue();
        String name = parts[1].getValue();
        String hash = parts.length > 4 ? parts[4].getValue() : "";
        String phone = parts.length > 5 ? parts[5].getValue() : "";

        switch (normalizedRole) {
            case "STUDENT":
                String dept = parts.length > 3 ? parts[3].getValue() : "N/A";
                String email = parts.length > 6 ? parts[6].getValue() : "N/A";
                Student s = new Student(id, name, normalizedRole, hash, phone, email);
                s.setDepartment(dept);
                if (parts.length > 7) s.setRoomNumber(parts[7].getValue());
                return s;

            case "HALL_ATTENDANT":
                String haEmail = parts.length > 6 ? parts[6].getValue() : "N/A";
                return new HallAttendant(id, name, normalizedRole, hash, phone, haEmail);

            case "MAINTENANCE_WORKER":
                String fieldStr = parts.length > 6 ? parts[6].getValue() : "ELECTRICIAN";
                WorkerField field;
                try {
                    field = WorkerField.valueOf(fieldStr);
                } catch (IllegalArgumentException e) {
                    field = WorkerField.ELECTRICIAN;
                }
                return new MaintenanceWorker(id, name, normalizedRole, hash, phone, field);

            case "STORE_IN_CHARGE":
                return new StoreInCharge(id, name, normalizedRole, hash, phone);

            case "HALL_OFFICER":
                String hoEmail = parts.length > 6 ? parts[6].getValue() : "N/A";
                return new HallOfficer(id, name, normalizedRole, hash, phone, hoEmail);

            case "ADMIN":
                return new SystemAdmin(id, name, normalizedRole, hash, phone);

            case "CAFETERIA_MANAGER":
                return new CafeteriaManager(id, name, normalizedRole, hash, phone);

            default:
                return null;
        }
    }

    private String reconstructLine(MyString[] parts) {
        MyString result = new MyString("");
        for (int i = 0; i < parts.length; i++) {
            result = result.concat(parts[i]);
            if (i < parts.length - 1) {
                result = result.concat(new MyString("|"));
            }
        }
        return result.getValue();
    }

    private void writeToFile(File file, String content) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.print(content);
        } catch (IOException e) {
            System.err.println("Write Error: " + e.getMessage());
        }
    }
}
