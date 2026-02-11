package cli.forms.account;

import controllers.account.CreateAccountController;
import utils.RoleMapper;
import libraries.collections.MyString;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.InputHelper;
import exceptions.account.*;

public class CreateAccount {

    private final CreateAccountController controller;

    public CreateAccount(CreateAccountController controller) {
        this.controller = controller;
    }

    public void show() {
        ConsoleUtil.clearScreen();

        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("║                           CREATE NEW ACCOUNT                        ║");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("║ [1] Student                                                         ║");
        System.out.println("║ [2] Attendant                                                       ║");
        System.out.println("║ [3] Maintenance Worker                                              ║");
        System.out.println("║ [4] Store-in-Charge                                                 ║");
        System.out.println("║ [5] Hall Office                                                     ║");
        System.out.println("║ [6] Admin                                                           ║");
        System.out.println("║ [7] Cafeteria Manager                                               ║");
        System.out.println("║ [0] Back                                                            ║");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println();
        System.out.print("Enter choice: ");

        int choice = FastInput.readInt();
        if (choice == 0) return;

        MyString role = RoleMapper.getRoleFromChoice(choice);
        if (!controller.isValidRole(role)) {
            System.out.println("Invalid role choice!");
            return;
        }

        String id;
        while (true) {
            System.out.print("Enter User ID: ");
            id = FastInput.readNonEmptyLine();

            if (controller.userExists(new MyString(id), role)) {
                System.out.println("Error: User ID already exists! Please enter a different User ID.");
            } else {
                break;
            }
        }

        try {
            System.out.print("Enter Full Name: ");
            String name = FastInput.readNonEmptyLine();

            String rawPass;
            while (true) {
                System.out.print("Enter Password: ");
                rawPass = InputHelper.readPassword().getValue();

                try {
                    controller.isValidPassword(rawPass);
                    break;
                } catch (InvalidPasswordException e) {
                    System.out.println(e.getMessage());
                }
            }

            String phone;
            while (true) {
                System.out.print("Enter Phone: ");
                phone = FastInput.readNonEmptyLine();

                try {
                    controller.isValidPhone(phone);
                    break;
                } catch (InvalidPhoneException e) {
                    System.out.println(e.getMessage());
                }
            }

            String dept = null;
            String email = null;
            Integer workerFieldChoice = null;

            String roleStr = role.getValue();

            if (roleStr.equals("STUDENT")) {
                while (true) {
                    System.out.print("Enter Department: ");
                    dept = FastInput.readNonEmptyLine();

                    try {
                        controller.isValidDepartment(dept);
                        break;
                    } catch (InvalidDepartmentException e) {
                        System.out.println(e.getMessage());
                    }
                }
                while (true) {
                    System.out.print("Enter Email: ");
                    email = FastInput.readNonEmptyLine();

                    try {
                        controller.isValidEmail(email, "STUDENT");
                        break;
                    } catch (InvalidEmailException e) {
                        System.out.println(e.getMessage());
                    }
                }

            } else if (roleStr.equals("MAINTENANCE_WORKER")) {
                System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
                System.out.println("║                        SELECT WORKER FIELD                          ║");
                System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
                System.out.println("║ [1] Electrician                                                     ║");
                System.out.println("║ [2] Plumber                                                         ║");
                System.out.println("║ [3] Internet Technician                                             ║");
                System.out.println("║ [4] Cleaning Staff                                                  ║");
                System.out.println("║ [5] Security                                                        ║");
                System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

                System.out.print("Enter Field Choice: ");
                workerFieldChoice = FastInput.readInt();

            } else if (roleStr.equals("HALL_ATTENDANT") || roleStr.equals("HALL_OFFICER")) {
                while (true) {
                    System.out.print("Enter Email: ");
                    email = FastInput.readNonEmptyLine();

                    try {
                        controller.isValidEmail(email, roleStr);
                        break;
                    } catch (InvalidEmailException e) {
                        System.out.println(e.getMessage());
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

            System.out.println(message);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
