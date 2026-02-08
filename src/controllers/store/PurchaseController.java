package controllers.store;

import models.store.*;
import exceptions.InsufficientInventoryException;

import java.io.*;

public class PurchaseController {
    private InventoryController inventory;
    private final String SALES = "data/inventories/sales.txt";
    private final String DUES = "data/inventories/dues.txt";

    public PurchaseController(InventoryController inventory) {
        this.inventory = inventory;
    }

    public void purchase(String studentId, String itemId, int qty, boolean credit)
            throws InsufficientInventoryException {

        Item item = inventory.getItem(itemId);
        if (item == null) {
            throw new InsufficientInventoryException("Item not found");
        }

        inventory.reduceStock(itemId, qty);

        double total = item.getPrice() * qty;
        writeSale(new SaleRecord(studentId, itemId, qty, total));

        if (credit) {
            updateDue(studentId, total);
        }
    }

    private void writeSale(SaleRecord s) {
        try {
            File f = new File(SALES);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            PrintWriter pw = new PrintWriter(new FileWriter(SALES, true));
            pw.println(s);
            pw.close();
        } catch (IOException e) {
            System.out.println("Error recording sale: " + e.getMessage());
        }
    }

    private void updateDue(String studentId, double amount) {
        try {
            File f = new File(DUES);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            BufferedReader br = new BufferedReader(new FileReader(DUES));
            StringBuilder sb = new StringBuilder();
            boolean found = false;
            String line;

            while ((line = br.readLine()) != null) {
                DueRecord d = DueRecord.fromString(line);
                if (d.getStudentId().equals(studentId)) {
                    d.addAmount(amount);
                    found = true;
                }
                sb.append(d).append("\n");
            }
            br.close();

            if (!found) {
                sb.append(new DueRecord(studentId, amount)).append("\n");
            }

            PrintWriter pw = new PrintWriter(new FileWriter(DUES));
            pw.print(sb);
            pw.close();

        } catch (IOException e) {
            System.out.println("Error updating dues: " + e.getMessage());
        }
    }
}