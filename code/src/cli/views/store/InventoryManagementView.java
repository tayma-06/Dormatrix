package cli.views.store;

import controllers.store.InventoryController;
import models.store.Item;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

public class InventoryManagementView {

    private final InventoryController inventoryController;

    public InventoryManagementView() {
        this.inventoryController = new InventoryController();
    }

    public void show() {
        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);

            int totalItems = inventoryController.getItemCount();
            Item[] lowStock = inventoryController.getLowStockItems(5);

            TerminalUI.tBoxTop();
            TerminalUI.tBoxTitle("INVENTORY MANAGEMENT");
            TerminalUI.tBoxSep();
            String summary = "Total Items: " + totalItems;
            if (lowStock.length > 0) {
                summary += " | Low Stock: " + lowStock.length + " items";
            }
            TerminalUI.tBoxLine(summary);
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("[1] View All Inventory");
            TerminalUI.tBoxLine("[2] Add New Item");
            TerminalUI.tBoxLine("[3] Update Item");
            TerminalUI.tBoxLine("[4] Delete Item");
            TerminalUI.tBoxLine("[5] Add Stock");
            TerminalUI.tBoxLine("[6] Search Items");
            TerminalUI.tBoxLine("[7] View Low Stock Items");
            TerminalUI.tBoxLine("[8] View Items by Price Range");
            TerminalUI.tBoxLine("[0] Back", utils.ConsoleColors.Accent.EXIT);
            TerminalUI.tBoxSep();
            TerminalUI.tInputRow();

            String choice = FastInput.readLine();

