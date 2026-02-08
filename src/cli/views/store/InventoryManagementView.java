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
            System.out.println("\n====================================================================");
            System.out.println("|                   INVENTORY MANAGEMENT                           |");
            System.out.println("====================================================================");
            System.out.println("1. View All Items");
            System.out.println("2. Add New Item");
            System.out.println("3. Update Item");
            System.out.println("4. Delete Item");
            System.out.println("5. Add Stock to Existing Item");
            System.out.println("6. Search Items");
            System.out.println("7. Filter by Price Range");
            System.out.println("8. View Low Stock Items");
            System.out.println("0. Back");
            System.out.print("\nEnter your choice: ");

            int choice = FastInput.readInt();

            switch (choice) {
                case 1:
                    viewAllItems();
                    break;
                case 2:
                    addNewItem();
                    break;
                case 3:
                    updateItem();
                    break;
                case 4:
                    deleteItem();
                    break;
                case 5:
                    addStock();
                    break;
                case 6:
                    searchItems();
                    break;
                case 7:
                    filterByPrice();
                    break;
                case 8:
                    viewLowStock();
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

    private void viewAllItems() {
        System.out.println("\n====================================================================");
        System.out.println("|                      ALL INVENTORY ITEMS                         |");
        System.out.println("====================================================================");
        inventoryController.showInventory();
        System.out.printf("\nTotal Items: %d\n", inventoryController.getItemCount());
    }

    private void addNewItem() {
        System.out.println("\n--------------------------------------------------------------------");
        System.out.println("|                       ADD NEW ITEM                               |");
        System.out.println("--------------------------------------------------------------------");

        System.out.print("Enter Item ID: ");
        String itemId = FastInput.readLine();

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
            System.out.println("\n✗ Failed to add item!");
        }
    }

    private void updateItem() {
        System.out.println("\n--------------------------------------------------------------------");
        System.out.println("|                       UPDATE ITEM                                |");
        System.out.println("--------------------------------------------------------------------");

        inventoryController.showInventory();

        // Check if inventory is empty
        if (inventoryController.getItemCount() == 0) {
            return;
        }

        System.out.print("\nEnter Item ID to update: ");
        String itemId = FastInput.readLine();

        Item item = inventoryController.getItem(itemId);
        if (item == null) {
            System.out.println("✗ Item not found!");
            return;
        }

        System.out.println("\nCurrent Details:");
        System.out.printf("  Name: %s\n", item.getName());
        System.out.printf("  Quantity: %d\n", item.getQuantity());
        System.out.printf("  Price: $%.2f\n", item.getPrice());

        System.out.print("\nEnter New Name (or press Enter to keep current): ");
        String newName = FastInput.readLine();
        if (newName.isEmpty()) newName = item.getName();

        System.out.print("Enter New Quantity (or -1 to keep current): ");
        int newQuantity = FastInput.readInt();
        if (newQuantity == -1) newQuantity = item.getQuantity();

        System.out.print("Enter New Price (or -1 to keep current): $");
        double newPrice = FastInput.readDouble();
        if (newPrice == -1) newPrice = item.getPrice();

        if (inventoryController.updateItem(itemId, newName, newQuantity, newPrice)) {
            System.out.println("\n✓ Item updated successfully!");
        } else {
            System.out.println("\n✗ Failed to update item!");
        }
    }

    private void deleteItem() {
        System.out.println("\n--------------------------------------------------------------------");
        System.out.println("|                       DELETE ITEM                                |");
        System.out.println("--------------------------------------------------------------------");

        inventoryController.showInventory();

        // Check if inventory is empty
        if (inventoryController.getItemCount() == 0) {
            return;
        }

        System.out.print("\nEnter Item ID to delete: ");
        String itemId = FastInput.readLine();

        Item item = inventoryController.getItem(itemId);
        if (item == null) {
            System.out.println("✗ Item not found!");
            return;
        }

        System.out.printf("\nAre you sure you want to delete '%s'? (y/n): ", item.getName());
        String confirm = FastInput.readLine();

        if (confirm.equalsIgnoreCase("y")) {
            if (inventoryController.deleteItem(itemId)) {
                System.out.println("✓ Item deleted successfully!");
            } else {
                System.out.println("✗ Failed to delete item!");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private void addStock() {
        System.out.println("\n--------------------------------------------------------------------");
        System.out.println("|                     ADD STOCK TO ITEM                            |");
        System.out.println("--------------------------------------------------------------------");

        inventoryController.showInventory();

        // Check if inventory is empty
        if (inventoryController.getItemCount() == 0) {
            return;
        }

        System.out.print("\nEnter Item ID: ");
        String itemId = FastInput.readLine();

        Item item = inventoryController.getItem(itemId);
        if (item == null) {
            System.out.println("✗ Item not found!");
            return;
        }

        System.out.printf("\nCurrent Stock: %d\n", item.getQuantity());
        System.out.print("Enter Quantity to Add: ");
        int addQty = FastInput.readInt();

        if (addQty <= 0) {
            System.out.println("✗ Invalid quantity!");
            return;
        }

        if (inventoryController.addStock(itemId, addQty)) {
            System.out.printf("\n✓ Stock added successfully!\n");
            System.out.printf("  New Stock Level: %d\n", item.getQuantity() + addQty);
        } else {
            System.out.println("\n✗ Failed to add stock!");
        }
    }

    private void searchItems() {
        System.out.println("\n--------------------------------------------------------------------");
        System.out.println("|                      SEARCH ITEMS                                |");
        System.out.println("--------------------------------------------------------------------");

        System.out.print("Enter search term: ");
        String searchTerm = FastInput.readLine();

        Item[] results = inventoryController.searchByName(searchTerm);

        System.out.println("\n====================================================================");
        System.out.println("|                      SEARCH RESULTS                              |");
        System.out.println("====================================================================");
        inventoryController.showItems(results);
        System.out.printf("\nFound %d item(s)\n", results.length);
    }

    private void filterByPrice() {
        System.out.println("\n--------------------------------------------------------------------");
        System.out.println("|                   FILTER BY PRICE RANGE                          |");
        System.out.println("--------------------------------------------------------------------");

        System.out.print("Enter Minimum Price: $");
        double minPrice = FastInput.readDouble();

        System.out.print("Enter Maximum Price: $");
        double maxPrice = FastInput.readDouble();

        Item[] results = inventoryController.filterByPriceRange(minPrice, maxPrice);

        System.out.println("\n====================================================================");
        System.out.println("|                      FILTER RESULTS                              |");
        System.out.println("====================================================================");
        inventoryController.showItems(results);
        System.out.printf("\nFound %d item(s) in price range $%.2f - $%.2f\n",
                results.length, minPrice, maxPrice);
    }

    private void viewLowStock() {
        System.out.println("\n--------------------------------------------------------------------");
        System.out.println("|                    LOW STOCK ALERT                               |");
        System.out.println("--------------------------------------------------------------------");

        System.out.print("Enter Stock Threshold: ");
        int threshold = FastInput.readInt();

        Item[] results = inventoryController.getLowStockItems(threshold);

        System.out.println("\n====================================================================");
        System.out.println("|                   LOW STOCK ITEMS                                |");
        System.out.println("====================================================================");
        inventoryController.showItems(results);
        System.out.printf("\nFound %d item(s) with stock <= %d\n", results.length, threshold);
    }
}