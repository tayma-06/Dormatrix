package controllers.account;

import controllers.authentication.AccountManager;
import libraries.collections.MyString;
import libraries.hashing.HashFunction;
import models.enums.WorkerField;
import models.users.*;

public class CreateAccountController {

    private final AccountManager manager;

    public CreateAccountController(AccountManager manager) {
        this.manager = manager;
    }

    public boolean isValidRole(MyString role) {
        return role != null && !role.getValue().equals("UNKNOWN");
    }

    public boolean userExists(MyString id, MyString role) {
        return manager.userExists(id, role);
    }

    public String createAccount(
            MyString role,
            String id,
            String name,
            String rawPassword,
            String phone,
            // optional fields:
            String department,
            String email,
            Integer workerFieldChoice
    ) {
        MyString userId = new MyString(id);
        if (!isValidRole(role)) return "Invalid role choice!";
        if (manager.userExists(userId, role)) return "Error: User ID already exists!";

        MyString hashedPass = HashFunction.hashPassword(new MyString(rawPassword));

        User user = buildUser(
                role,
                id,
                name,
                hashedPass.getValue(),
                phone,
                department,
                email,
                workerFieldChoice
        );

        if (user == null) return "Error: Could not build user (invalid data).";

        boolean saved = manager.registerUser(user, role);
        return saved ? "Account created successfully!" : "System Error: Could not save file.";
    }

    private User buildUser(
            MyString role,
            String id,
            String name,
            String hashedPass,
            String phone,
            String department,
            String email,
            Integer workerFieldChoice
    ) {
        String roleStr = role.getValue();

        if (roleStr.equals("STUDENT")) {
            Student s = new Student(id, name, roleStr, hashedPass, phone, email);
            s.setDepartment(department);
            return s;

        } else if (roleStr.equals("MAINTENANCE_WORKER")) {
            WorkerField field = mapWorkerField(workerFieldChoice);
            return new MaintenanceWorker(id, name, roleStr, hashedPass, phone, field);

        } else if (roleStr.equals("HALL_ATTENDANT")) {
            return new HallAttendant(id, name, roleStr, hashedPass, phone, email);

        } else if (roleStr.equals("STORE_IN_CHARGE")) {
            return new StoreInCharge(id, name, roleStr, hashedPass, phone);

        } else if (roleStr.equals("HALL_OFFICER")) {
            return new HallOfficer(id, name, roleStr, hashedPass, phone, email);

        } else if (roleStr.equals("ADMIN")) {
            return new SystemAdmin(id, name, roleStr, hashedPass, phone);
        }

        return null;
    }

    private WorkerField mapWorkerField(Integer fieldChoice) {
        if (fieldChoice == null) return WorkerField.ELECTRICIAN;

        return switch (fieldChoice) {
            case 1 -> WorkerField.ELECTRICIAN;
            case 2 -> WorkerField.PLUMBER;
            case 3 -> WorkerField.INTERNET_TECH;
            case 4 -> WorkerField.CLEANING;
            case 5 -> WorkerField.SECURITY;
            default -> WorkerField.ELECTRICIAN;
        };
    }
}
