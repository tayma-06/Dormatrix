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

public class ShoppingCartView {

    public ShoppingCartView() {
    }

    public static void show(String studentId) {
        ShoppingCart cart = new ShoppingCart();

        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);

            // Header with balance info
            double balance = BalanceController.getBalance(studentId);
            double dues = DueController.getDue(studentId);

            TerminalUI.tBoxTop();
            TerminalUI.tBoxTitle("SHOPPING CART");
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("Student ID: " + studentId);
            String balInfo = String.format("Balance: $%.2f", balance);
            if (dues > 0) balInfo += String.format(" | Dues: $%.2f", dues);
            TerminalUI.tBoxLine(balInfo);
            TerminalUI.tBoxSep();

            if (cart.isEmpty()) {
                TerminalUI.tBoxLine("Cart is empty");
            } else {
                TerminalUI.tBoxLine(String.format("Cart: %d item(s) | Total: $%.2f", cart.getItemCount(), cart.getTotal()));
            }

            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("[1] Browse Inventory");
            TerminalUI.tBoxLine("[2] Add Item to Cart");
            TerminalUI.tBoxLine("[3] Remove Item from Cart");
            TerminalUI.tBoxLine("[4] View Cart Details");
            TerminalUI.tBoxLine("[5] Checkout");
            TerminalUI.tBoxLine("[6] Clear Cart");
            TerminalUI.tBoxLine("[0] Back", utils.ConsoleColors.Accent.EXIT);
            TerminalUI.tBoxBottom();
            TerminalUI.tEmpty();
            TerminalUI.tPrompt("Enter your choice: ");

            String choice = FastInput.readLine().toUpperCase();

            switch (choice) {
                case "1": browseAndAddItems(cart); break;
                case "2": addItemToCart(cart); break;
                case "3": removeItemFromCart(cart); break;
                case "4": viewCartDetails(cart); break;
                case "5": processCheckout(studentId, cart); break;
                case "6":
                    cart.clear();
                    TerminalUI.tSuccess("Cart cleared!");
                    break;
                case "0": ConsoleUtil.clearScreen(); return;
                default: TerminalUI.tError("Invalid choice!");
            }

            if (!choice.equals("0")) {
                TerminalUI.tPause();
            }
        }
    }

    private static void browseAndAddItems(ShoppingCart cart) {
        InventoryListView inventoryView = new InventoryListView(cart);
        inventoryView.showWithCartOptions();
    }

    private static void addItemToCart(ShoppingCart cart) {
        TerminalUI.tEmpty();
        TerminalUI.tPrompt("Enter Item ID: ");
        String itemId = FastInput.readLine();

        Item item = InventoryController.getItem(itemId);
        if (item == null) { TerminalUI.tError("Item not found!"); return; }
        if (item.getQuantity() <= 0) { TerminalUI.tError("Item out of stock!"); return; }

        TerminalUI.tBoxLine(String.format("Item: %s - $%.2f (Available: %d)", item.getName(), item.getPrice(), item.getQuantity()));
        TerminalUI.tPrompt("Enter Quantity: ");
        int qty = FastInput.readInt();

        if (qty <= 0) { TerminalUI.tError("Invalid quantity!"); return; }
        if (qty > item.getQuantity()) { TerminalUI.tError("Insufficient stock! Available: " + item.getQuantity()); return; }

        cart.addItem(itemId, item.getName(), qty, item.getPrice());
        TerminalUI.tSuccess("Added to cart!");
        TerminalUI.tBoxLine(String.format("Cart Total: $%.2f", cart.getTotal()));
    }

    private static void removeItemFromCart(ShoppingCart cart) {
        if (cart.isEmpty()) { TerminalUI.tError("Cart is empty!"); return; }
        viewCartDetails(cart);
        TerminalUI.tPrompt("Enter Item ID to remove: ");
        String itemId = FastInput.readLine();
        cart.removeItem(itemId);
        TerminalUI.tSuccess("Item removed from cart!");
    }

    private static void viewCartDetails(ShoppingCart cart) {
        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("CART DETAILS");
        TerminalUI.tBoxSep();

        if (cart.isEmpty()) {
            TerminalUI.tBoxLine("Your cart is empty.");
        } else {
            for (CartItem item : cart.getItems()) {
                TerminalUI.tBoxLine(item.toString());
            }
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine(String.format("TOTAL: $%.2f (%d items)", cart.getTotal(), cart.getItemCount()));
        }
        TerminalUI.tBoxBottom();
    }

    private static void processCheckout(String studentId, ShoppingCart cart) {
        if (cart.isEmpty()) { TerminalUI.tError("Cart is empty! Add items before checkout."); return; }

        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("CHECKOUT");
        TerminalUI.tBoxSep();
        for (CartItem item : cart.getItems()) {
            TerminalUI.tBoxLine(item.toString());
        }
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine(String.format("TOTAL: $%.2f", cart.getTotal()));
        TerminalUI.tBoxBottom();

        double balance = BalanceController.getBalance(studentId);
        double currentDues = DueController.getDue(studentId);
        TerminalUI.tBoxLine(String.format("Your Balance: $%.2f", balance));
        if (currentDues > 0) TerminalUI.tBoxLine(String.format("Current Dues: $%.2f", currentDues));

        TerminalUI.tEmpty();
        TerminalUI.tSubDashboard("PAYMENT METHOD", new String[]{
            "[1] Pay with Balance",
            "[2] Buy on Credit (Add to Dues)",
            "[0] Cancel"
        });

        String paymentChoice = FastInput.readLine();
        boolean useCredit;
        switch (paymentChoice) {
            case "1":
                useCredit = false;
                if (balance < cart.getTotal()) {
                    TerminalUI.tError(String.format("Insufficient balance! Need $%.2f more.", cart.getTotal() - balance));
                    return;
                }
                break;
            case "2":
                useCredit = true;
                TerminalUI.tPrompt("This will be added to dues. Confirm? (y/n): ");
                String confirm = FastInput.readLine();
                if (!confirm.equalsIgnoreCase("y")) { TerminalUI.tPrint("Checkout cancelled."); return; }
                break;
            case "0": TerminalUI.tPrint("Checkout cancelled."); return;
            default: TerminalUI.tError("Invalid payment method!"); return;
        }

        boolean success = PurchaseController.purchaseCart(studentId, cart.getItems(), useCredit);
        if (success) {
            cart.clear();
            TerminalUI.tSuccess("Thank you for your purchase!");
        } else {
            TerminalUI.tError("Purchase failed! Please try again.");
        }
    }
}
