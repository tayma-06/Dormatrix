package controllers.store;

import controllers.balance.BalanceController;

public class PurchaseController {
    private final InventoryController inventoryController;
    private final BalanceController balanceController = new BalanceController();
    private final DueController dueController = new DueController();

    public PurchaseController(InventoryController inventoryController) {
        this.inventoryController = inventoryController;
    }

    public void purchase(String studentId, String itemId, int qty, boolean useCredit) {
        if (!useCredit) {
            double balance = balanceController.getBalance(studentId);
            double price = inventoryController.getItem(itemId).getPrice() * qty;
            if (balance < price) {
                System.out.println("Insufficient balance to buy " + itemId);
                return;
            }
            balanceController.deductBalance(studentId, price);
        } else {
            double price = inventoryController.getItem(itemId).getPrice() * qty;
            dueController.addDue(studentId, price);
        }
        inventoryController.reduceQuantity(itemId, qty);
    }
}
