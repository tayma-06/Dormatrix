package cli.views.store;

import controllers.store.InventoryController;
import models.store.Item;
import utils.FastInput;

public class InventoryManagementView {
    private final InventoryController inventoryController;

    public InventoryManagementView() {
        this.inventoryController = new InventoryController();
    }

    public void show() {
        while (true) {
            displayHeader();
            displayInventorySummary();
            displayMenu();

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
        System.out.println("|                   INVENTORY MANAGEMENT                           |");
        System.out.println("====================================================================");
    }

    private void displayInventorySummary() {
        int totalItems = inventoryController.getItemCount();
        Item[] lowStock = inventoryController.getLowStockItems(5);

        System.out.printf("  Total Items: %d", totalItems);
        if (lowStock.length > 0) {
            System.out.printf(" | ⚠ Low Stock: %d items\n", lowStock.length);
        } else {
            System.out.println();
        }
        System.out.println("--------------------------------------------------------------------");
    }

    private void displayMenu() {
        System.out.println("\nOptions:");
        System.out.println("  [1] View All Inventory");
        System.out.println("  [2] Add New Item");
        System.out.println("  [3] Update Item");
        System.out.println("  [4] Delete Item");
        System.out.println("  [5] Add Stock");
        System.out.println("  [6] Search Items");
        System.out.println("  [7] View Low Stock Items");
        System.out.println("  [8] View Items by Price Range");
        System.out.println("  [0] Back");
        System.out.print("\nEnter your choice: ");
    }

    private void viewInventory() {
        System.out.println("\n====================================================================");
        System.out.println("|                    COMPLETE INVENTORY                            |");
        System.out.println("====================================================================");

        Item[] items = inventoryController.getAllItems();

        if (items.length == 0) {
            System.out.println("  No items in inventory.");
            return;
        }

        System.out.printf("  %-10s %-25s %10s %10s %8s\n",
                "Item ID", "Name", "Price", "Stock", "Status");
        System.out.println("--------------------------------------------------------------------");

        for (Item item : items) {
            String status;
            if (item.getQuantity() == 0) {
                status = "✗ OUT";
            } else if (item.getQuantity() <= 5) {
                status = "⚠ LOW";
            } else {
                status = "✓ OK";
            }

            System.out.printf("  %-10s %-25s $%9.2f %10d %8s\n",
                    item.getItemId(), item.getName(), item.getPrice(),
                    item.getQuantity(), status);
        }

        System.out.println("====================================================================");
        System.out.printf("  Total Items: %d\n", items.length);
        System.out.println("====================================================================");
    }

    private void addNewItem() {
        System.out.println("\n--- Add New Item ---");

        System.out.print("Enter Item ID: ");
        String itemId = FastInput.readLine();

        if (inventoryController.getItem(itemId) != null) {
            System.out.println("✗ Item with this ID already exists!");
            return;
        }

        System.out.print("Enter Item Name: ");
        String name = FastInput.readLine();

        System.out.print("Enter Initial Quantity: ");
        int quantity = FastInput.readInt();

        System.out.print("Enter Price: $");
        double price = FastInput.readDouble();

        if (inventoryController.addItem(itemId, name, quantity, price)) {
            System.out.println("\n✓ Item added successfully!");
            System.out.printf("  ID: %s | Name: %s | Qty: %d | Price: $%.2f\n",
                    itemId, name, quantity, price);
        } else {
            System.out.println("✗ Failed to add item!");
        }
    }

    private void updateItem() {
        System.out.println("\n--- Update Item ---");

        System.out.print("Enter Item ID to update: ");
        String itemId = FastInput.readLine();

        Item item = inventoryController.getItem(itemId);
        if (item == null) {
            System.out.println("✗ Item not found!");
            return;
        }

        System.out.println("\nCurrent Details:");
        System.out.printf("  Name:     %s\n", item.getName());
        System.out.printf("  Quantity: %d\n", item.getQuantity());
        System.out.printf("  Price:    $%.2f\n", item.getPrice());

        System.out.println("\nEnter New Details (press Enter to keep current):");

        System.out.print("New Name [" + item.getName() + "]: ");
        String newName = FastInput.readLine();
        if (newName.isEmpty()) newName = item.getName();

        System.out.print("New Quantity [" + item.getQuantity() + "]: ");
        String qtyInput = FastInput.readLine();
        int newQuantity = qtyInput.isEmpty() ? item.getQuantity() : Integer.parseInt(qtyInput);

        System.out.print("New Price [" + item.getPrice() + "]: $");
        String priceInput = FastInput.readLine();
        double newPrice = priceInput.isEmpty() ? item.getPrice() : Double.parseDouble(priceInput);

        if (inventoryController.updateItem(itemId, newName, newQuantity, newPrice)) {
            System.out.println("\n✓ Item updated successfully!");
        } else {
            System.out.println("✗ Failed to update item!");
        }
    }

    private void deleteItem() {
        System.out.println("\n--- Delete Item ---");

        System.out.print("Enter Item ID to delete: ");
        String itemId = FastInput.readLine();

        Item item = inventoryController.getItem(itemId);
        if (item == null) {
            System.out.println("✗ Item not found!");
            return;
        }

        System.out.println("\nItem Details:");
        System.out.printf("  ID:       %s\n", item.getItemId());
        System.out.printf("  Name:     %s\n", item.getName());
        System.out.printf("  Quantity: %d\n", item.getQuantity());
        System.out.printf("  Price:    $%.2f\n", item.getPrice());

        System.out.print("\n⚠ Are you sure you want to delete this item? (y/n): ");
        String confirm = FastInput.readLine();

        if (confirm.equalsIgnoreCase("y")) {
            if (inventoryController.deleteItem(itemId)) {
                System.out.println("✓ Item deleted successfully!");
            } else {
                System.out.println("✗ Failed to delete item!");
            }
        } else {
            System.out.println("✗ Deletion cancelled.");
        }
    }

    private void addStock() {
        System.out.println("\n--- Add Stock ---");

        System.out.print("Enter Item ID: ");
        String itemId = FastInput.readLine();

        Item item = inventoryController.getItem(itemId);
        if (item == null) {
            System.out.println("✗ Item not found!");
            return;
        }

        System.out.printf("Current Stock: %d\n", item.getQuantity());
        System.out.print("Enter quantity to add: ");
        int addQty = FastInput.readInt();

        if (inventoryController.addStock(itemId, addQty)) {
            Item updated = inventoryController.getItem(itemId);
            System.out.println("\n✓ Stock updated successfully!");
            System.out.printf("  Previous: %d | Added: %d | New Total: %d\n",
                    item.getQuantity(), addQty, updated.getQuantity());
        } else {
            System.out.println("✗ Failed to update stock!");
        }
    }

    private void searchItems() {
        System.out.println("\n--- Search Items ---");

        System.out.print("Enter search term (name): ");
        String searchTerm = FastInput.readLine();

        Item[] results = inventoryController.searchByName(searchTerm);

        System.out.println("\n====================================================================");
        System.out.println("|                      SEARCH RESULTS                              |");
        System.out.println("====================================================================");

        if (results.length == 0) {
            System.out.println("  No items found matching '" + searchTerm + "'");
            return;
        }

        inventoryController.showItems(results);
        System.out.printf("\nFound %d item(s)\n", results.length);
        System.out.println("====================================================================");
    }

    private void viewLowStock() {
        System.out.println("\n--- Low Stock Items ---");

        System.out.print("Enter threshold (default 5): ");
        String input = FastInput.readLine();
        int threshold = input.isEmpty() ? 5 : Integer.parseInt(input);

        Item[] lowStock = inventoryController.getLowStockItems(threshold);

        System.out.println("\n====================================================================");
        System.out.println("|                    LOW STOCK ALERT                               |");
        System.out.println("====================================================================");
        System.out.printf("  Items with stock <= %d:\n", threshold);
        System.out.println("--------------------------------------------------------------------");

        if (lowStock.length == 0) {
            System.out.println("  ✓ No low stock items found!");
            return;
        }

        System.out.printf("  %-10s %-25s %10s %10s\n", "Item ID", "Name", "Stock", "Price");
        System.out.println("--------------------------------------------------------------------");

        for (Item item : lowStock) {
            String alert = item.getQuantity() == 0 ? "✗" : "⚠";
            System.out.printf("  %s %-9s %-25s %10d $%9.2f\n",
                    alert, item.getItemId(), item.getName(),
                    item.getQuantity(), item.getPrice());
        }

        System.out.println("====================================================================");
        System.out.printf("  Total Low Stock Items: %d\n", lowStock.length);
        System.out.println("====================================================================");
    }

    private void viewByPriceRange() {
        System.out.println("\n--- Filter by Price Range ---");

        System.out.print("Enter minimum price: $");
        double minPrice = FastInput.readDouble();

        System.out.print("Enter maximum price: $");
        double maxPrice = FastInput.readDouble();

        Item[] results = inventoryController.filterByPriceRange(minPrice, maxPrice);

        System.out.println("\n====================================================================");
        System.out.printf("|          ITEMS IN PRICE RANGE: $%.2f - $%.2f           |\n",
                minPrice, maxPrice);
        System.out.println("====================================================================");

        if (results.length == 0) {
            System.out.println("  No items found in this price range.");
            return;
        }

        inventoryController.showItems(results);
        System.out.printf("\nFound %d item(s) in this price range\n", results.length);
        System.out.println("====================================================================");
    }
}