package controllers.account;

import controllers.authentication.AccountManager;
import libraries.collections.MyString;
import libraries.hashing.HashFunction;
import models.enums.WorkerField;
import models.users.*;
import exceptions.account.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public boolean isValidEmail(String email, String role) throws InvalidEmailException {
        if (role.equals("MAINTENANCE_WORKER") ||
                role.equals("STORE_IN_CHARGE") ||
                role.equals("CAFETERIA_MANAGER")) {
            return true;
        }

        String emailRegex = "";
        if (role.equals("STUDENT") || role.equals("ADMIN")) {
            emailRegex = "^[a-zA-Z0-9_+&*-]+@iut-dhaka\\.edu$";
        } else if (role.equals("HALL_ATTENDANT") || role.equals("HALL_OFFICER")) {
            emailRegex = "^[a-zA-Z0-9_+&*-]+@iut-dhaka\\.com$";
        } else {
            throw new InvalidEmailException("Error: Invalid email format!");
        }
        if (email == null) {
            throw new InvalidEmailException("Error: Email is required for this role!");
        }

        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            throw new InvalidEmailException("Error: Invalid email format!");
        }
        return true;
    }

    public boolean isValidPhone(String phone) throws InvalidPhoneException {
        String phoneRegex = "^\\+8801[1-9][0-9]{8}$|^017[0-9]{8}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        Matcher matcher = pattern.matcher(phone);
        if (!matcher.matches()) {
            throw new InvalidPhoneException("Error: Invalid phone number format!");
        }
        return true;
    }

    public boolean isValidPassword(String password) throws InvalidPasswordException {
        if (password.length() < 6 || !password.matches(".*\\d.*")) {
            throw new InvalidPasswordException("Error: Password must be at least 6 characters long and contain at least one number!");
        }
        return true;
    }

    public boolean isValidDepartment(String department) throws InvalidDepartmentException {
        if (!(department.equals("EEE") || department.equals("CSE") || department.equals("ME") || department.equals("IPE") ||
                department.equals("SWE") || department.equals("CEE") || department.equals("BTM"))) {
            throw new InvalidDepartmentException("Error: Invalid department! Allowed: EEE, CSE, ME, IPE, SWE, CEE, BTM");
        }
        return true;
    }

    public String createAccount(
            MyString role,
            String id,
            String name,
            String rawPassword,
            String phone,
            String department,
            String email,
            Integer workerFieldChoice
    ) {
        try {
            if (!isValidRole(role)) throw new IllegalArgumentException("Invalid role choice!");
            if (userExists(new MyString(id), role)) throw new UserAlreadyExistsException("Error: User ID already exists!");

            isValidEmail(email, role.getValue());
            isValidPhone(phone);
            isValidPassword(rawPassword);
            if (role.getValue().equals("STUDENT")) {
                isValidDepartment(department);
            }

            MyString hashedPass = HashFunction.hashPassword(new MyString(rawPassword));
            User user = buildUser(role, id, name, hashedPass.getValue(), phone, department, email, workerFieldChoice);
            if (user == null) return "Error: Could not build user (invalid data).";

            boolean saved = manager.registerUser(user, role);
            return saved ? "Account created successfully!" : "System Error: Could not save file.";
        } catch (Exception e) {
            return e.getMessage();
        }
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
        } else if (roleStr.equals("CAFETERIA_MANAGER")) {
            return new models.users.CafeteriaManager(id, name, roleStr, hashedPass, phone);
        }else if (roleStr.equals("ADMIN")) {
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
//            case 5 -> WorkerField.SECURITY;
            default -> WorkerField.ELECTRICIAN;
        };
    }
}
