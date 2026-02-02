package cli.forms.account;

import controllers.account.CreateAccountController;
import controllers.account.RoleMapper;
import libraries.collections.MyString;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.InputHelper;

public class CreateAccount {

    private final CreateAccountController controller;

    public CreateAccount(CreateAccountController controller) {
        this.controller = controller;
    }

    public void show() {
        ConsoleUtil.clearScreen();

        System.out.println("-----------------------------------------------------------------------");
        System.out.println("|                           Create New Account                        |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("| 1. Student                                                          |");
        System.out.println("| 2. Attendant                                                        |");
        System.out.println("| 3. Maintenance Worker                                               |");
        System.out.println("| 4. Store-in-Charge                                                  |");
        System.out.println("| 5. Hall Office                                                      |");
        System.out.println("| 6. Admin                                                            |");
        System.out.println("| 0. Back                                                             |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.print("Enter choice: ");

        int choice = FastInput.readInt();
        if (choice == 0) return;

        MyString role = RoleMapper.getRoleFromChoice(choice);
        if (!controller.isValidRole(role)) {
            System.out.println("Invalid role choice!");
            return;
        }

        System.out.print("Enter User ID: ");
        String id = FastInput.readNonEmptyLine();

        // (optional) quick UI-side check before asking many fields
        if (controller.userExists(new MyString(id), role)) {
            System.out.println("Error: User ID already exists!");
            return;
        }

        System.out.print("Enter Full Name: ");
        String name = FastInput.readNonEmptyLine();

        System.out.print("Enter Password: ");
        String rawPass = InputHelper.readPassword().getValue();

        System.out.print("Enter Phone: ");
        String phone = FastInput.readNonEmptyLine();

        String dept = null;
        String email = null;
        Integer workerFieldChoice = null;

        String roleStr = role.getValue();

        if (roleStr.equals("STUDENT")) {
            System.out.print("Enter Department: ");
            dept = FastInput.readNonEmptyLine();

            System.out.print("Enter Email: ");
            email = FastInput.readNonEmptyLine();

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

            workerFieldChoice = FastInput.readInt();

        } else if (roleStr.equals("HALL_ATTENDANT") || roleStr.equals("HALL_OFFICER")) {
            System.out.print("Enter Email: ");
            email = FastInput.readNonEmptyLine();
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
    }
}
