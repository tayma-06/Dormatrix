package controllers.store;

import utils.TerminalUI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SalesSummaryController {
    private final String SALES_FILE = "data/store/sales.txt";

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
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("DAILY SALES SUMMARY");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Date: " + date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        TerminalUI.tBoxSep();

        int totalTransactions = 0;
        double totalRevenue = 0.0;

        try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            TerminalUI.tBoxLine("Student ID | Item ID | Qty | Amount | Date");
            TerminalUI.tBoxSep();

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    LocalDate saleDate = LocalDate.parse(parts[4]);
                    if (saleDate.equals(date)) {
                        totalTransactions++;
                        double amount = Double.parseDouble(parts[3]);
                        totalRevenue += amount;
                        TerminalUI.tBoxLine(String.format("%-10s | %-7s | %3s | BDT %6.2f | %s",
                                parts[0], parts[1], parts[2], amount, parts[4]));
                    }
                }
            }
        } catch (IOException e) {
            TerminalUI.tError("No sales data found.");
            return;
        }

        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Total Transactions: " + totalTransactions);
        TerminalUI.tBoxLine(String.format("Total Revenue:      BDT %.2f", totalRevenue));
        TerminalUI.tBoxBottom();
    }

    private void showSummaryForDateRange(LocalDate startDate, LocalDate endDate) {
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("SALES SUMMARY REPORT");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Period: " + startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                + " to " + endDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        TerminalUI.tBoxSep();

        int totalTransactions = 0;
        double totalRevenue = 0.0;

        try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            TerminalUI.tBoxLine("Student ID | Item ID | Qty | Amount | Date");
            TerminalUI.tBoxSep();

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    LocalDate saleDate = LocalDate.parse(parts[4]);
                    if (!saleDate.isBefore(startDate) && !saleDate.isAfter(endDate)) {
                        totalTransactions++;
                        double amount = Double.parseDouble(parts[3]);
                        totalRevenue += amount;
                        TerminalUI.tBoxLine(String.format("%-10s | %-7s | %3s | BDT %6.2f | %s",
                                parts[0], parts[1], parts[2], amount, parts[4]));
                    }
                }
            }
        } catch (IOException e) {
            TerminalUI.tError("No sales data found.");
            return;
        }

        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Total Transactions: " + totalTransactions);
        TerminalUI.tBoxLine(String.format("Total Revenue:      BDT %.2f", totalRevenue));
        if (totalTransactions > 0) {
            TerminalUI.tBoxLine(String.format("Average Sale:       BDT %.2f", totalRevenue / totalTransactions));
        }
        TerminalUI.tBoxBottom();
    }

    public void showCustomSummary(LocalDate startDate, LocalDate endDate) {
        showSummaryForDateRange(startDate, endDate);
    }
}