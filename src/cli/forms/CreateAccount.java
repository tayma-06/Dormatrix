package cli.forms;

import controllers.authentication.AccountManager;
import libraries.collections.MyString;
import libraries.hashing.HashFunction;
import models.users.*;
import models.enums.WorkerField;
import java.util.Scanner;

public class CreateAccount {
    private final AccountManager manager;
    private final Scanner scanner;

    public CreateAccount(AccountManager manager, Scanner scanner) {
        this.manager = manager;
        this.scanner = scanner;
    }

    public void show() {
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("|                           Create New Account                        |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("| 1. Student                                                          |");
        System.out.println("| 2. Attendant                                                        |");
        System.out.println("| 3. Maintenance Worker                                               |");
        System.out.println("| 4. Store-in-Charge                                                  |");
        System.out.println("| 5. Hall Office                                                      |");
        System.out.println("| 6. Admin                                                            |");
        System.out.println("| 0. Exit                                                             |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.print("Enter choice: ");

        int choice = -1;
        if (scanner.hasNextInt()) {
            choice = scanner.nextInt();
            scanner.nextLine();
        } else {
            scanner.nextLine();
        }

        if (choice == 0) return;

        MyString role = getRoleFromChoice(choice);
        if (role.getValue().equals("UNKNOWN")) {
            System.out.println("Invalid role choice!");
            return;
        }
        System.out.print("Enter User ID: ");
        MyString id = new MyString(scanner.nextLine().trim());
        if (manager.userExists(id, role)) {
            System.out.println("Error: User ID already exists!");
            return;
        }
        System.out.print("Enter Full Name: ");
        MyString name = new MyString(scanner.nextLine().trim());

        System.out.print("Enter Password: ");
        MyString rawPass = new MyString(scanner.nextLine().trim());

        System.out.print("Enter Phone: ");
        MyString phone = new MyString(scanner.nextLine().trim());

        MyString hashedPass = HashFunction.hashPassword(rawPass);

        User newUser = buildUser(role, id, name, hashedPass, phone);

        if (newUser != null) {
            if (manager.registerUser(newUser, role)) {
                System.out.println("✓ Account created successfully!");
            } else {
                System.out.println("✗ System Error: Could not save file.");
            }
        }
    }

    private User buildUser(MyString role, MyString id, MyString name, MyString pass, MyString phone) {
        String roleStr = role.getValue();

        if (roleStr.equals("STUDENT")) {
            System.out.print("Enter Department: ");
            String dept = scanner.nextLine().trim();
            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();

            Student s = new Student(id.getValue(), name.getValue(), role.getValue(), pass.getValue(), phone.getValue());
            s.setDepartment(dept);
            s.setEmail(email);
            return s;

        } else if (roleStr.equals("MAINTENANCE_WORKER")) {
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("|                           Select Worker Field                       |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("| 1. Electrician                                                      |");
            System.out.println("| 2. Plumber                                                          |");
            System.out.println("| 3. Internet Technician                                              |");
            System.out.println("| 4. Cleaning Staff                                                   |");
            System.out.println("| 5. Security                                                         |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.print("Enter Field Choice: ");
            int fieldChoice = 1;
            if(scanner.hasNextInt()) {
                fieldChoice = scanner.nextInt();
                scanner.nextLine();
            } else {
                scanner.nextLine();
            }

            WorkerField field;
            switch (fieldChoice) {
                case 1 -> field = WorkerField.ELECTRICIAN;
                case 2 -> field = WorkerField.PLUMBER;
                case 3 -> field = WorkerField.INTERNET_TECH;
                case 4 -> field = WorkerField.CLEANING;
                case 5 -> field = WorkerField.SECURITY;
                default -> {
                    System.out.println("Invalid choice, defaulting to ELECTRICIAN.");
                    field = WorkerField.ELECTRICIAN;
                }
            }
            return new MaintenanceWorker(id.getValue(), name.getValue(), role.getValue(), pass.getValue(), phone.getValue(), field);

        } else if (roleStr.equals("HALL_ATTENDANT")) {
            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();
            return new HallAttendant(id.getValue(), name.getValue(), role.getValue(), pass.getValue(), phone.getValue(), email);

        } else if (roleStr.equals("STORE_IN_CHARGE")) {
            return new StoreInCharge(id.getValue(), name.getValue(), role.getValue(), pass.getValue(), phone.getValue());

        } else if (roleStr.equals("HALL_OFFICER")) {
            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();
            return new HallOfficer(id.getValue(), name.getValue(), role.getValue(), pass.getValue(), phone.getValue(), email);

        } else if (roleStr.equals("ADMIN")) {
            return new SystemAdmin(id.getValue(), name.getValue(), role.getValue(), pass.getValue(), phone.getValue());
        }

        return null;
    }

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