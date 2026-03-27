package cli.views.store;

import controllers.store.SalesSummaryController;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static utils.TerminalUIExtras.tArrowSelect;

public class SalesSummaryView {

    private final SalesSummaryController salesController;

    public SalesSummaryView() {
        this.salesController = new SalesSummaryController();
    }

    public void show() {
        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);

            TerminalUI.tBoxTop();
            TerminalUI.tBoxTitle("SALES REPORTS");
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("All report amounts are shown in BDT.");
            TerminalUI.tBoxBottom();

            int choice;
            try {
                choice = tArrowSelect("SALES REPORT OPTIONS", new String[]{
                        "Daily Summary (Today)",
                        "Weekly Summary (Last 7 Days)",
                        "Monthly Summary (Last 30 Days)",
                        "Custom Date Range",
                        "Back"
                }, false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            switch (choice) {
                case 0 -> {
                    ConsoleUtil.clearScreen();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);
                    salesController.showDailySummary();
                    TerminalUI.tPause();
                }
                case 1 -> {
                    ConsoleUtil.clearScreen();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);
                    salesController.showWeeklySummary();
                    TerminalUI.tPause();
                }
                case 2 -> {
                    ConsoleUtil.clearScreen();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);
                    salesController.showMonthlySummary();
                    TerminalUI.tPause();
                }
                case 3 -> showCustomDateRange();
                default -> {
                    return;
                }
            }
        }
    }

    private void showCustomDateRange() {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("CUSTOM DATE RANGE REPORT");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Date format: dd-MM-yyyy (e.g., 25-01-2025)");
        TerminalUI.tBoxBottom();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        try {
            TerminalUI.tPrompt("Enter start date: ");
            String startInput = FastInput.readLine();
            LocalDate startDate = LocalDate.parse(startInput, formatter);

            TerminalUI.tPrompt("Enter end date: ");
            String endInput = FastInput.readLine();
            LocalDate endDate = LocalDate.parse(endInput, formatter);

            if (startDate.isAfter(endDate)) {
                TerminalUI.tError("Start date cannot be after end date!");
                TerminalUI.tPause();
                return;
            }

            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);
            salesController.showCustomSummary(startDate, endDate);
            TerminalUI.tPause();

        } catch (DateTimeParseException e) {
            TerminalUI.tError("Invalid date format! Please use dd-MM-yyyy");
            TerminalUI.tPause();
        } catch (Exception e) {
            TerminalUI.tError("Error processing date range: " + e.getMessage());
            TerminalUI.tPause();
        }
    }
}