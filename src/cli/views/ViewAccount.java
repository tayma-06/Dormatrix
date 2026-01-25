package cli.views;
import cli.forms.CreateAccount;
import controllers.authentication.AccountManager;
import libraries.collections.MyString;
import java.io.*;
import java.util.Scanner;
public class ViewAccount {
    private final AccountManager manager;
    private final Scanner scanner;
    public ViewAccount(AccountManager manager, Scanner scanner) {
        this.manager = manager;
        this.scanner = scanner;
    }
    public void show() {
        System.out.println("\n=== VIEW ACCOUNTS ===");
        System.out.println("1-6: Specific Role, 7: View All");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("|                          View Accounts                              |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("| 1. Student                                                          |");
        System.out.println("| 2. Attendant                                                        |");
        System.out.println("| 3. Maintenance Worker                                               |");
        System.out.println("| 4. Store-in-Charge                                                  |");
        System.out.println("| 5. Hall Office                                                      |");
        System.out.println("| 6. Admin                                                            |");
        System.out.println("| 7. View All Accounts                                                |");
        System.out.println("| 0. Exit                                                             |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        if (choice == 7) {
            viewAll();
        } else {
            viewRole(CreateAccount.getRoleFromChoice(choice));
        }
    }
    private void viewRole(MyString role) {
        MyString filename = manager.getFilename(role);
        File file = new File(filename.getValue());
        System.out.println();
        System.out.println("------------- " + role.getValue() + " -------------");
//        if (!file.exists()) {
//            System.out.println("(No records found)");
//            return;
//        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                MyString[] parts = new MyString(line).split('|');
                if (parts.length >= 2) {
                    System.out.println("ID: " + parts[0].getValue() + " | Name: " + parts[1].getValue());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file.");
        }
        System.out.println("---------------------------------------------------");
    }
    private void viewAll() {
        MyString[] roles = {
                new MyString("STUDENT"), new MyString("HALL_ATTENDANT"),
                new MyString("MAINTENANCE_WORKER"), new MyString("STORE_IN_CHARGE"),
                new MyString("HALL_OFFICER"), new MyString("ADMIN")
        };
        for (MyString r : roles) viewRole(r);
    }
}