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
            File f = new File(FILE);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
                return;
            }

            BufferedReader br = new BufferedReader(new FileReader(FILE));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    items[count++] = Item.fromString(line);
                }
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Error loading inventory: " + e.getMessage());
        }
    }

    private void save() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FILE));
            for (int i = 0; i < count; i++) {
                pw.println(items[i]);
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error saving inventory: " + e.getMessage());
        }
    }

    public Item getItem(String itemId) {
        for (int i = 0; i < count; i++) {
            if (items[i].getItemId().equals(itemId)) {
                return items[i];
            }
        }
        return null;
    }

    public void reduceStock(String itemId, int qty) throws InsufficientInventoryException {
        Item item = getItem(itemId);
        if (item == null) {
            throw new InsufficientInventoryException("Item not found");
        }
        if (item.getQuantity() < qty) {
            throw new InsufficientInventoryException("Insufficient stock. Available: " + item.getQuantity());
        }

        item.reduceQuantity(qty);
        save();
    }

    // Add new item to inventory
    public boolean addItem(String itemId, String name, int quantity, double price) {
        // Check if item already exists
        if (getItem(itemId) != null) {
            System.out.println("Error: Item ID already exists!");
            return false;
        }

        if (count >= items.length) {
            System.out.println("Error: Inventory is full!");
            return false;
        }

        items[count++] = new Item(itemId, name, quantity, price);
        save();
        return true;
    }

    // Update existing item
    public boolean updateItem(String itemId, String newName, int newQuantity, double newPrice) {
        for (int i = 0; i < count; i++) {
            if (items[i].getItemId().equals(itemId)) {
                items[i] = new Item(itemId, newName, newQuantity, newPrice);
                save();
                return true;
            }
        }
        return false;
    }

    // Delete item from inventory
    public boolean deleteItem(String itemId) {
        for (int i = 0; i < count; i++) {
            if (items[i].getItemId().equals(itemId)) {
                // Shift items left
                for (int j = i; j < count - 1; j++) {
                    items[j] = items[j + 1];
                }
                items[--count] = null;
                save();
                return true;
            }
        }
        return false;
    }

    // Increase stock quantity
    public boolean addStock(String itemId, int quantity) {
        Item item = getItem(itemId);
        if (item == null) {
            return false;
        }

        int newQuantity = item.getQuantity() + quantity;
        return updateItem(itemId, item.getName(), newQuantity, item.getPrice());
    }

    // Search items by name (partial match)
    public Item[] searchByName(String searchTerm) {
        Item[] results = new Item[count];
        int resultCount = 0;

        String lowerSearch = searchTerm.toLowerCase();
        for (int i = 0; i < count; i++) {
            if (items[i].getName().toLowerCase().contains(lowerSearch)) {
                results[resultCount++] = items[i];
            }
        }

        Item[] trimmedResults = new Item[resultCount];
        System.arraycopy(results, 0, trimmedResults, 0, resultCount);
        return trimmedResults;
    }

    // Filter items by price range
    public Item[] filterByPriceRange(double minPrice, double maxPrice) {
        Item[] results = new Item[count];
        int resultCount = 0;

        for (int i = 0; i < count; i++) {
            double price = items[i].getPrice();
            if (price >= minPrice && price <= maxPrice) {
                results[resultCount++] = items[i];
            }
        }

        Item[] trimmedResults = new Item[resultCount];
        System.arraycopy(results, 0, trimmedResults, 0, resultCount);
        return trimmedResults;
    }

    // Filter items with low stock
    public Item[] getLowStockItems(int threshold) {
        Item[] results = new Item[count];
        int resultCount = 0;

        for (int i = 0; i < count; i++) {
            if (items[i].getQuantity() <= threshold) {
                results[resultCount++] = items[i];
            }
        }

        Item[] trimmedResults = new Item[resultCount];
        System.arraycopy(results, 0, trimmedResults, 0, resultCount);
        return trimmedResults;
    }

    public void showInventory() {
        if (count == 0) {
            System.out.println("  No items in inventory.");
            return;
        }

        System.out.println("--------------------------------------------------------------------");
        System.out.printf("  %-10s %-25s %10s %10s\n", "Item ID", "Name", "Quantity", "Price");
        System.out.println("--------------------------------------------------------------------");

        for (int i = 0; i < count; i++) {
            Item item = items[i];
            System.out.printf("  %-10s %-25s %10d $%9.2f\n",
                    item.getItemId(), item.getName(), item.getQuantity(), item.getPrice());
        }
        System.out.println("--------------------------------------------------------------------");
    }

    public void showItems(Item[] itemList) {
        if (itemList.length == 0) {
            System.out.println("  No items found.");
            return;
        }

        System.out.println("--------------------------------------------------------------------");
        System.out.printf("  %-10s %-25s %10s %10s\n", "Item ID", "Name", "Quantity", "Price");
        System.out.println("--------------------------------------------------------------------");

        for (Item item : itemList) {
            System.out.printf("  %-10s %-25s %10d $%9.2f\n",
                    item.getItemId(), item.getName(), item.getQuantity(), item.getPrice());
        }
        System.out.println("--------------------------------------------------------------------");
    }

    public int getItemCount() {
        return count;
    }

    public Item[] getAllItems() {
        Item[] allItems = new Item[count];
        System.arraycopy(items, 0, allItems, 0, count);
        return allItems;
    }
}
