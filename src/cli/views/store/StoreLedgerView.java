package cli.views.store;

import controllers.balance.BalanceController;
import controllers.store.DueController;
import controllers.store.PurchaseHistoryController;
import utils.ConsoleUtil;
import utils.FastInput;

public class StoreLedgerView {

    private final BalanceController balanceController;
    private final DueController dueController;
    private final PurchaseHistoryController purchaseHistoryController;

    public StoreLedgerView() {
        this.balanceController = new BalanceController();
        this.dueController = new DueController();
        this.purchaseHistoryController = new PurchaseHistoryController();
    }

    public void show(String studentId) {
        while (true) {
            ConsoleUtil.clearScreen();
            displayHeader(studentId);
            displayMenu();

            String choice = FastInput.readLine();

            switch (choice) {
                case "1":
                    viewBalance(studentId);
                    break;
                case "2":
                    addBalance(studentId);
                    break;
                case "3":
                    viewDues(studentId);
                    break;
                case "4":
                    payDues(studentId);
                    break;
                case "5":
                    viewPurchaseHistory(studentId);
                    break;
                case "6":
                    viewRecentPurchases(studentId);
                    break;
                case "0":
                    ConsoleUtil.clearScreen();
                    return;
                default:
                    System.out.println("✗ Invalid choice!");
            }

            if (!choice.equals("0")) {
                System.out.println("\nPress Enter to continue...");
                FastInput.readLine();
            }
        }
    }

    private void displayHeader(String studentId) {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("|                      STORE ACCOUNT & LEDGER                         |");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.printf("  Student ID: %s\n", studentId);

        double balance = balanceController.getBalance(studentId);
        double dues = dueController.getDue(studentId);

        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.printf("  Current Balance: $%.2f\n", balance);
        System.out.printf("  Outstanding Dues: $%.2f", dues);

        if (dues > 0) {
            System.out.println(" ⚠");
        } else {
            System.out.println(" ✓");
        }
        System.out.println("═══════════════════════════════════════════════════════════════════════");
    }

    private void displayMenu() {
        System.out.println("\nOptions:");
        System.out.println("  [1] View Balance Details");
        System.out.println("  [2] Add Balance");
        System.out.println("  [3] View Outstanding Dues");
        System.out.println("  [4] Pay Dues");
        System.out.println("  [5] View Complete Purchase History");
        System.out.println("  [6] View Recent Purchases");
        System.out.println("  [0] Back");
        System.out.print("\nEnter your choice: ");
    }

    private void viewBalance(String studentId) {
        double balance = balanceController.getBalance(studentId);

        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("|                         BALANCE DETAILS                             |");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.printf("  Student ID: %s\n", studentId);
        System.out.printf("  Current Balance: $%.2f\n", balance);

        if (balance < 100) {
            System.out.println("  Status: ⚠ Low Balance - Consider adding funds");
        } else {
            System.out.println("  Status: ✓ Sufficient Balance");
        }
        System.out.println("═══════════════════════════════════════════════════════════════════════");
    }

    private void addBalance(String studentId) {
        System.out.println("\n--- Add Balance ---");

        double currentBalance = balanceController.getBalance(studentId);
        System.out.printf("Current Balance: $%.2f\n", currentBalance);

        System.out.print("Enter amount to add: $");
        double amount = FastInput.readDouble();

        if (amount <= 0) {
            System.out.println("✗ Invalid amount!");
            return;
        }

        balanceController.addBalance(studentId, amount);
        double newBalance = balanceController.getBalance(studentId);

        System.out.println("\n✓ Balance added successfully!");
        System.out.printf("  Previous Balance: $%.2f\n", currentBalance);
        System.out.printf("  Amount Added:     $%.2f\n", amount);
        System.out.printf("  New Balance:      $%.2f\n", newBalance);
    }

    private void viewDues(String studentId) {
        double dues = dueController.getDue(studentId);

        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("|                       OUTSTANDING DUES                              |");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.printf("  Student ID: %s\n", studentId);
        System.out.printf("  Total Dues: $%.2f\n", dues);

        if (dues > 0) {
            System.out.println("  Status: ⚠ Payment Required");
            System.out.println("\n  Note: Please clear your dues to maintain good standing.");
        } else {
            System.out.println("  Status: ✓ No Outstanding Dues");
        }
        System.out.println("═══════════════════════════════════════════════════════════════════════");
    }

    private void payDues(String studentId) {
        double dues = dueController.getDue(studentId);

        if (dues <= 0) {
            System.out.println("\n✓ You have no outstanding dues!");
            return;
        }

        System.out.println("\n--- Pay Dues ---");
        System.out.printf("Total Outstanding Dues: $%.2f\n", dues);

        double balance = balanceController.getBalance(studentId);
        System.out.printf("Your Current Balance:   $%.2f\n", balance);

        if (balance < dues) {
            System.out.printf("\n✗ Insufficient balance! You need $%.2f more.\n", dues - balance);
            System.out.println("  Please add balance first.");
            return;
        }

        System.out.print("\nConfirm payment of $" + String.format("%.2f", dues) + "? (y/n): ");
        String confirm = FastInput.readLine();

        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("✗ Payment cancelled.");
            return;
        }

        // Deduct from balance
        if (balanceController.deductBalance(studentId, dues)) {
            // Clear dues
            dueController.payDue(studentId);

            double newBalance = balanceController.getBalance(studentId);

            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════════════");
            System.out.println("|                      PAYMENT SUCCESSFUL                             |");
            System.out.println("═══════════════════════════════════════════════════════════════════════");
            System.out.printf("  Amount Paid:      $%.2f\n", dues);
            System.out.printf("  Previous Balance: $%.2f\n", balance);
            System.out.printf("  New Balance:      $%.2f\n", newBalance);
            System.out.println("  Status:           ✓ All Dues Cleared");
            System.out.println("═══════════════════════════════════════════════════════════════════════");
        } else {
            System.out.println("✗ Payment failed! Please try again.");
        }
    }

    private void viewPurchaseHistory(String studentId) {
        purchaseHistoryController.showPurchaseHistory(studentId);
    }

    private void viewRecentPurchases(String studentId) {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("|                          RECENT PURCHASES                           |");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println();
        System.out.print("Enter number of days (default 7): ");
        String input = FastInput.readLine();
        int days = input.isEmpty() ? 7 : Integer.parseInt(input);

        purchaseHistoryController.showRecentPurchases(studentId, days);
    }
}
