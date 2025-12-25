package controllers.account;

import models.users.*;
import libraries.hashing.HashFunction;
import java.io.*;
import java.util.Scanner;

public class AccountManager {
    private static final String USER_DATA_PATH = "data/users/";
    private final Scanner scanner = new Scanner(System.in);

    // Create a new account
    public void createAccount() {
        System.out.println("\n=== CREATE NEW ACCOUNT ===");

        // Select role
        System.out.println("Select role:");
        System.out.println("1. Student");
        System.out.println("2. Hall Attendant");
        System.out.println("3. Maintenance Worker");
        System.out.println("4. Store In Charge");
        System.out.println("5. Hall Officer");
        System.out.println("6. Admin");
        System.out.print("Enter choice: ");
        int roleChoice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        String role = getRoleFromChoice(roleChoice);
        if (role.equals("UNKNOWN")) {
            System.out.println("Invalid role selection!");
            return;
        }

        // Get user details
        System.out.print("Enter User ID: ");
        String id = scanner.nextLine().trim();

        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        System.out.print("Enter Phone Number: ");
        String phoneNumber = scanner.nextLine().trim();

        // Create user based on role
        User user = null;
        String filename = USER_DATA_PATH + role.toLowerCase() + "s.txt";

        // Check if user already exists
        if (userExists(id, role)) {
            System.out.println("Error: User with ID '" + id + "' already exists!");
            return;
        }

        try {
            // Hash password
            String hashedPassword = HashFunction.hashPassword(password);

            // Create user object
            switch (role) {
                case "STUDENT":
                    System.out.print("Enter Department: ");
                    String department = scanner.nextLine().trim();
                    System.out.print("Enter Email: ");
                    String email = scanner.nextLine().trim();

                    user = new Student(id, name, role, hashedPassword, phoneNumber);
                    ((Student) user).setDepartment(department);
                    ((Student) user).setEmail(email);
                    break;

                case "HALL_ATTENDANT":
                    System.out.print("Enter Email: ");
                    String attendantEmail = scanner.nextLine().trim();
                    user = new HallAttendant(id, name, role, hashedPassword, phoneNumber, attendantEmail);
                    break;

                case "MAINTENANCE_WORKER":
                    user = new MaintenanceWorker(id, name, role, hashedPassword, phoneNumber);
                    break;

                case "STORE_IN_CHARGE":
                    user = new StoreInCharge(id, name, role, hashedPassword, phoneNumber);
                    break;

                case "HALL_OFFICER":
                    System.out.print("Enter Email: ");
                    String officerEmail = scanner.nextLine().trim();
                    user = new HallOfficer(id, name, role, hashedPassword, phoneNumber, officerEmail);
                    break;

                case "ADMIN":
                    user = new SystemAdmin(id, name, role, hashedPassword, phoneNumber);
                    break;
            }

            // Save to file
            saveUserToFile(user, filename);
            System.out.println("\n✓ Account created successfully!");
            System.out.println("User ID: " + id);
            System.out.println("Role: " + role);

        } catch (Exception e) {
            System.out.println("Error creating account: " + e.getMessage());
        }
    }

    // Delete an account
    public void deleteAccount() {
        System.out.println("\n=== DELETE ACCOUNT ===");

        // Select role
        System.out.println("Select role of account to delete:");
        System.out.println("1. Student");
        System.out.println("2. Hall Attendant");
        System.out.println("3. Maintenance Worker");
        System.out.println("4. Store In Charge");
        System.out.println("5. Hall Officer");
        System.out.println("6. Admin");
        System.out.print("Enter choice: ");
        int roleChoice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        String role = getRoleFromChoice(roleChoice);
        if (role.equals("UNKNOWN")) {
            System.out.println("Invalid role selection!");
            return;
        }

        System.out.print("Enter User ID to delete: ");
        String id = scanner.nextLine().trim();

        String filename = USER_DATA_PATH + role.toLowerCase() + "s.txt";
        File file = new File(filename);

        if (!file.exists()) {
            System.out.println("Error: No users found for role '" + role + "'");
            return;
        }

        try {
            // Read all users except the one to delete
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            boolean found = false;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length > 0 && parts[0].equals(id)) {
                    found = true;
                    continue; // Skip this line (delete)
                }
                content.append(line).append("\n");
            }
            reader.close();

            if (!found) {
                System.out.println("Error: User with ID '" + id + "' not found!");
                return;
            }

            // Write back to file
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            writer.print(content.toString());
            writer.close();

