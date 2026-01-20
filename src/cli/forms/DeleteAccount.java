package cli.forms;
import controllers.authentication.AccountManager;
import libraries.collections.MyString;
import java.util.Scanner;
public class DeleteAccount {
    private final AccountManager manager;
    private final Scanner scanner;
    public DeleteAccount(AccountManager manager, Scanner scanner) {
        this.manager = manager;
        this.scanner = scanner;
    }
    public void show() {
        System.out.println("\n=== DELETE ACCOUNT ===");
        System.out.println("Select role (1-6): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        MyString role = CreateAccount.getRoleFromChoice(choice);
        System.out.print("Enter User ID to delete: ");
        MyString id = new MyString(scanner.nextLine().trim());
        if (manager.deleteUser(id, role)) {
            System.out.println("✓ Account deleted.");
        } else {
            System.out.println("✗ User not found.");
        }
    }
}