package cli.views;

import controllers.store.InventoryController;
import controllers.store.PurchaseController;
import exceptions.InsufficientInventoryException;

import java.util.Scanner;

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
        System.out.println("--------------------------------------------");
        System.out.println("|              Purchase Item               |");
        System.out.println("--------------------------------------------");

        inventoryController.showInventory();

        System.out.print("\nEnter Student ID: ");
        String studentId = scanner.nextLine();

        System.out.print("Enter Item ID: ");
        String itemId = scanner.nextLine();

        System.out.print("Enter Quantity: ");
        int qty = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Credit purchase? (y/n): ");
        boolean credit = scanner.nextLine().equalsIgnoreCase("y");

        try {
            purchaseController.purchase(studentId, itemId, qty, credit);
            System.out.println("Purchase successful!");
        } catch (InsufficientInventoryException e) {
            System.out.println("Purchase failed: " + e.getMessage());
        }
    }
}

