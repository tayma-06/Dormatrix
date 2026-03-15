package cli.forms.account;

import controllers.account.CreateAccountController;
import controllers.authentication.AuthController;
import exceptions.account.InvalidDepartmentException;
import exceptions.account.InvalidEmailException;
import exceptions.account.InvalidPasswordException;
import exceptions.account.InvalidPhoneException;
import libraries.collections.MyString;
import utils.FastInput;
import utils.RoleMapper;

import static utils.TerminalUI.*;

public class CreateAccount {

    private final CreateAccountController controller;

    public CreateAccount(CreateAccountController controller) {
        this.controller = controller;
    }

    public void show() {
        fillBackground(getActiveBgColor());
        at(2, 1);

        tSubDashboard("CREATE NEW ACCOUNT", new String[]{
                "[1] Student",
                "[2] Attendant",
                "[3] Maintenance Worker",
                "[4] Store-in-Charge",
                "[5] Hall Office",
                "[6] Admin",
                "[7] Cafeteria Manager",
                "[0] Back"
        });

        int choice = FastInput.readInt();
        if (choice == 0) {
            return;
        }

        MyString role = RoleMapper.getRoleFromChoice(choice);
        if (!controller.isValidRole(role)) {
            fillBackground(getActiveBgColor());
            at(2, 1);
            tError("Invalid role choice!");
            tPause();
            return;
        }

        String roleLabel = getRoleLabel(role.getValue());
        fillBackground(getActiveBgColor());
        at(2, 1);

        tBoxTop();
        tBoxTitle("CREATE ACCOUNT");
        tBoxSep();
        tBoxLine("Selected Role: " + roleLabel);
        tBoxLine("Fill in the required information below.");
        tBoxBottom();
        tEmpty();

        String id;
        while (true) {
            tPrompt("Enter User ID: ");
            id = FastInput.readNonEmptyLine();

            if (controller.userExists(new MyString(id), role)) {
                tError("User ID already exists! Please enter a different one.");
                tEmpty();
            } else {
                break;
            }
        }

        try {
            tPrompt("Enter Full Name: ");
            String name = FastInput.readNonEmptyLine();

            String rawPass;
            while (true) {
                MyString password = AuthController.readPassword("Enter Password   : ");
                MyString confirm = AuthController.readPassword("Confirm Password : ");

                rawPass = password.getValue();

                if (!rawPass.equals(confirm.getValue())) {
                    tError("Password and confirm password do not match.");
                    tEmpty();
                    continue;
                }

                try {
                    controller.isValidPassword(rawPass);
                    break;
                } catch (InvalidPasswordException e) {
                    tError(e.getMessage());
                    tEmpty();
                }
            }

            String phone;
            while (true) {
                tPrompt("Enter Phone: ");
                phone = FastInput.readNonEmptyLine();

                try {
                    controller.isValidPhone(phone);
                    break;
                } catch (InvalidPhoneException e) {
                    tError(e.getMessage());
                    tEmpty();
                }
            }

            String dept = null;
            String email = null;
            Integer workerFieldChoice = null;

            String roleStr = role.getValue();

            if (roleStr.equals("STUDENT")) {
                while (true) {
                    tPrompt("Enter Department: ");
                    dept = FastInput.readNonEmptyLine();

                    try {
                        controller.isValidDepartment(dept);
                        break;
                    } catch (InvalidDepartmentException e) {
                        tError(e.getMessage());
                        tEmpty();
                    }
                }

                while (true) {
                    tPrompt("Enter Email: ");
                    email = FastInput.readNonEmptyLine();

                    try {
                        controller.isValidEmail(email, "STUDENT");
                        break;
                    } catch (InvalidEmailException e) {
                        tError(e.getMessage());
                        tEmpty();
                    }
                }

            } else if (roleStr.equals("MAINTENANCE_WORKER")) {
                fillBackground(getActiveBgColor());
                at(2, 1);

                tSubDashboard("SELECT WORKER FIELD", new String[]{
                        "[1] Electrician",
                        "[2] Plumber",
                        "[3] Internet Technician",
                        "[4] Cleaning Staff",
                        "[0] Back"
                });

                workerFieldChoice = FastInput.readInt();
                if (workerFieldChoice == 0) {
                    return;
                }

            } else if (roleStr.equals("HALL_ATTENDANT") || roleStr.equals("HALL_OFFICER")) {
                while (true) {
                    tPrompt("Enter Email: ");
                    email = FastInput.readNonEmptyLine();

                    try {
                        controller.isValidEmail(email, roleStr);
                        break;
                    } catch (InvalidEmailException e) {
                        tError(e.getMessage());
                        tEmpty();
                    }
                }
            }

            String message = controller.createAccount(
                    role,
                    id,
                    name,
                    rawPass,
                    phone,
                    dept,
                    email,
                    workerFieldChoice
            );

            fillBackground(getActiveBgColor());
            at(2, 1);

            if (message.toLowerCase().contains("success")) {
                tSuccess(message);
            } else {
                tError(message);
            }
            tPause();

        } catch (Exception e) {
            fillBackground(getActiveBgColor());
            at(2, 1);
            tError(e.getMessage());
            tPause();
        }
    }

    private String getRoleLabel(String role) {
        return switch (role) {
            case "STUDENT" -> "Student";
            case "HALL_ATTENDANT" -> "Attendant";
            case "MAINTENANCE_WORKER" -> "Maintenance Worker";
            case "STORE_IN_CHARGE" -> "Store-in-Charge";
            case "HALL_OFFICER" -> "Hall Office";
            case "ADMIN" -> "Admin";
            case "CAFETERIA_MANAGER" -> "Cafeteria Manager";
            default -> role;
        };
    }
}