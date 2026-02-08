package cli.views.store;

import controllers.store.InventoryController;
import models.store.Item;
import models.store.ShoppingCart;
import utils.FastInput;

public class InventoryListView {

    private final InventoryController inventoryController;
    private ShoppingCart cart;

    public InventoryListView() {
        this.inventoryController = new InventoryController();
    }

    // Constructor with cart for shopping integration
    public InventoryListView(ShoppingCart cart) {
        this.inventoryController = new InventoryController();
        this.cart = cart;
    }

    // Simple display (original functionality)
    public void show() {
        System.out.println("====================================================================");
        System.out.println("|                        INVENTORY LIST                            |");
        System.out.println("====================================================================");
        inventoryController.showInventory();
        System.out.println("====================================================================");
    }

    // Enhanced display with cart integration
    public void showWithCartOptions() {
        if (cart == null) {
            show(); // Fall back to simple display
            return;
        }

        while (true) {
            System.out.println("\n====================================================================");
            System.out.println("|                   BROWSE STORE INVENTORY                         |");
            System.out.println("====================================================================");

            // Show current cart summary
            if (!cart.isEmpty()) {
                System.out.printf("  🛒 Cart: %d item(s) | Total: $%.2f\n",
                        cart.getItemCount(), cart.getTotal());
                System.out.println("--------------------------------------------------------------------");
            }

            inventoryController.showInventory();

            if (inventoryController.getItemCount() == 0) {
                System.out.println("  No items available for purchase.");
                return;
            }

            System.out.println("\n--------------------------------------------------------------------");
            System.out.println("Options:");
            System.out.println("  [A] Quick Add to Cart");
            System.out.println("  [V] View Item Details");
            System.out.println("  [S] Search Items");
            System.out.println("  [C] View Cart");
            System.out.println("  [B] Back");
            System.out.print("\nEnter your choice: ");

            String choice = FastInput.readLine().toUpperCase();

            switch (choice) {
                case "A":
                    quickAddToCart();
                    break;
                case "V":
                    viewItemDetails();
                    break;
                case "S":
                    searchAndAdd();
                    break;
                case "C":
                    viewCart();
                    break;
                case "B":
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // Quick add item to cart
    private void quickAddToCart() {
        System.out.print("\nEnter Item ID to add: ");
        String itemId = FastInput.readLine();

        Item item = inventoryController.getItem(itemId);
        if (item == null) {
            System.out.println("✗ Item not found!");
            return;
        }

        if (item.getQuantity() <= 0) {
            System.out.println("✗ Item out of stock!");
            return;
        }

        System.out.printf("Adding: %s - $%.2f (Available: %d)\n",
                item.getName(), item.getPrice(), item.getQuantity());

        System.out.print("Enter Quantity: ");
        int qty = FastInput.readInt();

        if (qty <= 0) {
            System.out.println("✗ Invalid quantity!");
            return;
        }

        if (qty > item.getQuantity()) {
            System.out.println("✗ Insufficient stock! Available: " + item.getQuantity());
            return;
        }

        cart.addItem(itemId, item.getName(), qty, item.getPrice());
        System.out.println("✓ Added to cart!");
        System.out.printf("  Cart Total: $%.2f\n", cart.getTotal());
    }

    // View detailed item information
    private void viewItemDetails() {
        System.out.print("\nEnter Item ID: ");
        String itemId = FastInput.readLine();

        Item item = inventoryController.getItem(itemId);
        if (item == null) {
            System.out.println("✗ Item not found!");
            return;
        }

        System.out.println("\n====================================================================");
        System.out.println("|                      ITEM DETAILS                                |");
        System.out.println("====================================================================");
        System.out.printf("  Item ID:    %s\n", item.getItemId());
        System.out.printf("  Name:       %s\n", item.getName());
        System.out.printf("  Price:      $%.2f\n", item.getPrice());
        System.out.printf("  Stock:      %d units\n", item.getQuantity());

        if (item.getQuantity() > 0) {
            System.out.println("  Status:     ✓ In Stock");
        } else {
            System.out.println("  Status:     ✗ Out of Stock");
        }

        System.out.println("====================================================================");

        if (item.getQuantity() > 0) {
            System.out.print("\nAdd to cart? (y/n): ");
            String confirm = FastInput.readLine();

            if (confirm.equalsIgnoreCase("y")) {
                System.out.print("Enter Quantity: ");
                int qty = FastInput.readInt();

                if (qty > 0 && qty <= item.getQuantity()) {
                    cart.addItem(itemId, item.getName(), qty, item.getPrice());
                    System.out.println("✓ Added to cart!");
                } else {
                    System.out.println("✗ Invalid quantity!");
                }
            }
        }
    }

    // Search items and add to cart
    private void searchAndAdd() {
        System.out.print("\nEnter search term: ");
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

        System.out.print("\nAdd item to cart? (Enter Item ID or press Enter to skip): ");
        String itemId = FastInput.readLine();

        if (!itemId.isEmpty()) {
            Item item = inventoryController.getItem(itemId);
            if (item != null && item.getQuantity() > 0) {
                System.out.print("Enter Quantity: ");
                int qty = FastInput.readInt();

                if (qty > 0 && qty <= item.getQuantity()) {
                    cart.addItem(itemId, item.getName(), qty, item.getPrice());
                    System.out.println("✓ Added to cart!");
                } else {
                    System.out.println("✗ Invalid quantity!");
                }
            } else {
                System.out.println("✗ Item not available!");
            }
        }
    }

    // View current cart
    private void viewCart() {
        System.out.println("\n====================================================================");
        System.out.println("|                       YOUR CART                                  |");
        System.out.println("====================================================================");

        if (cart.isEmpty()) {
            System.out.println("  Your cart is empty.");
            return;
        }

        System.out.println("  Item ID    Item Name            Qty x Price    = Subtotal");
        System.out.println("--------------------------------------------------------------------");

        for (models.store.CartItem item : cart.getItems()) {
            System.out.println("  " + item);
        }

        System.out.println("====================================================================");
        System.out.printf("  TOTAL: $%.2f (%d items)\n", cart.getTotal(), cart.getItemCount());
        System.out.println("====================================================================");
    }

    // Display items with stock indicators
    public void showWithStockIndicators() {
        System.out.println("====================================================================");
        System.out.println("|                   INVENTORY WITH STOCK STATUS                    |");
        System.out.println("====================================================================");

        Item[] items = inventoryController.getAllItems();

        if (items.length == 0) {
            System.out.println("  No items in inventory.");
            return;
        }

        System.out.println("--------------------------------------------------------------------");
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
    }

    // Show items by category (if you want to add categories later)
    public void showByPriceRange(double min, double max) {
        System.out.println("====================================================================");
        System.out.printf("|          ITEMS IN PRICE RANGE: $%.2f - $%.2f           |\n", min, max);
        System.out.println("====================================================================");

        Item[] results = inventoryController.filterByPriceRange(min, max);
        inventoryController.showItems(results);

        System.out.printf("\nFound %d item(s) in this price range\n", results.length);
        System.out.println("====================================================================");
    }
}
