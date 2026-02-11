package controllers.store;

import models.store.SaleRecord;
import java.io.*;
import java.time.LocalDate;

public class SalesController {
    private static final String SALES_FILE = "data/store/sales.txt";

    /**
     * Records a sale transaction to the sales file
     * Format: studentId,itemId,quantity,amount,date
     */
    public boolean recordSale(String studentId, String itemId, int quantity, double total) {
        if (studentId == null || itemId == null || quantity <= 0 || total < 0) {
            System.out.println("✗ Invalid sale data");
            return false;
        }

        // Write in exact CSV format: studentId,itemId,quantity,amount,date
        String saleRecord = String.format("%s,%s,%d,%.2f,%s",
                studentId,
                itemId,
                quantity,
                total,
                LocalDate.now().toString()  // Format: YYYY-MM-DD
        );

        try (PrintWriter pw = new PrintWriter(new FileWriter(SALES_FILE, true))) {
            pw.println(saleRecord);
            return true;
        } catch (IOException e) {
            System.out.println("✗ Error recording sale: " + e.getMessage());
            return false;
        }
    }

    /**
     * Records multiple items from a shopping cart
     */
    public boolean recordCartSale(String studentId, models.store.CartItem[] items) {
        if (studentId == null || items == null || items.length == 0) {
            return false;
        }

        boolean allRecorded = true;
        for (models.store.CartItem item : items) {
            if (!recordSale(studentId, item.getItemId(), item.getQuantity(), item.getSubtotal())) {
                allRecorded = false;
            }
        }
        return allRecorded;
    }
}