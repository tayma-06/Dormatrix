package controllers.account;

import controllers.authentication.AccountManager;
import libraries.collections.MyString;

import java.io.*;

public class ViewAccountController {

    private final AccountManager manager;

    public ViewAccountController(AccountManager manager) {
        this.manager = manager;
    }

    public void handleViewChoice(int choice) {
        if (choice == 7) {
            viewAll();
        } else {
            viewRole(getRoleFromChoice(choice));
        }
    }

    private void viewRole(MyString role) {
        if (role.getValue().equals("UNKNOWN")) {
            System.out.println("Invalid role choice!");
            return;
        }

        MyString filename = manager.getFilename(role);
        File file = new File(filename.getValue());

        System.out.println();
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("                          " + role.getValue());
        System.out.println("-----------------------------------------------------------------------");

        if (!file.exists()) {
            System.out.println("(No records found)");
            System.out.println("-----------------------------------------------------------------------");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                MyString[] parts = new MyString(line).split('|');
                if (parts.length >= 2) {
                    System.out.println("ID: " + parts[0].getValue() +
                            " | Name: " + parts[1].getValue());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file.");
        }

        System.out.println("-----------------------------------------------------------------------");
    }

    private void viewAll() {
        MyString[] roles = {
                new MyString("STUDENT"),
                new MyString("HALL_ATTENDANT"),
                new MyString("MAINTENANCE_WORKER"),
                new MyString("STORE_IN_CHARGE"),
                new MyString("HALL_OFFICER"),
                new MyString("ADMIN")
        };

        for (MyString role : roles) {
            viewRole(role);
        }
    }

    private MyString getRoleFromChoice(int choice) {
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
