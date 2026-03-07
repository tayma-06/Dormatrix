package controllers.account;

import controllers.authentication.AccountManager;
import libraries.collections.MyString;
import utils.TerminalUI;

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
            TerminalUI.tError("Invalid role choice!");
            return;
        }

        MyString filename = manager.getFilename(role);
        File file = new File(filename.getValue());

        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle(role.getValue());
        TerminalUI.tBoxSep();

        if (!file.exists()) {
            TerminalUI.tBoxLine("(No records found)");
            TerminalUI.tBoxBottom();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                MyString[] parts = new MyString(line).split('|');
                if (parts.length >= 2) {
                    TerminalUI.tBoxLine("ID: " + parts[0].getValue() +
                            " | Name: " + parts[1].getValue());
                }
            }
        } catch (IOException e) {
            TerminalUI.tError("Error reading file.");
            return;
        }

        TerminalUI.tBoxBottom();
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
