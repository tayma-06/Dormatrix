package controllers.store;

import java.io.*;
import java.time.LocalDate;

public class PurchaseHistoryController {
    private final String SALES_FILE = "data/inventories/sales.txt";

    public void showPurchaseHistory(String studentId) {
        System.out.println("====================================================================");
        System.out.println("|                    PURCHASE HISTORY                              |");
        System.out.println("====================================================================");
        System.out.printf("  Student ID: %s\n", studentId);
        System.out.println("--------------------------------------------------------------------");

        int totalPurchases = 0;
        double totalSpent = 0.0;

        try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            System.out.println("  Item ID | Quantity | Amount  | Date");
            System.out.println("--------------------------------------------------------------------");

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[0].equals(studentId)) {
                    totalPurchases++;
                    double amount = Double.parseDouble(parts[3]);
                    totalSpent += amount;
                    System.out.printf("  %-7s | %8s | $%6.2f | %s\n",
                            parts[1], parts[2], amount, parts[4]);
                }
            }

            if (totalPurchases == 0) {
                System.out.println("  No purchase history found.");
            }

        } catch (IOException e) {
            System.out.println("  Error loading purchase history.");
        }

        System.out.println("====================================================================");
        System.out.printf("  Total Purchases: %d\n", totalPurchases);
        System.out.printf("  Total Spent:     $%.2f\n", totalSpent);
        System.out.println("====================================================================");
    }

    public void showRecentPurchases(String studentId, int days) {
        System.out.println("====================================================================");
        System.out.println("|                 RECENT PURCHASE HISTORY                          |");
        System.out.println("====================================================================");
        System.out.printf("  Student ID: %s (Last %d days)\n", studentId, days);
        System.out.println("--------------------------------------------------------------------");

        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        int totalPurchases = 0;
        double totalSpent = 0.0;

        try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            System.out.println("  Item ID | Quantity | Amount  | Date");
            System.out.println("--------------------------------------------------------------------");

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[0].equals(studentId)) {
                    LocalDate purchaseDate = LocalDate.parse(parts[4]);
                    if (!purchaseDate.isBefore(cutoffDate)) {
                        totalPurchases++;
                        double amount = Double.parseDouble(parts[3]);
                        totalSpent += amount;
                        System.out.printf("  %-7s | %8s | $%6.2f | %s\n",
                                parts[1], parts[2], amount, parts[4]);
                    }
                }
            }

            if (totalPurchases == 0) {
                System.out.println("  No recent purchases found.");
            }

        } catch (IOException e) {
            System.out.println("  Error loading purchase history.");
        }

        System.out.println("====================================================================");
        System.out.printf("  Total Recent Purchases: %d\n", totalPurchases);
        System.out.printf("  Total Spent:            $%.2f\n", totalSpent);
        System.out.println("====================================================================");
    }
}