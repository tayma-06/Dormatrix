package cli.views.store;

import controllers.store.InventoryController;
import controllers.store.PurchaseController;
import utils.FastInput;
import utils.TerminalUI;

public class PurchaseView {

    private final InventoryController inventoryController;
    private final PurchaseController purchaseController;

    public PurchaseView() {
        this.inventoryController = new InventoryController();
        this.purchaseController = new PurchaseController(inventoryController);
    }

    public void show() {
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("PURCHASE ITEM");
        TerminalUI.tBoxSep();

        inventoryController.showInventory();

        TerminalUI.tBoxBottom();
        TerminalUI.tEmpty();
        TerminalUI.tPrompt("Enter Student ID: ");
        String studentId = FastInput.readLine();

        TerminalUI.tPrompt("Enter Item ID: ");
        String itemId = FastInput.readLine();

        TerminalUI.tPrompt("Enter Quantity: ");
        int qty = FastInput.readInt();

        TerminalUI.tPrompt("Credit purchase? (y/n): ");
        boolean credit = FastInput.readLine().equalsIgnoreCase("y");

        purchaseController.purchase(studentId, itemId, qty, credit);
        TerminalUI.tSuccess("Purchase successful!");
    }
}
