package controllers.store;

import controllers.balance.BalanceController;
import models.store.Item;
import models.store.CartItem;
import utils.TerminalUI;

public class PurchaseController {
    private static final SalesController salesController = new SalesController();

    public PurchaseController(InventoryController inventoryController) {
    }

    /**
     * Process a single item purchase
     */
    public boolean purchase(String studentId, String itemId, int qty, boolean useCredit) {
        // Validate inputs
        if (studentId == null || itemId == null || qty <= 0) {
            TerminalUI.tError("Invalid purchase data");
            return false;
        }

        // Get item details
        Item item = InventoryController.getItem(itemId);
        if (item == null) {
            TerminalUI.tError("Item not found: " + itemId);
            return false;
        }

        // Check stock availability
        if (item.getQuantity() < qty) {
            TerminalUI.tError("Insufficient stock! Available: " + item.getQuantity() + ", Requested: " + qty);
            return false;
        }

        // Calculate total price
        double totalPrice = item.getPrice() * qty;

        // Handle payment
        if (!useCredit) {
            // Pay with balance
            double balance = BalanceController.getBalance(studentId);
            if (balance < totalPrice) {
                TerminalUI.tError(String.format("Insufficient balance! Required: $%.2f, Available: $%.2f",
                        totalPrice, balance));
                return false;
            }

            if (!BalanceController.deductBalance(studentId, totalPrice)) {
                TerminalUI.tError("Failed to deduct balance");
                return false;
            }
        } else {
            // Add to dues
            DueController.addDue(studentId, totalPrice);
        }

        // Update inventory
        InventoryController.reduceQuantity(itemId, qty);

        // Record the sale
        if (!salesController.recordSale(studentId, itemId, qty, totalPrice)) {
            TerminalUI.tPrint("Warning: Sale completed but not recorded in history");
        }

        TerminalUI.tSuccess(String.format("Purchase successful! Item: %s, Qty: %d, Total: $%.2f",
                item.getName(), qty, totalPrice));

        if (useCredit) {
            TerminalUI.tBoxTop();
            TerminalUI.tBoxLine(String.format("Added to dues: $%.2f", totalPrice));
            TerminalUI.tBoxBottom();
        } else {
            double newBalance = BalanceController.getBalance(studentId);
            TerminalUI.tBoxTop();
            TerminalUI.tBoxLine(String.format("Remaining balance: $%.2f", newBalance));
            TerminalUI.tBoxBottom();
        }

        return true;
    }

    /**
     * Process shopping cart purchase (multiple items)
     */
    public static boolean purchaseCart(String studentId, CartItem[] cartItems, boolean useCredit) {
        if (studentId == null || cartItems == null || cartItems.length == 0) {
            TerminalUI.tError("Cart is empty");
            return false;
        }

        // Calculate total and validate stock
        double grandTotal = 0.0;
        for (CartItem cartItem : cartItems) {
            Item item = InventoryController.getItem(cartItem.getItemId());
            if (item == null) {
                TerminalUI.tError("Item not found: " + cartItem.getItemId());
                return false;
            }

            if (item.getQuantity() < cartItem.getQuantity()) {
                TerminalUI.tError(String.format("Insufficient stock for %s! Available: %d, Requested: %d",
                        item.getName(), item.getQuantity(), cartItem.getQuantity()));
                return false;
            }

            grandTotal += cartItem.getSubtotal();
        }

        // Check payment
        if (!useCredit) {
            double balance = BalanceController.getBalance(studentId);
            if (balance < grandTotal) {
                TerminalUI.tError(String.format("Insufficient balance! Required: $%.2f, Available: $%.2f",
                        grandTotal, balance));
                return false;
            }
        }

        // Process payment
        if (!useCredit) {
            if (!BalanceController.deductBalance(studentId, grandTotal)) {
                TerminalUI.tError("Failed to deduct balance");
                return false;
            }
        } else {
            DueController.addDue(studentId, grandTotal);
        }

        // Update inventory and record sales
        for (CartItem cartItem : cartItems) {
            InventoryController.reduceQuantity(cartItem.getItemId(), cartItem.getQuantity());

            // Record each sale
            if (!salesController.recordSale(studentId, cartItem.getItemId(),
                    cartItem.getQuantity(), cartItem.getSubtotal())) {
                TerminalUI.tPrint("Warning: Failed to record sale for item: " + cartItem.getItemId());
            }
        }

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("PURCHASE COMPLETED");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Total Items:  " + cartItems.length);
        TerminalUI.tBoxLine(String.format("Total Amount: $%.2f", grandTotal));

        if (useCredit) {
            double totalDue = DueController.getDue(studentId);
            TerminalUI.tBoxLine("Payment:      On Credit");
            TerminalUI.tBoxLine(String.format("Total Dues:   $%.2f", totalDue));
        } else {
            double newBalance = BalanceController.getBalance(studentId);
            TerminalUI.tBoxLine("Payment:      Cash (Balance)");
            TerminalUI.tBoxLine(String.format("Remaining:    $%.2f", newBalance));
        }
        TerminalUI.tBoxBottom();

        return true;
    }

    /**
     * Get student's current balance
     */
    public double getBalance(String studentId) {
        return BalanceController.getBalance(studentId);
    }

    /**
     * Get student's current dues
     */
    public double getDues(String studentId) {
        return DueController.getDue(studentId);
    }
}