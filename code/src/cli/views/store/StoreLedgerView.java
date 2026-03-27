package cli.views.store;

import controllers.balance.BalanceController;
import controllers.store.DueController;
import controllers.store.PurchaseHistoryController;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import static utils.TerminalUIExtras.tArrowSelect;

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
            TerminalUI.tBoxLine(String.format("Current Balance: BDT %.2f", balance));
            TerminalUI.tBoxLine(String.format("Outstanding Dues: BDT %.2f %s", dues, dues > 0 ? "(!)" : "(OK)"));
            TerminalUI.tBoxBottom();

            int choice;
            try {
                choice = tArrowSelect("LEDGER ACTIONS", new String[]{
                        "View Balance Details",
                        "Add Balance",
                        "View Outstanding Dues",
                        "Pay Dues",
                        "View Complete Purchase History",
                        "View Recent Purchases",
                        "Back"
                }, false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            switch (choice) {
                case 0 -> viewBalance(studentId);
                case 1 -> addBalance(studentId);
                case 2 -> viewDues(studentId);
                case 3 -> payDues(studentId);
                case 4 -> viewPurchaseHistory(studentId);
                case 5 -> viewRecentPurchases(studentId);
                default -> {
                    ConsoleUtil.clearScreen();
                    return;
                }
            }
        }
    }

    private void viewBalance(String studentId) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);

        double balance = balanceController.getBalance(studentId);
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("BALANCE DETAILS");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Student ID: " + studentId);
        TerminalUI.tBoxLine(String.format("Current Balance: BDT %.2f", balance));
        TerminalUI.tBoxLine(balance < 100 ? "Status: Low Balance - Consider adding funds" : "Status: Sufficient Balance");
        TerminalUI.tBoxBottom();
        TerminalUI.tPause();
    }

    private void addBalance(String studentId) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);

        double currentBalance = balanceController.getBalance(studentId);
        TerminalUI.tBoxLine(String.format("Current Balance: BDT %.2f", currentBalance));
        TerminalUI.tPrompt("Enter amount to add (BDT): ");
        double amount = FastInput.readDouble();

        if (amount <= 0) {
            TerminalUI.tError("Invalid amount!");
            TerminalUI.tPause();
            return;
        }

        balanceController.addBalance(studentId, amount);
        double newBalance = balanceController.getBalance(studentId);
        TerminalUI.tSuccess("Balance added successfully!");
        TerminalUI.tBoxTop();
        TerminalUI.tBoxLine(String.format("Previous: BDT %.2f | Added: BDT %.2f | New: BDT %.2f",
                currentBalance, amount, newBalance));
        TerminalUI.tBoxBottom();
        TerminalUI.tPause();
    }

    private void viewDues(String studentId) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);

        double dues = dueController.getDue(studentId);
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("OUTSTANDING DUES");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Student ID: " + studentId);
        TerminalUI.tBoxLine(String.format("Total Dues: BDT %.2f", dues));
        TerminalUI.tBoxLine(dues > 0 ? "Status: Payment Required" : "Status: No Outstanding Dues");
        TerminalUI.tBoxBottom();
        TerminalUI.tPause();
    }

    private void payDues(String studentId) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);

        double dues = dueController.getDue(studentId);
        if (dues <= 0) {
            TerminalUI.tSuccess("You have no outstanding dues!");
            TerminalUI.tPause();
            return;
        }

        double balance = balanceController.getBalance(studentId);
        TerminalUI.tBoxTop();
        TerminalUI.tBoxLine(String.format("Outstanding Dues: BDT %.2f", dues));
        TerminalUI.tBoxLine(String.format("Your Balance:     BDT %.2f", balance));
        TerminalUI.tBoxBottom();

        if (balance < dues) {
            TerminalUI.tError(String.format("Insufficient balance! Need BDT %.2f more.", dues - balance));
            TerminalUI.tPause();
            return;
        }

        TerminalUI.tPrompt("Confirm payment of BDT " + String.format("%.2f", dues) + "? (y/n): ");
        String confirm = FastInput.readLine();
        if (!confirm.equalsIgnoreCase("y")) {
            TerminalUI.tPrint("Payment cancelled.");
            TerminalUI.tPause();
            return;
        }

        if (balanceController.deductBalance(studentId, dues)) {
            dueController.payDue(studentId);
            double newBalance = balanceController.getBalance(studentId);
            TerminalUI.tSuccess("PAYMENT SUCCESSFUL");
            TerminalUI.tBoxTop();
            TerminalUI.tBoxLine(String.format("Amount Paid: BDT %.2f", dues));
            TerminalUI.tBoxLine(String.format("New Balance: BDT %.2f", newBalance));
            TerminalUI.tBoxLine("Status: All Dues Cleared");
            TerminalUI.tBoxBottom();
        } else {
            TerminalUI.tError("Payment failed! Please try again.");
        }

        TerminalUI.tPause();
    }

    private void viewPurchaseHistory(String studentId) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        purchaseHistoryController.showPurchaseHistory(studentId);
        TerminalUI.tPause();
    }

    private void viewRecentPurchases(String studentId) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);

        TerminalUI.tPrompt("Enter number of days (default 7): ");
        String input = FastInput.readLine();
        int days = input.isEmpty() ? 7 : Integer.parseInt(input);

        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        purchaseHistoryController.showRecentPurchases(studentId, days);
        TerminalUI.tPause();
    }
}