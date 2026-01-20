package cli.views;
import controllers.authentication.AccountManager;
import libraries.collections.MyString;
import java.util.Scanner;
public class SearchUser {
    private final AccountManager manager;
    private final Scanner scanner;
    public SearchUser(AccountManager manager, Scanner scanner) {
        this.manager = manager;
        this.scanner = scanner;
    }
    public void show() {
        System.out.println();
        System.out.println("---SEARCH USER---"); // will clean the ui later
        System.out.print("Enter User ID: ");
        MyString id = new MyString(scanner.nextLine().trim());
        String result = manager.findUserDetails(id);
        if (result != null) {
            System.out.println(result);
        } else {
            System.out.println("User not found in any role.");
        }
    }
}