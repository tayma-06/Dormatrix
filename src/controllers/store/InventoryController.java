package controllers.store;

import models.store.Item;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryController {
    private final String FILE = "data/inventories/inventory.txt";

    public Item getItem(String itemId) {
        List<Item> list = readAll();
        for (Item item : list) {
            if (item.getItemId().equals(itemId)) return item;
        }
        return null;
    }

    public void reduceQuantity(String itemId, int qty) {
        List<Item> list = readAll();
        for (Item item : list) {
            if (item.getItemId().equals(itemId)) {
                item.reduceQuantity(qty);
                break;
            }
        }
        writeAll(list);
    }

    public Item[] getAllItems() {
        List<Item> list = readAll();
        return list.toArray(new Item[0]);
    }

    private List<Item> readAll() {
        List<Item> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                Item item = Item.fromString(line);
                if (item != null) list.add(item);
            }
        } catch (IOException ignored) { }
        return list;
    }

    private void writeAll(List<Item> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (Item item : list) pw.println(item.toFileString());
        } catch (IOException ignored) { }
    }

    public void showInventory() {
        System.out.println("\nItemID  Name                 Qty   Price");
        System.out.println("----------------------------------------");
        for (Item item : getAllItems()) {
            System.out.printf("%-7s %-20s %3d $%.2f\n",
                    item.getItemId(), item.getName(), item.getQuantity(), item.getPrice());
        }
    }

    public int getItemCount() {
        return getAllItems().length;
    }

    public boolean addItem(String itemId, String name, int quantity, double price) {
        if (itemId == null || itemId.isEmpty() || name == null || name.isEmpty() || quantity <= 0 || price < 0) {
            return false; // Invalid input
        }

        List<Item> list = readAll();

        // Check if item with same ID already exists
        for (Item item : list) {
            if (item.getItemId().equals(itemId)) {
                System.out.println("✗ Item with this ID already exists!");
                return false;
            }
        }

        // Add new item
        list.add(new Item(itemId, name, quantity, price));
        writeAll(list);
        System.out.println("✓ Item added successfully!");
        return true;
    }

    public boolean updateItem(String itemId, String newName, int newQuantity, double newPrice) {
        if (itemId == null || itemId.isEmpty() || newName == null || newName.isEmpty() || newQuantity < 0 || newPrice < 0) {
            return false; // Invalid input
        }

        List<Item> list = readAll();
        boolean found = false;

        for (Item item : list) {
            if (item.getItemId().equals(itemId)) {
                // Update fields
                item.setName(newName);
                item.setQuantity(newQuantity);
                item.setPrice(newPrice);
                found = true;
                break;
            }
        }

        if (found) {
            writeAll(list); // Save changes back to file
            System.out.println("✓ Item updated successfully!");
            return true;
        } else {
            System.out.println("✗ Item not found!");
            return false;
        }
    }

    public boolean deleteItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) return false;

        List<Item> list = readAll();
        boolean found = false;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getItemId().equals(itemId)) {
                list.remove(i);
                found = true;
                break;
            }
        }

        if (found) {
            writeAll(list); // Save changes back to file
            System.out.println("✓ Item deleted successfully!");
            return true;
        } else {
            System.out.println("✗ Item not found!");
            return false;
        }
    }
    public boolean addStock(String itemId, int addQty) {
        if (itemId == null || itemId.isEmpty() || addQty <= 0) {
            return false; // Invalid input
        }

        List<Item> list = readAll();
        boolean found = false;

        for (Item item : list) {
            if (item.getItemId().equals(itemId)) {
                item.setQuantity(item.getQuantity() + addQty); // Increase stock
                found = true;
                break;
            }
        }

        if (found) {
            writeAll(list); // Save updated inventory
            System.out.println("✓ Stock added successfully!");
            return true;
        } else {
            System.out.println("✗ Item not found!");
            return false;
        }
    }

    public Item[] searchByName(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return new Item[0]; // Return empty array if search term is invalid
        }

        List<Item> list = readAll();
        List<Item> results = new ArrayList<>();

        String lowerTerm = searchTerm.toLowerCase();

        for (Item item : list) {
            if (item.getName().toLowerCase().contains(lowerTerm)) {
                results.add(item);
            }
        }

        return results.toArray(new Item[0]);
    }

    public void showItems(Item[] results) {
        if (results == null || results.length == 0) {
            System.out.println("No items found.");
            return;
        }

        System.out.println("\nItemID  Name                 Qty   Price");
        System.out.println("----------------------------------------");
        for (Item item : results) {
            System.out.printf("%-7s %-20s %3d $%.2f\n",
                    item.getItemId(), item.getName(), item.getQuantity(), item.getPrice());
        }
    }

    public Item[] filterByPriceRange(double minPrice, double maxPrice) {
        if (minPrice > maxPrice) {
            double temp = minPrice;
            minPrice = maxPrice;
            maxPrice = temp; // swap if min > max
        }

        List<Item> list = readAll();
        List<Item> results = new ArrayList<>();

        for (Item item : list) {
            if (item.getPrice() >= minPrice && item.getPrice() <= maxPrice) {
                results.add(item);
            }
        }

        return results.toArray(new Item[0]);
    }

    public Item[] getLowStockItems(int threshold) {
        if (threshold < 0) threshold = 0; // treat negative threshold as 0

        List<Item> list = readAll();
        List<Item> lowStock = new ArrayList<>();

        for (Item item : list) {
            if (item.getQuantity() <= threshold) {
                lowStock.add(item);
            }
        }

        return lowStock.toArray(new Item[0]);
    }

}
