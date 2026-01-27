package cli.views;

import controllers.store.InventoryController;

public class InventoryListView {

    private final InventoryController inventoryController;

    public InventoryListView() {
        this.inventoryController = new InventoryController();
    }

    public void show() {
        System.out.println("--------------------------------------------");
        System.out.println("|            Inventory List                |");
        System.out.println("--------------------------------------------");

        inventoryController.showInventory();

        System.out.println("--------------------------------------------");
    }
}
