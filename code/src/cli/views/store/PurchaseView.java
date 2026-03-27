package cli.views.store;

import controllers.store.InventoryController;
import controllers.store.PurchaseController;
import models.store.Item;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import static utils.TerminalUIExtras.tArrowSelect;

public class PurchaseView {

    private final InventoryController inventoryController;
    private final PurchaseController purchaseController;

    public PurchaseView() {
        this.inventoryController = new InventoryController();
        this.purchaseController = new PurchaseController(inventoryController);
    }

    public void show() {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("PROCESS PURCHASE");
        TerminalUI.tBoxSep();

        Item[] items = inventoryController.getAllItems();
        if (items.length == 0) {
            TerminalUI.tBoxLine("No items available.");
            TerminalUI.tBoxBottom();
            return;
        }

        for (Item item : items) {
            TerminalUI.tBoxLine(String.format("%-10s %-20s BDT %8.2f  Qty: %d",
                    item.getItemId(), item.getName(), item.getPrice(), item.getQuantity()));
        }

        TerminalUI.tBoxBottom();
        TerminalUI.tPrompt("Enter Student ID: ");
        String studentId = FastInput.readLine();

        TerminalUI.tPrompt("Enter Item ID: ");
        String itemId = FastInput.readLine();

        TerminalUI.tPrompt("Enter Quantity: ");
        int qty = FastInput.readInt();

        int choice;
        try {
            choice = tArrowSelect("PURCHASE TYPE", new String[]{
                    "Normal Purchase",
                    "Credit Purchase",
                    "Back"
            }, false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (choice == 2 || choice < 0) {
            return;
        }

        boolean credit = choice == 1;
        purchaseController.purchase(studentId, itemId, qty, credit);
        TerminalUI.tPause();
    }
}