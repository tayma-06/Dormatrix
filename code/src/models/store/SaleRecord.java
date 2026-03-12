package models.store;

import java.time.LocalDate;

public class SaleRecord {
    private String studentId;
    private String itemId;
    private int quantity;
    private double total;
    private LocalDate date;

    public SaleRecord(String studentId, String itemId, int quantity, double total) {
        this.studentId = studentId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.total = total;
        this.date = LocalDate.now();
    }

    @Override
    public String toString() {
        return studentId + "," + itemId + "," + quantity + "," + total + "," + date;
    }
}