            System.out.println("\n✓ Account deleted successfully!");
            System.out.println("Deleted User ID: " + id);
            System.out.println("Role: " + role);

        } catch (IOException e) {
            System.out.println("Error deleting account: " + e.getMessage());
        }
    }

    // View accounts by role
    public void viewAccountsByRole() {
        System.out.println("\n=== VIEW ACCOUNTS BY ROLE ===");

        System.out.println("Select role to view:");
        System.out.println("1. Student");
        System.out.println("2. Hall Attendant");
        System.out.println("3. Maintenance Worker");
        System.out.println("4. Store In Charge");
        System.out.println("5. Hall Officer");
        System.out.println("6. Admin");
        System.out.println("7. View All Roles");
        System.out.print("Enter choice: ");
        int roleChoice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (roleChoice == 7) {
            viewAllAccounts();
        } else {
            String role = getRoleFromChoice(roleChoice);
            if (role.equals("UNKNOWN")) {
                System.out.println("Invalid role selection!");
                return;
            }
            viewAccountsForRole(role);
        }
    }

    // View accounts for a specific role
    private void viewAccountsForRole(String role) {
        String filename = USER_DATA_PATH + role.toLowerCase() + "s.txt";
        File file = new File(filename);

        if (!file.exists()) {
            System.out.println("No accounts found for role: " + role);
            return;
        }

        System.out.println("\n=== " + role + " ACCOUNTS ===");
        System.out.println("----------------------------------------------------------------");
        System.out.printf("%-15s %-25s %-15s\n", "User ID", "Name", "Phone");
        System.out.println("----------------------------------------------------------------");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    String id = parts[0];
                    String name = parts[1];
                    String phone = parts[5];
                    System.out.printf("%-15s %-25s %-15s\n", id, name, phone);
                    count++;
                }
            }
            reader.close();

            System.out.println("----------------------------------------------------------------");
            System.out.println("Total accounts: " + count);

        } catch (IOException e) {
            System.out.println("Error reading accounts: " + e.getMessage());
        }
    }

    // View all accounts from all roles
    private void viewAllAccounts() {
        System.out.println("\n=== ALL SYSTEM ACCOUNTS ===");

        String[] roles = {"STUDENT", "HALL_ATTENDANT", "MAINTENANCE_WORKER",
                "STORE_IN_CHARGE", "HALL_OFFICER", "ADMIN"};
        int totalAccounts = 0;

        for (String role : roles) {
            String filename = USER_DATA_PATH + role.toLowerCase() + "s.txt";
            File file = new File(filename);

            if (!file.exists()) continue;

            System.out.println("\n--- " + role + " ---");
            System.out.println("----------------------------------------------------------------");
            System.out.printf("%-15s %-25s %-15s\n", "User ID", "Name", "Phone");
            System.out.println("----------------------------------------------------------------");

            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                int count = 0;

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 6) {
                        String id = parts[0];
                        String name = parts[1];
                        String phone = parts[5];
                        System.out.printf("%-15s %-25s %-15s\n", id, name, phone);
                        count++;
                    }
                }
                reader.close();

                System.out.println("Total: " + count + " accounts");
                totalAccounts += count;

            } catch (IOException e) {
                System.out.println("Error reading " + role + " accounts: " + e.getMessage());
            }
        }

        System.out.println("\n================================================");
        System.out.println("TOTAL ACCOUNTS IN SYSTEM: " + totalAccounts);
    }

    // Check if user exists
    private boolean userExists(String userId, String role) {
        String filename = USER_DATA_PATH + role.toLowerCase() + "s.txt";
        File file = new File(filename);

        if (!file.exists()) return false;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length > 0 && parts[0].equals(userId)) {
                    reader.close();
                    return true;
                }
            }
            reader.close();
        } catch (IOException e) {
            // If we can't read the file, assume user doesn't exist
        }

        return false;
    }

    // Save user to file
    private void saveUserToFile(User user, String filename) throws IOException {
        // Create directory if it doesn't exist
        File directory = new File(USER_DATA_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Append user to file
        FileWriter writer = new FileWriter(filename, true);
        writer.write(user.toFileString() + "\n");
        writer.close();
    }

    // Helper method to get role from choice
    private String getRoleFromChoice(int choice) {
        return switch (choice) {
            case 1 -> "STUDENT";
            case 2 -> "HALL_ATTENDANT";
            case 3 -> "MAINTENANCE_WORKER";
            case 4 -> "STORE_IN_CHARGE";
            case 5 -> "HALL_OFFICER";
            case 6 -> "ADMIN";
            default -> "UNKNOWN";
        };
    }

    // Method to search for a user by ID across all roles
    public void searchUserById() {
        System.out.println("\n=== SEARCH USER BY ID ===");
        System.out.print("Enter User ID to search: ");
        String searchId = scanner.nextLine().trim();

        String[] roles = {"STUDENT", "HALL_ATTENDANT", "MAINTENANCE_WORKER",
                "STORE_IN_CHARGE", "HALL_OFFICER", "ADMIN"};

        boolean found = false;

        for (String role : roles) {
            String filename = USER_DATA_PATH + role.toLowerCase() + "s.txt";
            File file = new File(filename);

            if (!file.exists()) continue;

            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length > 0 && parts[0].equals(searchId)) {
                        System.out.println("\n✓ User Found!");
                        System.out.println("Role: " + role);
                        System.out.println("ID: " + parts[0]);
                        System.out.println("Name: " + parts[1]);
                        if (parts.length > 3 && !parts[3].isEmpty()) {
                            System.out.println("Department: " + parts[3]);
                        }
                        System.out.println("Phone: " + (parts.length > 5 ? parts[5] : "N/A"));
                        if (parts.length > 6 && !parts[6].isEmpty()) {
                            System.out.println("Email: " + parts[6]);
                        }
                        found = true;
                        break;
                    }
                }
                reader.close();
                if (found) break;

            } catch (IOException e) {
                // Continue with next role
            }
        }

        if (!found) {
            System.out.println("\n✗ User with ID '" + searchId + "' not found in any role.");
        }
    }
}