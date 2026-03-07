package cli.views.store;

import controllers.store.InventoryController;
import controllers.store.PurchaseController;
import java.util.Scanner;
import utils.TerminalUI;

public class PurchaseView {

    private final InventoryController inventoryController;
    private final PurchaseController purchaseController;
    private final Scanner scanner;

    public PurchaseView() {
        this.inventoryController = new InventoryController();
        this.purchaseController = new PurchaseController(inventoryController);
        this.scanner = new Scanner(System.in);
    }

    public void show() {
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("PURCHASE ITEM");
        TerminalUI.tBoxSep();

        inventoryController.showInventory();

        TerminalUI.tBoxBottom();
        TerminalUI.tEmpty();
        TerminalUI.tPrompt("Enter Student ID: ");
        String studentId = scanner.nextLine();

        TerminalUI.tPrompt("Enter Item ID: ");
        String itemId = scanner.nextLine();

        TerminalUI.tPrompt("Enter Quantity: ");
        int qty = scanner.nextInt();
        scanner.nextLine();

        TerminalUI.tPrompt("Credit purchase? (y/n): ");
        boolean credit = scanner.nextLine().equalsIgnoreCase("y");

        purchaseController.purchase(studentId, itemId, qty, credit);
        TerminalUI.tSuccess("Purchase successful!");
    }
}

