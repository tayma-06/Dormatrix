package cli.forms;

import controllers.authentication.AccountManager;
import libraries.collections.MyString;
import libraries.hashing.HashFunction;
import models.users.*;
import java.util.Scanner;

public class CreateAccount {
    private final AccountManager manager;
    private final Scanner scanner;

    public CreateAccount(AccountManager manager, Scanner scanner) {
        this.manager = manager;
        this.scanner = scanner;
    }
    public void show() {
        System.out.println("--- CREATE NEW ACCOUNT ---"); // clean the ui later
        System.out.println("Select role: 1. Student, 2. Hall Attendant, 3. Maintenance, 4. Store, 5. Officer, 6. Admin");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        MyString role = getRoleFromChoice(choice);
        if (role.equals(new MyString("UNKNOWN"))) {
            System.out.println("Invalid role!");
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
        if (role.equals(new MyString("STUDENT"))) {
            System.out.print("Enter Department: ");
            String dept = scanner.nextLine().trim();
            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();
            Student s = new Student(id.getValue(), name.getValue(), role.getValue(), pass.getValue(), phone.getValue());
            s.setDepartment(dept);
            s.setEmail(email);
            return s;
        } else if (role.equals(new MyString("ADMIN"))) {
            return new SystemAdmin(id.getValue(), name.getValue(), role.getValue(), pass.getValue(), phone.getValue());
        } else if (role.equals(new MyString("HALL_ATTENDANT"))) {
            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();
            return new HallAttendant(id.getValue(), name.getValue(), role.getValue(), pass.getValue(), phone.getValue(), email);
        } else if (role.equals(new MyString("MAINTENANCE_WORKER"))) {
            return new MaintenanceWorker(id.getValue(), name.getValue(), role.getValue(), pass.getValue(), phone.getValue());
        } else if (role.equals(new MyString("STORE_IN_CHARGE"))) {
            return new StoreInCharge(id.getValue(), name.getValue(), role.getValue(), pass.getValue(), phone.getValue());
        } else if (role.equals(new MyString("HALL_OFFICER"))) {
            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();
            return new HallOfficer(id.getValue(), name.getValue(), role.getValue(), pass.getValue(), phone.getValue(), email);
        }
        return null;
    }
    public static MyString getRoleFromChoice(int choice) {
        switch (choice) {
            case 1: return new MyString("STUDENT");
            case 2: return new MyString("HALL_ATTENDANT");
            case 3: return new MyString("MAINTENANCE_WORKER");
            case 4: return new MyString("STORE_IN_CHARGE");
            case 5: return new MyString("HALL_OFFICER");
            case 6: return new MyString("ADMIN");
            default: return new MyString("UNKNOWN");
        }
    }
}