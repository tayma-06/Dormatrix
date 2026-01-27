package controllers.store;

import models.store.Item;
import exceptions.InsufficientInventoryException;

import java.io.*;

public class InventoryController {

    private Item[] items = new Item[100];
    private int count = 0;
    private final String FILE = "data/inventories/inventory.txt";

    public InventoryController() {
        load();
    }

    private void load() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE));
            String line;
            while ((line = br.readLine()) != null) {
                items[count++] = Item.fromString(line);
            }
            br.close();
        } catch (IOException e) { }
    }

    private void save() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE));
            for (int i = 0; i < count; i++) {
                pw.println(items[i]);
            }
            pw.close();
        } catch (IOException e) { }
    }

    public Item getItem(String itemId) {
        for (int i = 0; i < count; i++) {
            if (items[i].getItemId().equals(itemId))
                return items[i];
        }
        return null;
    }

    public void reduceStock(String itemId, int qty)
            throws InsufficientInventoryException {

        Item i = getItem(itemId);
        if (i == null || i.getQuantity() < qty)
            throw new InsufficientInventoryException("Insufficient stock");

        i.reduceQuantity(qty);
        save();
    }

    public void showInventory() {
        for (int i = 0; i < count; i++) {
            System.out.println(items[i]);
        }
    }
}

