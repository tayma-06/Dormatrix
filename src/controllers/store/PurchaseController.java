package controllers.store;

import controllers.balance.BalanceController;
import models.store.Item;
import models.store.CartItem;

public class PurchaseController {
    private final InventoryController inventoryController;
    private final BalanceController balanceController;
    private final DueController dueController;
    private final SalesController salesController;

    public PurchaseController(InventoryController inventoryController) {
        this.inventoryController = inventoryController;
        this.balanceController = new BalanceController();
        this.dueController = new DueController();
        this.salesController = new SalesController();
    }

    /**
     * Process a single item purchase
     */
    public boolean purchase(String studentId, String itemId, int qty, boolean useCredit) {
        // Validate inputs
        if (studentId == null || itemId == null || qty <= 0) {
            System.out.println("✗ Invalid purchase data");
            return false;
        }

        // Get item details
        Item item = inventoryController.getItem(itemId);
        if (item == null) {
            System.out.println("✗ Item not found: " + itemId);
            return false;
        }

        // Check stock availability
        if (item.getQuantity() < qty) {
            System.out.println("✗ Insufficient stock! Available: " + item.getQuantity() + ", Requested: " + qty);
            return false;
        }

        // Calculate total price
        double totalPrice = item.getPrice() * qty;

        // Handle payment
        if (!useCredit) {
            // Pay with balance
            double balance = balanceController.getBalance(studentId);
            if (balance < totalPrice) {
                System.out.printf("✗ Insufficient balance! Required: $%.2f, Available: $%.2f\n",
                        totalPrice, balance);
                return false;
            }

            if (!balanceController.deductBalance(studentId, totalPrice)) {
                System.out.println("✗ Failed to deduct balance");
                return false;
            }
        } else {
            // Add to dues
            dueController.addDue(studentId, totalPrice);
        }

        // Update inventory
        inventoryController.reduceQuantity(itemId, qty);

        // Record the sale
        if (!salesController.recordSale(studentId, itemId, qty, totalPrice)) {
            System.out.println("⚠ Warning: Sale completed but not recorded in history");
        }

        System.out.printf("✓ Purchase successful! Item: %s, Qty: %d, Total: $%.2f\n",
                item.getName(), qty, totalPrice);

        if (useCredit) {
            System.out.printf("  Added to dues: $%.2f\n", totalPrice);
        } else {
            double newBalance = balanceController.getBalance(studentId);
            System.out.printf("  Remaining balance: $%.2f\n", newBalance);
        }

        return true;
    }

    /**
     * Process shopping cart purchase (multiple items)
     */
    public boolean purchaseCart(String studentId, CartItem[] cartItems, boolean useCredit) {
        if (studentId == null || cartItems == null || cartItems.length == 0) {
            System.out.println("✗ Cart is empty");
            return false;
        }

        // Calculate total and validate stock
        double grandTotal = 0.0;
        for (CartItem cartItem : cartItems) {
            Item item = inventoryController.getItem(cartItem.getItemId());
            if (item == null) {
                System.out.println("✗ Item not found: " + cartItem.getItemId());
                return false;
            }

            if (item.getQuantity() < cartItem.getQuantity()) {
                System.out.printf("✗ Insufficient stock for %s! Available: %d, Requested: %d\n",
                        item.getName(), item.getQuantity(), cartItem.getQuantity());
                return false;
            }

            grandTotal += cartItem.getSubtotal();
        }

        // Check payment
        if (!useCredit) {
            double balance = balanceController.getBalance(studentId);
            if (balance < grandTotal) {
                System.out.printf("✗ Insufficient balance! Required: $%.2f, Available: $%.2f\n",
                        grandTotal, balance);
                return false;
            }
        }

        // Process payment
        if (!useCredit) {
            if (!balanceController.deductBalance(studentId, grandTotal)) {
                System.out.println("✗ Failed to deduct balance");
                return false;
            }
        } else {
            dueController.addDue(studentId, grandTotal);
        }

        // Update inventory and record sales
        for (CartItem cartItem : cartItems) {
            inventoryController.reduceQuantity(cartItem.getItemId(), cartItem.getQuantity());
            salesController.recordSale(studentId, cartItem.getItemId(),
                    cartItem.getQuantity(), cartItem.getSubtotal());
        }

        System.out.println("\n====================================================================");
        System.out.println("|                   PURCHASE COMPLETED                             |");
        System.out.println("====================================================================");
        System.out.printf("  Total Items:  %d\n", cartItems.length);
        System.out.printf("  Total Amount: $%.2f\n", grandTotal);

        if (useCredit) {
            double totalDue = dueController.getDue(studentId);
            System.out.printf("  Payment:      On Credit\n");
            System.out.printf("  Total Dues:   $%.2f\n", totalDue);
        } else {
            double newBalance = balanceController.getBalance(studentId);
            System.out.printf("  Payment:      Cash (Balance)\n");
            System.out.printf("  Remaining:    $%.2f\n", newBalance);
        }
        System.out.println("====================================================================");

        return true;
    }

    /**
     * Get student's current balance
     */
    public double getBalance(String studentId) {
        return balanceController.getBalance(studentId);
    }

    /**
     * Get student's current dues
     */
    public double getDues(String studentId) {
        return dueController.getDue(studentId);
    }
}