package cli.views.store;

import controllers.store.InventoryController;
import models.store.Item;
import models.store.ShoppingCart;
import utils.FastInput;
import utils.TerminalUI;

public class InventoryListView {

    private final InventoryController inventoryController;
    private ShoppingCart cart;

    public InventoryListView() {
        this.inventoryController = new InventoryController();
    }

    public InventoryListView(ShoppingCart cart) {
        this.inventoryController = new InventoryController();
        this.cart = cart;
    }

    public void show() {
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("INVENTORY LIST");
        TerminalUI.tBoxSep();
        inventoryController.showInventory();
        TerminalUI.tBoxBottom();
    }

    public void showWithCartOptions() {
        if (cart == null) { show(); return; }

        while (true) {
            TerminalUI.tEmpty();
            TerminalUI.tBoxTop();
            TerminalUI.tBoxTitle("BROWSE STORE INVENTORY");
            TerminalUI.tBoxSep();

            if (!cart.isEmpty()) {
                TerminalUI.tBoxLine(String.format("Cart: %d item(s) | Total: $%.2f", cart.getItemCount(), cart.getTotal()));
                TerminalUI.tBoxSep();
            }

            Item[] items = inventoryController.getAllItems();
            if (items.length == 0) {
                TerminalUI.tBoxLine("No items available for purchase.");
                TerminalUI.tBoxBottom();
                return;
            }
            for (Item item : items) {
                String status = item.getQuantity() == 0 ? "[OUT]" : item.getQuantity() <= 5 ? "[LOW]" : "";
                TerminalUI.tBoxLine(String.format("%-10s %-20s $%8.2f  Qty: %d %s",
                        item.getItemId(), item.getName(), item.getPrice(), item.getQuantity(), status));
            }

            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("[A] Quick Add to Cart");
            TerminalUI.tBoxLine("[V] View Item Details");
            TerminalUI.tBoxLine("[S] Search Items");
            TerminalUI.tBoxLine("[C] View Cart");
            TerminalUI.tBoxLine("[B] Back", utils.ConsoleColors.Accent.EXIT);
            TerminalUI.tBoxBottom();
            TerminalUI.tEmpty();
            TerminalUI.tPrompt("Enter your choice: ");

            String choice = FastInput.readLine().toUpperCase();

            switch (choice) {
                case "A": quickAddToCart(); break;
                case "V": viewItemDetails(); break;
                case "S": searchAndAdd(); break;
                case "C": viewCart(); break;
                case "B": return;
                default: TerminalUI.tError("Invalid choice!");
            }
        }
    }

    private void quickAddToCart() {
        TerminalUI.tPrompt("Enter Item ID to add: ");
        String itemId = FastInput.readLine();
        Item item = InventoryController.getItem(itemId);
        if (item == null) { TerminalUI.tError("Item not found!"); return; }
        if (item.getQuantity() <= 0) { TerminalUI.tError("Item out of stock!"); return; }

        TerminalUI.tBoxLine(String.format("Adding: %s - $%.2f (Available: %d)", item.getName(), item.getPrice(), item.getQuantity()));
        TerminalUI.tPrompt("Enter Quantity: ");
        int qty = FastInput.readInt();
        if (qty <= 0) { TerminalUI.tError("Invalid quantity!"); return; }
        if (qty > item.getQuantity()) { TerminalUI.tError("Insufficient stock! Available: " + item.getQuantity()); return; }

        cart.addItem(itemId, item.getName(), qty, item.getPrice());
        TerminalUI.tSuccess("Added to cart!");
        TerminalUI.tBoxLine(String.format("Cart Total: $%.2f", cart.getTotal()));
    }