            switch (choice) {
                case "1":
                    viewInventory();
                    break;
                case "2":
                    addNewItem();
                    break;
                case "3":
                    updateItem();
                    break;
                case "4":
                    deleteItem();
                    break;
                case "5":
                    addStock();
                    break;
                case "6":
                    searchItems();
                    break;
                case "7":
                    viewLowStock();
                    break;
                case "8":
                    viewByPriceRange();
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

    private void viewInventory() {
        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("COMPLETE INVENTORY");
        TerminalUI.tBoxSep();

        Item[] items = inventoryController.getAllItems();
        if (items.length == 0) {
            TerminalUI.tBoxLine("No items in inventory.");
        } else {
            TerminalUI.tBoxLine(String.format("%-10s %-20s %10s %8s %6s", "ID", "Name", "Price", "Stock", "Status"));
            TerminalUI.tBoxSep();
            for (Item item : items) {
                String status = item.getQuantity() == 0 ? "OUT" : item.getQuantity() <= 5 ? "LOW" : "OK";
                TerminalUI.tBoxLine(String.format("%-10s %-20s $%8.2f %8d %6s",
                        item.getItemId(), item.getName(), item.getPrice(), item.getQuantity(), status));
            }
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("Total Items: " + items.length);
        }
        TerminalUI.tBoxBottom();
    }

    private void addNewItem() {
        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("ADD NEW ITEM");
        TerminalUI.tBoxBottom();

        TerminalUI.tPrompt("Enter Item ID: ");
        String itemId = FastInput.readLine();
        if (InventoryController.getItem(itemId) != null) {
            TerminalUI.tError("Item with this ID already exists!");
            return;
        }
        TerminalUI.tPrompt("Enter Item Name: ");
        String name = FastInput.readLine();
        TerminalUI.tPrompt("Enter Initial Quantity: ");
        int quantity = FastInput.readInt();
        TerminalUI.tPrompt("Enter Price: $");
        double price = FastInput.readDouble();

        if (inventoryController.addItem(itemId, name, quantity, price)) {
            TerminalUI.tSuccess("Item added successfully!");
            TerminalUI.tBoxLine(String.format("ID: %s | Name: %s | Qty: %d | Price: $%.2f", itemId, name, quantity, price));
        } else {
            TerminalUI.tError("Failed to add item!");
        }
    }

    private void updateItem() {
        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("UPDATE ITEM");
        TerminalUI.tBoxBottom();

        TerminalUI.tPrompt("Enter Item ID to update: ");
        String itemId = FastInput.readLine();
        Item item = InventoryController.getItem(itemId);
        if (item == null) {
            TerminalUI.tError("Item not found!");
            return;
        }

        TerminalUI.tBoxTop();
        TerminalUI.tBoxLine("Name:     " + item.getName());
        TerminalUI.tBoxLine("Quantity: " + item.getQuantity());
        TerminalUI.tBoxLine(String.format("Price:    $%.2f", item.getPrice()));
        TerminalUI.tBoxBottom();

        TerminalUI.tPrint("Enter new values (Enter to keep current):");
        TerminalUI.tPrompt("New Name [" + item.getName() + "]: ");
        String newName = FastInput.readLine();
        if (newName.isEmpty()) {
            newName = item.getName();
        }
        TerminalUI.tPrompt("New Quantity [" + item.getQuantity() + "]: ");
        String qtyInput = FastInput.readLine();
        int newQuantity = qtyInput.isEmpty() ? item.getQuantity() : Integer.parseInt(qtyInput);
        TerminalUI.tPrompt("New Price [" + item.getPrice() + "]: $");
        String priceInput = FastInput.readLine();
        double newPrice = priceInput.isEmpty() ? item.getPrice() : Double.parseDouble(priceInput);

        if (inventoryController.updateItem(itemId, newName, newQuantity, newPrice)) {
            TerminalUI.tSuccess("Item updated successfully!");
        } else {
            TerminalUI.tError("Failed to update item!");
        }
    }

    private void deleteItem() {
        TerminalUI.tEmpty();
        TerminalUI.tPrompt("Enter Item ID to delete: ");
        String itemId = FastInput.readLine();
        Item item = InventoryController.getItem(itemId);
        if (item == null) {
            TerminalUI.tError("Item not found!");
            return;
        }

        TerminalUI.tBoxTop();
        TerminalUI.tBoxLine("ID:       " + item.getItemId());
        TerminalUI.tBoxLine("Name:     " + item.getName());
        TerminalUI.tBoxLine("Quantity: " + item.getQuantity());
        TerminalUI.tBoxLine(String.format("Price:    $%.2f", item.getPrice()));
        TerminalUI.tBoxBottom();

        TerminalUI.tPrompt("Are you sure you want to delete? (y/n): ");
        String confirm = FastInput.readLine();
        if (confirm.equalsIgnoreCase("y")) {
            if (inventoryController.deleteItem(itemId)) {
                TerminalUI.tSuccess("Item deleted successfully!");
            } else {
                TerminalUI.tError("Failed to delete item!");
            }
        } else {
            TerminalUI.tPrint("Deletion cancelled.");
        }
    }

    private void addStock() {
        TerminalUI.tEmpty();
        TerminalUI.tPrompt("Enter Item ID: ");
        String itemId = FastInput.readLine();
        Item item = InventoryController.getItem(itemId);
        if (item == null) {
            TerminalUI.tError("Item not found!");
            return;
        }
        TerminalUI.tBoxLine("Current Stock: " + item.getQuantity());
        TerminalUI.tPrompt("Enter quantity to add: ");
        int addQty = FastInput.readInt();

        if (inventoryController.addStock(itemId, addQty)) {
            Item updated = InventoryController.getItem(itemId);
            TerminalUI.tSuccess("Stock updated successfully!");
            TerminalUI.tBoxLine(String.format("Previous: %d | Added: %d | New Total: %d",
                    item.getQuantity(), addQty, updated.getQuantity()));
        } else {
            TerminalUI.tError("Failed to update stock!");
        }
    }

    private void searchItems() {
        TerminalUI.tEmpty();
        TerminalUI.tPrompt("Enter search term (name): ");
        String searchTerm = FastInput.readLine();

        Item[] results = inventoryController.searchByName(searchTerm);
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("SEARCH RESULTS");
        TerminalUI.tBoxSep();
        if (results.length == 0) {
            TerminalUI.tBoxLine("No items found matching '" + searchTerm + "'");
        } else {
            for (Item item : results) {
                TerminalUI.tBoxLine(String.format("%-10s %-20s $%8.2f  Qty: %d",
                        item.getItemId(), item.getName(), item.getPrice(), item.getQuantity()));
            }
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("Found " + results.length + " item(s)");
        }
        TerminalUI.tBoxBottom();
    }

    private void viewLowStock() {
        TerminalUI.tEmpty();
        TerminalUI.tPrompt("Enter threshold (default 5): ");
        String input = FastInput.readLine();
        int threshold = input.isEmpty() ? 5 : Integer.parseInt(input);

        Item[] lowStock = inventoryController.getLowStockItems(threshold);
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("LOW STOCK ALERT (Stock <= " + threshold + ")");
        TerminalUI.tBoxSep();
        if (lowStock.length == 0) {
            TerminalUI.tBoxLine("No low stock items found!");
        } else {
            for (Item item : lowStock) {
                String alert = item.getQuantity() == 0 ? "[OUT]" : "[LOW]";
                TerminalUI.tBoxLine(String.format("%s %-10s %-20s  Stock: %d  $%.2f",
                        alert, item.getItemId(), item.getName(), item.getQuantity(), item.getPrice()));
            }
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("Total Low Stock Items: " + lowStock.length);
        }
        TerminalUI.tBoxBottom();
    }

    private void viewByPriceRange() {
        TerminalUI.tEmpty();
        TerminalUI.tPrompt("Enter minimum price: $");
        double minPrice = FastInput.readDouble();
        TerminalUI.tPrompt("Enter maximum price: $");
        double maxPrice = FastInput.readDouble();

        Item[] results = inventoryController.filterByPriceRange(minPrice, maxPrice);
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle(String.format("ITEMS: $%.2f - $%.2f", minPrice, maxPrice));
        TerminalUI.tBoxSep();
        if (results.length == 0) {
            TerminalUI.tBoxLine("No items found in this price range.");
        } else {
            for (Item item : results) {
                TerminalUI.tBoxLine(String.format("%-10s %-20s $%8.2f  Qty: %d",
                        item.getItemId(), item.getName(), item.getPrice(), item.getQuantity()));
            }
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("Found " + results.length + " item(s)");
        }
        TerminalUI.tBoxBottom();
    }
}
