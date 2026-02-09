package controllers.store;

import models.store.SaleRecord;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SalesSummaryController {
    private final String SALES_FILE = "data/store/sales.txt"; // FIXED

    public void showDailySummary() {
        LocalDate today = LocalDate.now();
        showSummaryForDate(today);
    }

    public void showWeeklySummary() {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        showSummaryForDateRange(weekAgo, today);
    }

    public void showMonthlySummary() {
        LocalDate today = LocalDate.now();
        LocalDate monthAgo = today.minusMonths(1);
        showSummaryForDateRange(monthAgo, today);
    }

    private void showSummaryForDate(LocalDate date) {
        System.out.println("====================================================================");
        System.out.println("|                    DAILY SALES SUMMARY                           |");
        System.out.println("====================================================================");
        System.out.println("  Date: " + date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        System.out.println("--------------------------------------------------------------------");

        int totalTransactions = 0;
        double totalRevenue = 0.0;

        try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            System.out.println("  Student ID | Item ID | Qty | Amount  | Date");
            System.out.println("--------------------------------------------------------------------");

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    LocalDate saleDate = LocalDate.parse(parts[4]);
                    if (saleDate.equals(date)) {
                        totalTransactions++;
                        double amount = Double.parseDouble(parts[3]);
                        totalRevenue += amount;
                        System.out.printf("  %-10s | %-7s | %3s | $%6.2f | %s\n",
                                parts[0], parts[1], parts[2], amount, parts[4]);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("  No sales data found.");
            return;
        }

        System.out.println("====================================================================");
        System.out.printf("  Total Transactions: %d\n", totalTransactions);
        System.out.printf("  Total Revenue:      $%.2f\n", totalRevenue);
        System.out.println("====================================================================");
    }

    private void showSummaryForDateRange(LocalDate startDate, LocalDate endDate) {
        System.out.println("====================================================================");
        System.out.println("|                    SALES SUMMARY REPORT                          |");
        System.out.println("====================================================================");
        System.out.println("  Period: " + startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) +
                " to " + endDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        System.out.println("--------------------------------------------------------------------");

        int totalTransactions = 0;
        double totalRevenue = 0.0;

        try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            System.out.println("  Student ID | Item ID | Qty | Amount  | Date");
            System.out.println("--------------------------------------------------------------------");

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    LocalDate saleDate = LocalDate.parse(parts[4]);
                    if (!saleDate.isBefore(startDate) && !saleDate.isAfter(endDate)) {
                        totalTransactions++;
                        double amount = Double.parseDouble(parts[3]);
                        totalRevenue += amount;
                        System.out.printf("  %-10s | %-7s | %3s | $%6.2f | %s\n",
                                parts[0], parts[1], parts[2], amount, parts[4]);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("  No sales data found.");
            return;
        }

        System.out.println("====================================================================");
        System.out.printf("  Total Transactions: %d\n", totalTransactions);
        System.out.printf("  Total Revenue:      $%.2f\n", totalRevenue);
        if (totalTransactions > 0) {
            System.out.printf("  Average Sale:       $%.2f\n", totalRevenue / totalTransactions);
        }
        System.out.println("====================================================================");
    }

    public void showCustomSummary(LocalDate startDate, LocalDate endDate) {
        showSummaryForDateRange(startDate, endDate);
    }
}