    private void viewItemDetails() {
        TerminalUI.tPrompt("Enter Item ID: ");
        String itemId = FastInput.readLine();
        Item item = InventoryController.getItem(itemId);
        if (item == null) { TerminalUI.tError("Item not found!"); return; }

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("ITEM DETAILS");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Item ID:  " + item.getItemId());
        TerminalUI.tBoxLine("Name:     " + item.getName());
        TerminalUI.tBoxLine(String.format("Price:    $%.2f", item.getPrice()));
        TerminalUI.tBoxLine("Stock:    " + item.getQuantity() + " units");
        TerminalUI.tBoxLine("Status:   " + (item.getQuantity() > 0 ? "In Stock" : "Out of Stock"));
        TerminalUI.tBoxBottom();

        if (item.getQuantity() > 0) {
            TerminalUI.tPrompt("Add to cart? (y/n): ");
            String confirm = FastInput.readLine();
            if (confirm.equalsIgnoreCase("y")) {
                TerminalUI.tPrompt("Enter Quantity: ");
                int qty = FastInput.readInt();
                if (qty > 0 && qty <= item.getQuantity()) {
                    cart.addItem(itemId, item.getName(), qty, item.getPrice());
                    TerminalUI.tSuccess("Added to cart!");
                } else {
                    TerminalUI.tError("Invalid quantity!");
                }
            }
        }
    }

    private void searchAndAdd() {
        TerminalUI.tPrompt("Enter search term: ");
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

        if (results.length > 0) {
            TerminalUI.tPrompt("Add item to cart? (Enter Item ID or Enter to skip): ");
            String itemId = FastInput.readLine();
            if (!itemId.isEmpty()) {
                Item item = InventoryController.getItem(itemId);
                if (item != null && item.getQuantity() > 0) {
                    TerminalUI.tPrompt("Enter Quantity: ");
                    int qty = FastInput.readInt();
                    if (qty > 0 && qty <= item.getQuantity()) {
                        cart.addItem(itemId, item.getName(), qty, item.getPrice());
                        TerminalUI.tSuccess("Added to cart!");
                    } else { TerminalUI.tError("Invalid quantity!"); }
                } else { TerminalUI.tError("Item not available!"); }
            }
        }
    }

    private void viewCart() {
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("YOUR CART");
        TerminalUI.tBoxSep();
        if (cart.isEmpty()) {
            TerminalUI.tBoxLine("Your cart is empty.");
        } else {
            for (models.store.CartItem item : cart.getItems()) {
                TerminalUI.tBoxLine(item.toString());
            }
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine(String.format("TOTAL: $%.2f (%d items)", cart.getTotal(), cart.getItemCount()));
        }
        TerminalUI.tBoxBottom();
    }

    public void showWithStockIndicators() {
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("INVENTORY WITH STOCK STATUS");
        TerminalUI.tBoxSep();

        Item[] items = inventoryController.getAllItems();
        if (items.length == 0) {
            TerminalUI.tBoxLine("No items in inventory.");
            TerminalUI.tBoxBottom();
            return;
        }

        TerminalUI.tBoxLine(String.format("%-10s %-20s %10s %8s %6s", "ID", "Name", "Price", "Stock", "Status"));
        TerminalUI.tBoxSep();

        for (Item item : items) {
            String status = item.getQuantity() == 0 ? "OUT" : item.getQuantity() <= 5 ? "LOW" : "OK";
            TerminalUI.tBoxLine(String.format("%-10s %-20s $%8.2f %8d %6s",
                    item.getItemId(), item.getName(), item.getPrice(), item.getQuantity(), status));
        }

        TerminalUI.tBoxBottom();
    }

    // Show items by category (if you want to add categories later)
    public void showByPriceRange(double min, double max) {
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle(String.format("ITEMS IN PRICE RANGE: $%.2f - $%.2f", min, max));
        TerminalUI.tBoxSep();
        Item[] results = inventoryController.filterByPriceRange(min, max);
        if (results.length == 0) {
            TerminalUI.tBoxLine("No items found in this price range.");
        } else {
            for (Item item : results) {
                TerminalUI.tBoxLine(String.format("%-10s %-20s $%8.2f Qty: %d",
                        item.getItemId(), item.getName(), item.getPrice(), item.getQuantity()));
            }
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("Found " + results.length + " item(s) in this price range");
        }
        TerminalUI.tBoxBottom();
    }
}
