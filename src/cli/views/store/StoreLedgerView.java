package cli.views.store;

import controllers.balance.BalanceController;
import controllers.store.DueController;
import utils.FastInput;

public class StoreLedgerView {

    private final BalanceController balanceController;
    private final DueController dueController;

    public StoreLedgerView() {
        this.balanceController = new BalanceController();
        this.dueController = new DueController();
    }

    public void show(String studentId) {
        while (true) {
            System.out.println("\n====================================================================");
            System.out.println("|                   STORE PURCHASES & DUES                         |");
            System.out.println("====================================================================");

            double balance = balanceController.getBalance(studentId);
            double dues = dueController.getDue(studentId);

            System.out.printf("  Current Balance: $%.2f\n", balance);
            System.out.printf("  Outstanding Dues: $%.2f\n", dues);
            System.out.println("--------------------------------------------------------------------");

            System.out.println("1. Add Money to Balance");
            System.out.println("2. Pay Outstanding Dues");
            System.out.println("3. View Account Summary");
            System.out.println("0. Back");
            System.out.print("\nEnter your choice: ");

            int choice = FastInput.readInt();

            switch (choice) {
                case 1 -> addBalance(studentId);
                case 2 -> payDues(studentId);
                case 3 -> showAccountSummary(studentId);
                case 0 -> { return; }
                default -> System.out.println("Invalid choice!");
            }

            System.out.println("\nPress Enter to continue...");
            FastInput.readLine();
        }
    }

    private void addBalance(String studentId) {
        System.out.println("\nEnter amount to add: $");
        double amount = FastInput.readDouble();
        if (amount <= 0) {
            System.out.println("✗ Invalid amount!");
            return;
        }
        balanceController.addBalance(studentId, amount);
        System.out.println("✓ Money added successfully!");
    }

    private void payDues(String studentId) {
        double dues = dueController.getDue(studentId);
        if (dues <= 0) {
            System.out.println("You have no outstanding dues!");
            return;
        }

        double balance = balanceController.getBalance(studentId);
        System.out.printf("Outstanding Dues: $%.2f\n", dues);
        System.out.printf("Current Balance: $%.2f\n", balance);

        if (balance < dues) {
            System.out.printf("Insufficient balance. You need $%.2f more.\n", dues - balance);
            return;
        }

        System.out.print("Confirm payment of $" + String.format("%.2f", dues) + "? (y/n): ");
        String confirm = FastInput.readLine();
        if (confirm.equalsIgnoreCase("y")) {
            balanceController.deductBalance(studentId, dues);
            dueController.payDue(studentId);
            System.out.println("✓ Payment successful!");
        } else {
            System.out.println("Payment cancelled.");
        }
    }

    private void showAccountSummary(String studentId) {
        double balance = balanceController.getBalance(studentId);
        double dues = dueController.getDue(studentId);
        double net = balance - dues;

        System.out.println("\n====================================================================");
        System.out.println("|                     ACCOUNT SUMMARY                              |");
        System.out.println("====================================================================");
        System.out.printf("Student ID: %s\n", studentId);
        System.out.printf("Account Balance: $%.2f\n", balance);
        System.out.printf("Outstanding Dues: $%.2f\n", dues);
        System.out.printf("Net Balance: $%.2f\n", net);
        if (net < 0) System.out.println("⚠ You owe money! Please settle your dues.");
        else if (balance < 50) System.out.println("⚠ Low balance! Consider adding more money.");
        else System.out.println("✓ Your account is in good standing!");
        System.out.println("====================================================================");
    }
}
