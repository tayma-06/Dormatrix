package cli.views.store;

import controllers.balance.BalanceController;
import controllers.store.DueController;
import controllers.store.InventoryController;
import controllers.store.PurchaseController;
import models.store.CartItem;
import models.store.Item;
import models.store.ShoppingCart;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import static utils.TerminalUIExtras.tArrowSelect;

public class ShoppingCartView {

    public ShoppingCartView() {
    }

    public static void show(String studentId) {
        ShoppingCart cart = new ShoppingCart();

        while (true) {
            renderCartHome(studentId, cart);

            int choice;
            try {
                choice = tArrowSelect("CART ACTIONS", new String[]{
                        "Browse Inventory",
                        "Add Item to Cart",
                        "Remove Item from Cart",
                        "View Cart Details",
                        "Checkout",
                        "Clear Cart",
                        "Back"
                }, false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            switch (choice) {
                case 0 -> browseAndAddItems(cart);
                case 1 -> addItemToCart(cart);
                case 2 -> removeItemFromCart(cart);
                case 3 -> viewCartDetails(cart, true);
                case 4 -> processCheckout(studentId, cart);
                case 5 -> clearCart(cart);
                default -> {
                    ConsoleUtil.clearScreen();
                    return;
                }
            }
        }
    }

    private static void renderCartHome(String studentId, ShoppingCart cart) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);

        double balance = BalanceController.getBalance(studentId);
        double dues = DueController.getDue(studentId);

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("SHOPPING CART");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Student ID: " + studentId);
        String balInfo = String.format("Balance: BDT %.2f", balance);
        if (dues > 0) {
            balInfo += String.format(" | Dues: BDT %.2f", dues);
        }
        TerminalUI.tBoxLine(balInfo);
        TerminalUI.tBoxSep();

        if (cart.isEmpty()) {
            TerminalUI.tBoxLine("Cart is empty");
        } else {
            TerminalUI.tBoxLine(String.format("Cart: %d item(s) | Total: BDT %.2f", cart.getItemCount(), cart.getTotal()));
        }

        TerminalUI.tBoxBottom();
    }

    private static void browseAndAddItems(ShoppingCart cart) {
        InventoryListView inventoryView = new InventoryListView(cart);
        inventoryView.showWithCartOptions();
    }

    private static void addItemToCart(ShoppingCart cart) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tInfoBox("ADD ITEM TO CART", "Enter an item ID below to add it directly to the cart.");
        TerminalUI.tPrompt("Enter Item ID: ");
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

        int qty = promptForQuantity(item, "ADD ITEM TO CART");
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

    private static int promptForQuantity(Item item, String title) {
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

    private static void removeItemFromCart(ShoppingCart cart) {
        if (cart.isEmpty()) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);
            TerminalUI.tError("Cart is empty!");
            TerminalUI.tPause();
            return;
        }

        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        renderCartItemsBox("REMOVE ITEM", cart);
        TerminalUI.tPrompt("Enter Item ID to remove: ");
        String itemId = FastInput.readLine().trim();

        CartItem target = findCartItem(cart, itemId);
        if (target == null) {
            TerminalUI.tError("That item is not in the cart.");
            TerminalUI.tPause();
            return;
        }

        cart.removeItem(itemId);

        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tInfoBox("ITEM REMOVED",
                "Removed: " + target.getItemName(),
                String.format("Remaining Cart Total: BDT %.2f", cart.getTotal()));
        TerminalUI.tPause();
    }

    private static void viewCartDetails(ShoppingCart cart, boolean pauseAfter) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        renderCartItemsBox("CART DETAILS", cart);
        if (pauseAfter) {
            TerminalUI.tPause();
        }
    }

    private static void renderCartItemsBox(String title, ShoppingCart cart) {
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle(title);
        TerminalUI.tBoxSep();

        if (cart.isEmpty()) {
            TerminalUI.tBoxLine("Your cart is empty.");
        } else {
            for (CartItem item : cart.getItems()) {
                TerminalUI.tBoxLine(String.format("%-10s %-20s %5d x BDT %6.2f = BDT %8.2f",
                        item.getItemId(), item.getItemName(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal()));
            }
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine(String.format("TOTAL: BDT %.2f (%d items)", cart.getTotal(), cart.getItemCount()));
        }

        TerminalUI.tBoxBottom();
    }

    private static void processCheckout(String studentId, ShoppingCart cart) {
        if (cart.isEmpty()) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);
            TerminalUI.tError("Cart is empty! Add items before checkout.");
            TerminalUI.tPause();
            return;
        }

        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);
            renderCartItemsBox("CHECKOUT", cart);

            double balance = BalanceController.getBalance(studentId);
            double currentDues = DueController.getDue(studentId);
            TerminalUI.tInfoBox("PAYMENT SUMMARY",
                    String.format("Your Balance: BDT %.2f", balance),
                    currentDues > 0 ? String.format("Current Dues: BDT %.2f", currentDues) : "Current Dues: BDT 0.00",
                    String.format("Amount Payable: BDT %.2f", cart.getTotal()));

            int paymentChoice;
            try {
                paymentChoice = tArrowSelect("PAYMENT METHOD", new String[]{
                        "Pay with Balance",
                        "Buy on Credit (Add to Dues)",
                        "Cancel"
                }, false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            boolean useCredit;
            switch (paymentChoice) {
                case 0 -> {
                    useCredit = false;
                    if (balance < cart.getTotal()) {
                        TerminalUI.tError(String.format("Insufficient balance! Need BDT %.2f more.", cart.getTotal() - balance));
                        TerminalUI.tPause();
                        continue;
                    }
                }
                case 1 -> {
                    useCredit = true;
                    if (!confirmCreditPurchase(cart.getTotal())) {
                        return;
                    }
                }
                default -> {
                    return;
                }
            }

            boolean success = PurchaseController.purchaseCart(studentId, cart.getItems(), useCredit);
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);

            if (success) {
                cart.clear();
                TerminalUI.tInfoBox("CHECKOUT COMPLETE",
                        "Thank you for your purchase!",
                        useCredit ? "This amount has been added to your dues." : "The amount was paid from your balance.");
            } else {
                TerminalUI.tError("Purchase failed! Please try again.");
            }
            TerminalUI.tPause();
            return;
        }
    }

    private static boolean confirmCreditPurchase(double total) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tInfoBox("CREDIT PURCHASE",
                String.format("This purchase will add BDT %.2f to your dues.", total),
                "Type y to confirm or n to cancel.");
        TerminalUI.tPrompt("Confirm credit purchase? (y/n): ");
        String confirm = FastInput.readLine().trim();
        return confirm.equalsIgnoreCase("y");
    }

    private static void clearCart(ShoppingCart cart) {
        cart.clear();
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
        TerminalUI.tInfoBox("CART CLEARED", "All items have been removed from the cart.");
        TerminalUI.tPause();
    }

    private static CartItem findCartItem(ShoppingCart cart, String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        for (CartItem item : cart.getItems()) {
            if (item.getItemId().equalsIgnoreCase(itemId.trim())) {
                return item;
            }
        }
        return null;
    }
}