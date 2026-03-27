package controllers.store;

import controllers.balance.BalanceController;
import models.store.CartItem;
import models.store.Item;
import utils.TerminalUI;

public class PurchaseController {
    private static final SalesController salesController = new SalesController();

    public PurchaseController(InventoryController inventoryController) {
    }

    public boolean purchase(String studentId, String itemId, int qty, boolean useCredit) {
        if (studentId == null || itemId == null || qty <= 0) {
            TerminalUI.tError("Invalid purchase data");
            return false;
        }

        Item item = InventoryController.getItem(itemId);
        if (item == null) {
            TerminalUI.tError("Item not found: " + itemId);
            return false;
        }

        if (item.getQuantity() < qty) {
            TerminalUI.tError("Insufficient stock! Available: " + item.getQuantity() + ", Requested: " + qty);
            return false;
        }

        double totalPrice = item.getPrice() * qty;

        if (!useCredit) {
            double balance = BalanceController.getBalance(studentId);
            if (balance < totalPrice) {
                TerminalUI.tError(String.format("Insufficient balance! Required: BDT %.2f, Available: BDT %.2f",
                        totalPrice, balance));
                return false;
            }

            if (!BalanceController.deductBalance(studentId, totalPrice)) {
                TerminalUI.tError("Failed to deduct balance");
                return false;
            }
        } else {
            DueController.addDue(studentId, totalPrice);
        }

        InventoryController.reduceQuantity(itemId, qty);

        if (!salesController.recordSale(studentId, itemId, qty, totalPrice)) {
            TerminalUI.tPrint("Warning: Sale completed but not recorded in history");
        }

        TerminalUI.tSuccess(String.format("Purchase successful! Item: %s, Qty: %d, Total: BDT %.2f",
                item.getName(), qty, totalPrice));

        if (useCredit) {
            TerminalUI.tBoxTop();
            TerminalUI.tBoxLine(String.format("Added to dues: BDT %.2f", totalPrice));
            TerminalUI.tBoxBottom();
        } else {
            double newBalance = BalanceController.getBalance(studentId);
            TerminalUI.tBoxTop();
            TerminalUI.tBoxLine(String.format("Remaining balance: BDT %.2f", newBalance));
            TerminalUI.tBoxBottom();
        }

        return true;
    }

    public static boolean purchaseCart(String studentId, CartItem[] cartItems, boolean useCredit) {
        if (studentId == null || cartItems == null || cartItems.length == 0) {
            TerminalUI.tError("Cart is empty");
            return false;
        }

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

        if (!useCredit) {
            double balance = BalanceController.getBalance(studentId);
            if (balance < grandTotal) {
                TerminalUI.tError(String.format("Insufficient balance! Required: BDT %.2f, Available: BDT %.2f",
                        grandTotal, balance));
                return false;
            }
        }

        if (!useCredit) {
            if (!BalanceController.deductBalance(studentId, grandTotal)) {
                TerminalUI.tError("Failed to deduct balance");
                return false;
            }
        } else {
            DueController.addDue(studentId, grandTotal);
        }

        for (CartItem cartItem : cartItems) {
            InventoryController.reduceQuantity(cartItem.getItemId(), cartItem.getQuantity());

            if (!salesController.recordSale(studentId, cartItem.getItemId(),
                    cartItem.getQuantity(), cartItem.getSubtotal())) {
                TerminalUI.tPrint("Warning: Failed to record sale for item: " + cartItem.getItemId());
            }
        }

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("PURCHASE COMPLETED");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Total Items:  " + cartItems.length);
        TerminalUI.tBoxLine(String.format("Total Amount: BDT %.2f", grandTotal));

        if (useCredit) {
            double totalDue = DueController.getDue(studentId);
            TerminalUI.tBoxLine("Payment:      On Credit");
            TerminalUI.tBoxLine(String.format("Total Dues:   BDT %.2f", totalDue));
        } else {
            double newBalance = BalanceController.getBalance(studentId);
            TerminalUI.tBoxLine("Payment:      Cash (Balance)");
            TerminalUI.tBoxLine(String.format("Remaining:    BDT %.2f", newBalance));
        }
        TerminalUI.tBoxBottom();

        return true;
    }

    public double getBalance(String studentId) {
        return BalanceController.getBalance(studentId);
    }

    public double getDues(String studentId) {
        return DueController.getDue(studentId);
    }
}