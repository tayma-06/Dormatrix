package cli.views.store;

import controllers.store.BalanceController;
import controllers.store.DueController;
import controllers.store.PurchaseHistoryController;
import utils.FastInput;

public class StoreLedgerView {

    private final BalanceController balanceController;
    private final DueController dueController;
    private final PurchaseHistoryController historyController;

    public StoreLedgerView() {
        this.balanceController = new BalanceController();
        this.dueController = new DueController();
        this.historyController = new PurchaseHistoryController();
    }

    public StoreLedgerView(BalanceController balanceController, DueController dueController, PurchaseHistoryController historyController) {
        this.balanceController = balanceController;
        this.dueController = dueController;
        this.historyController = historyController;
    }

    public void show(String username) {
        while (true) {
            System.out.println("\n====================================================================");
            System.out.println("|                   STORE PURCHASES & DUES                         |");
            System.out.println("====================================================================");

            // Display current balance and dues
            double balance = balanceController.getBalance(username);
            double dues = dueController.getDue(username);

            System.out.printf("  Current Balance: $%.2f\n", balance);
            System.out.printf("  Outstanding Dues: $%.2f\n", dues);
            System.out.println("--------------------------------------------------------------------");

            System.out.println("1. Add Money to Balance");
            System.out.println("2. View Purchase History");
            System.out.println("3. View Recent Purchases (Last 30 days)");
            System.out.println("4. Pay Outstanding Dues");
            System.out.println("5. View Account Summary");
            System.out.println("0. Back");
            System.out.print("\nEnter your choice: ");

            int choice = FastInput.readInt();

            switch (choice) {
                case 1:
                    addBalance(username);
                    break;
                case 2:
                    historyController.showPurchaseHistory(username);
                    break;
                case 3:
                    historyController.showRecentPurchases(username, 30);
                    break;
                case 4:
                    payDues(username);
                    break;
                case 5:
                    showAccountSummary(username);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }

            System.out.println("\nPress Enter to continue...");
            FastInput.readLine();
        }
    }

    private void addBalance(String username) {
        System.out.println("\n--------------------------------------------------------------------");
        System.out.println("|                     ADD MONEY TO BALANCE                         |");
        System.out.println("--------------------------------------------------------------------");

        double currentBalance = balanceController.getBalance(username);
        System.out.printf("  Current Balance: $%.2f\n", currentBalance);

        System.out.print("\nEnter amount to add: $");
        double amount = FastInput.readDouble();

        if (amount <= 0) {
            System.out.println("✗ Invalid amount!");
            return;
        }

        balanceController.addBalance(username, amount);

        System.out.println("\n====================================================================");
        System.out.println("  ✓ MONEY ADDED SUCCESSFULLY!");
        System.out.println("====================================================================");
        System.out.printf("  Amount Added: $%.2f\n", amount);
        System.out.printf("  New Balance:  $%.2f\n", currentBalance + amount);
        System.out.println("====================================================================");
    }

    private void payDues(String username) {
        System.out.println("\n--------------------------------------------------------------------");
        System.out.println("|                     PAY OUTSTANDING DUES                         |");
        System.out.println("--------------------------------------------------------------------");

        double dues = dueController.getDue(username);

        if (dues <= 0) {
            System.out.println("  You have no outstanding dues!");
            return;
        }

        double balance = balanceController.getBalance(username);

        System.out.printf("  Outstanding Dues: $%.2f\n", dues);
        System.out.printf("  Current Balance:  $%.2f\n", balance);
        System.out.println();

        if (balance < dues) {
            System.out.println("  ✗ Insufficient balance to pay dues!");
            System.out.printf("  You need $%.2f more.\n", dues - balance);
            return;
        }

        System.out.print("Confirm payment of $" + String.format("%.2f", dues) + "? (y/n): ");
        String confirm = FastInput.readLine();

        if (confirm.equalsIgnoreCase("y")) {
            balanceController.deductBalance(username, dues);
            dueController.payDue(username);

            System.out.println("\n====================================================================");
            System.out.println("  ✓ PAYMENT SUCCESSFUL!");
            System.out.println("====================================================================");
            System.out.printf("  Amount Paid:      $%.2f\n", dues);
            System.out.printf("  Remaining Balance: $%.2f\n", balance - dues);
            System.out.println("  Outstanding Dues: $0.00");
            System.out.println("====================================================================");
        } else {
            System.out.println("Payment cancelled.");
        }
    }

    private void showAccountSummary(String username) {
        System.out.println("\n====================================================================");
        System.out.println("|                     ACCOUNT SUMMARY                              |");
        System.out.println("====================================================================");
        System.out.printf("  Student ID: %s\n", username);
        System.out.println("--------------------------------------------------------------------");

        double balance = balanceController.getBalance(username);
        double dues = dueController.getDue(username);
        double netBalance = balance - dues;

        System.out.printf("  Account Balance:     $%.2f\n", balance);
        System.out.printf("  Outstanding Dues:    $%.2f\n", dues);
        System.out.println("  ---------------------------------------------------");
        System.out.printf("  Net Balance:         $%.2f\n", netBalance);

        if (netBalance < 0) {
            System.out.println("\n  ⚠ You owe money! Please settle your dues.");
        } else if (balance < 50) {
            System.out.println("\n  ⚠ Low balance! Consider adding more money.");
        } else {
            System.out.println("\n  ✓ Your account is in good standing!");
        }

        System.out.println("====================================================================");
    }
}