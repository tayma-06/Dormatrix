package cli.views.store;

import controllers.store.SalesSummaryController;
import utils.FastInput;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SalesSummaryView {
    private final String SALES_FILE = "data/store/sales.txt";

    private final SalesSummaryController salesController;

    public SalesSummaryView() {
        this.salesController = new SalesSummaryController();
    }

    public void show() {
        while (true) {
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
                    System.out.println("✗ Invalid choice!");
            }

            if (!choice.equals("0")) {
                System.out.println("\nPress Enter to continue...");
                FastInput.readLine();
            }
        }
    }

    private void displayHeader() {
        System.out.println("\n====================================================================");
        System.out.println("|                      SALES REPORTS                               |");
        System.out.println("====================================================================");
    }

    private void displayMenu() {
        System.out.println("\nReport Options:");
        System.out.println("  [1] Daily Summary (Today)");
        System.out.println("  [2] Weekly Summary (Last 7 Days)");
        System.out.println("  [3] Monthly Summary (Last 30 Days)");
        System.out.println("  [4] Custom Date Range");
        System.out.println("  [0] Back");
        System.out.print("\nEnter your choice: ");
    }

    private void showCustomDateRange() {
        System.out.println("\n--- Custom Date Range Report ---");
        System.out.println("Date format: dd-MM-yyyy (e.g., 25-01-2025)");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate startDate = null;
        LocalDate endDate = null;

        try {
            System.out.print("Enter start date: ");
            String startInput = FastInput.readLine();
            startDate = LocalDate.parse(startInput, formatter);

            System.out.print("Enter end date: ");
            String endInput = FastInput.readLine();
            endDate = LocalDate.parse(endInput, formatter);

            if (startDate.isAfter(endDate)) {
                System.out.println("✗ Start date cannot be after end date!");
                return;
            }

            salesController.showCustomSummary(startDate, endDate);

        } catch (DateTimeParseException e) {
            System.out.println("✗ Invalid date format! Please use dd-MM-yyyy");
        } catch (Exception e) {
            System.out.println("✗ Error processing date range: " + e.getMessage());
        }
    }
}