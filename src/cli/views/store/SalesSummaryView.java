package cli.views.store;

import controllers.store.SalesSummaryController;
import utils.FastInput;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SalesSummaryView {

    private final SalesSummaryController summaryController;

    public SalesSummaryView() {
        this.summaryController = new SalesSummaryController();
    }

    public void show() {
        while (true) {
            System.out.println("\n====================================================================");
            System.out.println("|                      SALES SUMMARY                               |");
            System.out.println("====================================================================");
            System.out.println("1. Daily Summary (Today)");
            System.out.println("2. Weekly Summary (Last 7 days)");
            System.out.println("3. Monthly Summary (Last 30 days)");
            System.out.println("4. Custom Date Range");
            System.out.println("0. Back");
            System.out.print("\nEnter your choice: ");

            int choice = FastInput.readInt();

            switch (choice) {
                case 1:
                    summaryController.showDailySummary();
                    break;
                case 2:
                    summaryController.showWeeklySummary();
                    break;
                case 3:
                    summaryController.showMonthlySummary();
                    break;
                case 4:
                    showCustomSummary();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }

            System.out.println("\nPress Enter to continue...");
            FastInput.readLine();
        }
    }

    private void showCustomSummary() {
        System.out.println("\n--------------------------------------------------------------------");
        System.out.println("|                   CUSTOM DATE RANGE                              |");
        System.out.println("--------------------------------------------------------------------");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            System.out.print("Enter Start Date (YYYY-MM-DD): ");
            String startStr = FastInput.readLine();
            LocalDate startDate = LocalDate.parse(startStr, formatter);

            System.out.print("Enter End Date (YYYY-MM-DD): ");
            String endStr = FastInput.readLine();
            LocalDate endDate = LocalDate.parse(endStr, formatter);

            if (endDate.isBefore(startDate)) {
                System.out.println("Error: End date cannot be before start date!");
                return;
            }

            summaryController.showCustomSummary(startDate, endDate);

        } catch (DateTimeParseException e) {
            System.out.println("Error: Invalid date format! Please use YYYY-MM-DD");
        }
    }
}