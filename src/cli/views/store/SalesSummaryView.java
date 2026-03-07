package cli.views.store;

import controllers.store.SalesSummaryController;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

public class SalesSummaryView {

    private final String SALES_FILE = "data/store/sales.txt";

    private final SalesSummaryController salesController;

    public SalesSummaryView() {
        this.salesController = new SalesSummaryController();
    }

    public void show() {
        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);
            displayHeader();
            displayMenu();

            String choice = FastInput.readLine();

            switch (choice) {
                case "1":
                    salesController.showDailySummary();
                    break;
                case "2":
                    salesController.showWeeklySummary();
                    break;
                case "3":
                    salesController.showMonthlySummary();
                    break;
                case "4":
                    showCustomDateRange();
                    break;
                case "0":
                    return;
                default:
                    TerminalUI.tError("Invalid choice!");
            }

            if (!choice.equals("0")) {
                TerminalUI.tPause();
            }
        }
    }

    private void displayHeader() {
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("SALES REPORTS");
        TerminalUI.tBoxSep();
    }

    private void displayMenu() {
        TerminalUI.tBoxLine("[1] Daily Summary (Today)");
        TerminalUI.tBoxLine("[2] Weekly Summary (Last 7 Days)");
        TerminalUI.tBoxLine("[3] Monthly Summary (Last 30 Days)");
        TerminalUI.tBoxLine("[4] Custom Date Range");
        TerminalUI.tBoxLine("[0] Back", utils.ConsoleColors.Accent.EXIT);
        TerminalUI.tBoxSep();
        TerminalUI.tInputRow();
    }

    private void showCustomDateRange() {
        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("CUSTOM DATE RANGE REPORT");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Date format: dd-MM-yyyy (e.g., 25-01-2025)");
        TerminalUI.tBoxBottom();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate startDate = null;
        LocalDate endDate = null;

        try {
            TerminalUI.tPrompt("Enter start date: ");
            String startInput = FastInput.readLine();
            startDate = LocalDate.parse(startInput, formatter);

            TerminalUI.tPrompt("Enter end date: ");
            String endInput = FastInput.readLine();
            endDate = LocalDate.parse(endInput, formatter);

            if (startDate.isAfter(endDate)) {
                TerminalUI.tError("Start date cannot be after end date!");
                return;
            }

            salesController.showCustomSummary(startDate, endDate);

        } catch (DateTimeParseException e) {
            TerminalUI.tError("Invalid date format! Please use dd-MM-yyyy");

        } catch (Exception e) {
            TerminalUI.tError("Error processing date range: " + e.getMessage());
        }
    }
}
