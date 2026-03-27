package cli.views.store;

import controllers.store.InventoryController;
import models.store.CartItem;
import models.store.Item;
import models.store.ShoppingCart;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import static utils.TerminalUIExtras.tArrowSelect;

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
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("INVENTORY LIST");
        TerminalUI.tBoxSep();

        Item[] items = inventoryController.getAllItems();
        if (items.length == 0) {
            TerminalUI.tBoxLine("No items available.");
        } else {
            for (Item item : items) {
                String status = item.getQuantity() == 0 ? "[OUT]" : item.getQuantity() <= 5 ? "[LOW]" : "";
                TerminalUI.tBoxLine(String.format("%-10s %-20s BDT %8.2f  Qty: %d %s",
                        item.getItemId(), item.getName(), item.getPrice(), item.getQuantity(), status));
            }
        }

        TerminalUI.tBoxBottom();
    }

    public void showWithCartOptions() {
        if (cart == null) {
            show();
            return;
        }

        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);

            Item[] items = inventoryController.getAllItems();
            int availableCount = 0;
            for (Item item : items) {
                if (item.getQuantity() > 0) {
                    availableCount++;
                }
            }

            TerminalUI.tBoxTop();
            TerminalUI.tBoxTitle("STORE BROWSING");
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("Available Items: " + availableCount + " | Total Listed: " + items.length);
            if (!cart.isEmpty()) {
                TerminalUI.tBoxLine(String.format("Cart: %d item(s) | Total: BDT %.2f",
                        cart.getItemCount(), cart.getTotal()));
            } else {
                TerminalUI.tBoxLine("Cart is empty");
            }
            TerminalUI.tBoxBottom();

            int choice;
            try {
                choice = tArrowSelect("STORE OPTIONS", new String[]{
                        "View Inventory List",
                        "Quick Add to Cart",
                        "View Item Details",
                        "Search Items",
                        "View Cart",
                        "Back"
                }, false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            switch (choice) {
                case 0 -> {
                    show();
                    TerminalUI.tPause();
                }
                case 1 -> quickAddToCart();
                case 2 -> viewItemDetails();
                case 3 -> searchAndAdd();
                case 4 -> viewCart();
                default -> {
                    return;
                }
            }
        }
    }

    private void quickAddToCart() {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tInfoBox("QUICK ADD TO CART", "Enter an item ID to add it directly from the inventory.");
        TerminalUI.tPrompt("Enter Item ID to add: ");
        String itemId = FastInput.readLine().trim();

        if (itemId.isEmpty()) {
            TerminalUI.tError("Item ID cannot be empty!");
            TerminalUI.tPause();
            return;
        }

        Item item = InventoryController.getItem(itemId);
        if (item == null) {
            TerminalUI.tError("Item not found!");
            TerminalUI.tPause();
            return;
        }
        if (item.getQuantity() <= 0) {
            TerminalUI.tError("Item out of stock!");
            TerminalUI.tPause();
            return;
        }

        int qty = promptForQuantity(item, "QUICK ADD TO CART");
        if (qty < 0) {
            return;
        }

        cart.addItem(itemId, item.getName(), qty, item.getPrice());

        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tInfoBox("ITEM ADDED",
                String.format("Added %d x %s", qty, item.getName()),
                String.format("Unit Price: BDT %.2f", item.getPrice()),
                String.format("Cart Total: BDT %.2f", cart.getTotal()));
        TerminalUI.tPause();
    }

    private void viewItemDetails() {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tInfoBox("VIEW ITEM DETAILS", "Enter an item ID to see its full details.");
        TerminalUI.tPrompt("Enter Item ID: ");
        String itemId = FastInput.readLine().trim();

        Item item = InventoryController.getItem(itemId);
        if (item == null) {
            TerminalUI.tError("Item not found!");
            TerminalUI.tPause();
            return;
        }

        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tInfoBox("ITEM DETAILS",
                "Item ID: " + item.getItemId(),
                "Name: " + item.getName(),
                String.format("Price: BDT %.2f", item.getPrice()),
                "Stock: " + item.getQuantity() + " units",
                "Status: " + (item.getQuantity() > 0 ? "In Stock" : "Out of Stock"));

        if (item.getQuantity() <= 0 || cart == null) {
            TerminalUI.tPause();
            return;
        }

        TerminalUI.tPrompt("Add to cart? (y/n): ");
        String confirm = FastInput.readLine().trim();
        if (!confirm.equalsIgnoreCase("y")) {
            return;
        }

        int qty = promptForQuantity(item, "ITEM DETAILS");
        if (qty < 0) {
            return;
        }

        cart.addItem(itemId, item.getName(), qty, item.getPrice());
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tInfoBox("ITEM ADDED",
                String.format("Added %d x %s", qty, item.getName()),
                String.format("Cart Total: BDT %.2f", cart.getTotal()));
        TerminalUI.tPause();
    }

    private void searchAndAdd() {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tInfoBox("SEARCH ITEMS", "Type a search term to find matching items.");
        TerminalUI.tPrompt("Enter search term: ");
        String searchTerm = FastInput.readLine().trim();
        Item[] results = inventoryController.searchByName(searchTerm);

        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("SEARCH RESULTS");
        TerminalUI.tBoxSep();
        if (results.length == 0) {
            TerminalUI.tBoxLine("No items found matching '" + searchTerm + "'");
            TerminalUI.tBoxBottom();
            TerminalUI.tPause();
            return;
        }

        for (Item item : results) {
            TerminalUI.tBoxLine(String.format("%-10s %-20s BDT %8.2f  Qty: %d",
                    item.getItemId(), item.getName(), item.getPrice(), item.getQuantity()));
        }
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Found " + results.length + " item(s)");
        TerminalUI.tBoxBottom();

        TerminalUI.tPrompt("Add item to cart? (Enter Item ID or Enter to skip): ");
        String itemId = FastInput.readLine().trim();
        if (itemId.isEmpty()) {
            return;
        }

        Item item = InventoryController.getItem(itemId);
        if (item == null || item.getQuantity() <= 0) {
            TerminalUI.tError("Item not available!");
            TerminalUI.tPause();
            return;
        }

        int qty = promptForQuantity(item, "SEARCH RESULTS");
        if (qty < 0) {
            return;
        }

        cart.addItem(itemId, item.getName(), qty, item.getPrice());
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tInfoBox("ITEM ADDED",
                String.format("Added %d x %s", qty, item.getName()),
                String.format("Cart Total: BDT %.2f", cart.getTotal()));
        TerminalUI.tPause();
    }

    private int promptForQuantity(Item item, String title) {
        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);
            TerminalUI.tInfoBox(title,
                    "Selected Item:",
                    "ID: " + item.getItemId(),
                    "Name: " + item.getName(),
                    String.format("Price: BDT %.2f", item.getPrice()),
                    "Available Stock: " + item.getQuantity());
            TerminalUI.tPrompt("Enter Quantity: ");
            String rawQty = FastInput.readLine().trim();

            int qty;
            try {
                qty = Integer.parseInt(rawQty);
            } catch (NumberFormatException e) {
                TerminalUI.tError("Please enter a valid quantity.");
                TerminalUI.tPause();
                continue;
            }

            if (qty <= 0) {
                TerminalUI.tError("Quantity must be greater than zero.");
                TerminalUI.tPause();
                continue;
            }
            if (qty > item.getQuantity()) {
                TerminalUI.tError("Insufficient stock! Available: " + item.getQuantity());
                TerminalUI.tPause();
                continue;
            }
            return qty;
        }
    }

    private void viewCart() {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("YOUR CART");
        TerminalUI.tBoxSep();

        if (cart.isEmpty()) {
            TerminalUI.tBoxLine("Your cart is empty.");
        } else {
            for (CartItem item : cart.getItems()) {
                TerminalUI.tBoxLine(String.format("%-10s %-20s %5d x BDT %6.2f = BDT %8.2f",
                        item.getItemId(), item.getItemName(), item.getQuantity(),
                        item.getUnitPrice(), item.getSubtotal()));
            }
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine(String.format("TOTAL: BDT %.2f (%d items)",
                    cart.getTotal(), cart.getItemCount()));
        }

        TerminalUI.tBoxBottom();
        TerminalUI.tPause();
    }

    public void showWithStockIndicators() {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("INVENTORY WITH STOCK STATUS");
        TerminalUI.tBoxSep();

        Item[] items = inventoryController.getAllItems();
        if (items.length == 0) {
            TerminalUI.tBoxLine("No items in inventory.");
            TerminalUI.tBoxBottom();
            return;
        }

        TerminalUI.tBoxLine(String.format("%-10s %-20s %14s %8s %6s", "ID", "Name", "Price", "Stock", "Status"));
        TerminalUI.tBoxSep();

        for (Item item : items) {
            String status = item.getQuantity() == 0 ? "OUT" : item.getQuantity() <= 5 ? "LOW" : "OK";
            TerminalUI.tBoxLine(String.format("%-10s %-20s BDT %8.2f %8d %6s",
                    item.getItemId(), item.getName(), item.getPrice(), item.getQuantity(), status));
        }

        TerminalUI.tBoxBottom();
    }

    public void showByPriceRange(double min, double max) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle(String.format("ITEMS IN PRICE RANGE: BDT %.2f - BDT %.2f", min, max));
        TerminalUI.tBoxSep();

        Item[] results = inventoryController.filterByPriceRange(min, max);
        if (results.length == 0) {
            TerminalUI.tBoxLine("No items found in this price range.");
        } else {
            for (Item item : results) {
                TerminalUI.tBoxLine(String.format("%-10s %-20s BDT %8.2f Qty: %d",
                        item.getItemId(), item.getName(), item.getPrice(), item.getQuantity()));
            }
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("Found " + results.length + " item(s) in this price range");
        }

        TerminalUI.tBoxBottom();
    }
}