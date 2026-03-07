package controllers.store;

import java.io.*;
import java.time.LocalDate;
import utils.TerminalUI;

public class PurchaseHistoryController {
    private final String SALES_FILE = "data/store/sales.txt"; // FIXED

    public void showPurchaseHistory(String studentId) {
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("PURCHASE HISTORY");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Student ID: " + studentId);
        TerminalUI.tBoxSep();

        int totalPurchases = 0;
        double totalSpent = 0.0;

        try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            TerminalUI.tBoxLine("Item ID | Quantity | Amount | Date");
            TerminalUI.tBoxSep();

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[0].equals(studentId)) {
                    totalPurchases++;
                    double amount = Double.parseDouble(parts[3]);
                    totalSpent += amount;
                    TerminalUI.tBoxLine(String.format("%-7s | %8s | $%6.2f | %s",
                            parts[1], parts[2], amount, parts[4]));
                }
            }

            if (totalPurchases == 0) {
                TerminalUI.tBoxLine("No purchase history found.");
            }

        } catch (IOException e) {
            TerminalUI.tError("Error loading purchase history.");
            return;
        }

        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Total Purchases: " + totalPurchases);
        TerminalUI.tBoxLine(String.format("Total Spent:     $%.2f", totalSpent));
        TerminalUI.tBoxBottom();
    }

    public void showRecentPurchases(String studentId, int days) {
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("RECENT PURCHASE HISTORY");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Student ID: " + studentId + " (Last " + days + " days)");
        TerminalUI.tBoxSep();

        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        int totalPurchases = 0;
        double totalSpent = 0.0;

        try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            TerminalUI.tBoxLine("Item ID | Quantity | Amount | Date");
            TerminalUI.tBoxSep();

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[0].equals(studentId)) {
                    LocalDate purchaseDate = LocalDate.parse(parts[4]);
                    if (!purchaseDate.isBefore(cutoffDate)) {
                        totalPurchases++;
                        double amount = Double.parseDouble(parts[3]);
                        totalSpent += amount;
                        TerminalUI.tBoxLine(String.format("%-7s | %8s | $%6.2f | %s",
                                parts[1], parts[2], amount, parts[4]));
                    }
                }
            }

            if (totalPurchases == 0) {
                TerminalUI.tBoxLine("No recent purchases found.");
            }

        } catch (IOException e) {
            TerminalUI.tError("Error loading purchase history.");
            return;
        }

        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Total Recent Purchases: " + totalPurchases);
        TerminalUI.tBoxLine(String.format("Total Spent:            $%.2f", totalSpent));
        TerminalUI.tBoxBottom();
    }
}