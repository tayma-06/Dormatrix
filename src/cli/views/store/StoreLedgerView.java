package cli.views.store;

import controllers.balance.BalanceController;
import controllers.store.DueController;
import controllers.store.PurchaseHistoryController;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

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
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);

            double balance = balanceController.getBalance(studentId);
            double dues = dueController.getDue(studentId);

            TerminalUI.tBoxTop();
            TerminalUI.tBoxTitle("STORE ACCOUNT & LEDGER");
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("Student ID: " + studentId);
            TerminalUI.tBoxLine(String.format("Current Balance: $%.2f", balance));
            TerminalUI.tBoxLine(String.format("Outstanding Dues: $%.2f %s", dues, dues > 0 ? "(!)" : "(OK)"));
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("[1] View Balance Details");
            TerminalUI.tBoxLine("[2] Add Balance");
            TerminalUI.tBoxLine("[3] View Outstanding Dues");
            TerminalUI.tBoxLine("[4] Pay Dues");
            TerminalUI.tBoxLine("[5] View Complete Purchase History");
            TerminalUI.tBoxLine("[6] View Recent Purchases");
            TerminalUI.tBoxLine("[0] Back", utils.ConsoleColors.Accent.EXIT);
            TerminalUI.tBoxBottom();
            TerminalUI.tEmpty();
            TerminalUI.tPrompt("Enter your choice: ");

            String choice = FastInput.readLine();

            switch (choice) {
                case "1": viewBalance(studentId); break;
                case "2": addBalance(studentId); break;
                case "3": viewDues(studentId); break;
                case "4": payDues(studentId); break;
                case "5": viewPurchaseHistory(studentId); break;
                case "6": viewRecentPurchases(studentId); break;
                case "0": ConsoleUtil.clearScreen(); return;
                default: TerminalUI.tError("Invalid choice!");
            }

            if (!choice.equals("0")) {
                TerminalUI.tPause();
            }
        }
    }

    private void viewBalance(String studentId) {
        double balance = balanceController.getBalance(studentId);
        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("BALANCE DETAILS");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Student ID: " + studentId);
        TerminalUI.tBoxLine(String.format("Current Balance: $%.2f", balance));
        TerminalUI.tBoxLine(balance < 100 ? "Status: Low Balance - Consider adding funds" : "Status: Sufficient Balance");
        TerminalUI.tBoxBottom();
    }

    private void addBalance(String studentId) {
        double currentBalance = balanceController.getBalance(studentId);
        TerminalUI.tEmpty();
        TerminalUI.tBoxLine(String.format("Current Balance: $%.2f", currentBalance));
        TerminalUI.tPrompt("Enter amount to add: $");
        double amount = FastInput.readDouble();

        if (amount <= 0) {
            TerminalUI.tError("Invalid amount!");
            return;
        }

        balanceController.addBalance(studentId, amount);
        double newBalance = balanceController.getBalance(studentId);
        TerminalUI.tSuccess("Balance added successfully!");
        TerminalUI.tBoxTop();
        TerminalUI.tBoxLine(String.format("Previous: $%.2f | Added: $%.2f | New: $%.2f", currentBalance, amount, newBalance));
        TerminalUI.tBoxBottom();
    }

    private void viewDues(String studentId) {
        double dues = dueController.getDue(studentId);
        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("OUTSTANDING DUES");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Student ID: " + studentId);
        TerminalUI.tBoxLine(String.format("Total Dues: $%.2f", dues));
        TerminalUI.tBoxLine(dues > 0 ? "Status: Payment Required" : "Status: No Outstanding Dues");
        TerminalUI.tBoxBottom();
    }

    private void payDues(String studentId) {
        double dues = dueController.getDue(studentId);
        if (dues <= 0) {
            TerminalUI.tSuccess("You have no outstanding dues!");
            return;
        }
        double balance = balanceController.getBalance(studentId);
        TerminalUI.tBoxTop();
        TerminalUI.tBoxLine(String.format("Outstanding Dues: $%.2f", dues));
        TerminalUI.tBoxLine(String.format("Your Balance:     $%.2f", balance));
        TerminalUI.tBoxBottom();

        if (balance < dues) {
            TerminalUI.tError(String.format("Insufficient balance! Need $%.2f more.", dues - balance));
            return;
        }

        TerminalUI.tPrompt("Confirm payment of $" + String.format("%.2f", dues) + "? (y/n): ");
        String confirm = FastInput.readLine();
        if (!confirm.equalsIgnoreCase("y")) {
            TerminalUI.tPrint("Payment cancelled.");
            return;
        }

        if (balanceController.deductBalance(studentId, dues)) {
            dueController.payDue(studentId);
            double newBalance = balanceController.getBalance(studentId);
            TerminalUI.tSuccess("PAYMENT SUCCESSFUL");
            TerminalUI.tBoxTop();
            TerminalUI.tBoxLine(String.format("Amount Paid: $%.2f", dues));
            TerminalUI.tBoxLine(String.format("New Balance: $%.2f", newBalance));
            TerminalUI.tBoxLine("Status: All Dues Cleared");
            TerminalUI.tBoxBottom();
        } else {
            TerminalUI.tError("Payment failed! Please try again.");
        }
    }

    private void viewPurchaseHistory(String studentId) {
        purchaseHistoryController.showPurchaseHistory(studentId);
    }

    private void viewRecentPurchases(String studentId) {
        TerminalUI.tEmpty();
        TerminalUI.tPrompt("Enter number of days (default 7): ");
        String input = FastInput.readLine();
        int days = input.isEmpty() ? 7 : Integer.parseInt(input);
        purchaseHistoryController.showRecentPurchases(studentId, days);
    }
}
