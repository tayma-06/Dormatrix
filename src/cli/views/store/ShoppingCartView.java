package cli.views.store;

import controllers.balance.BalanceController;
import controllers.store.InventoryController;
import controllers.store.PurchaseController;
import models.store.CartItem;
import models.store.Item;
import models.store.ShoppingCart;
import utils.FastInput;

public class ShoppingCartView {

    private final InventoryController inventoryController;
    private final PurchaseController purchaseController;
    private final BalanceController balanceController;
    private final ShoppingCart cart;

    public ShoppingCartView() {
        this.inventoryController = new InventoryController();
        this.purchaseController = new PurchaseController(inventoryController);
        this.balanceController = new BalanceController();
        this.cart = new ShoppingCart();
    }

    public void show(String studentId) {
        while (true) {
            System.out.println("\n====================================================================");
            System.out.println("|                       SHOPPING CART                              |");
            System.out.println("====================================================================");

            System.out.printf("Your Balance: $%.2f\n", balanceController.getBalance(studentId));
            System.out.println("--------------------------------------------------------------------");
            System.out.println("1. Browse Items");
            System.out.println("2. Add Item to Cart");
            System.out.println("3. View Cart");
            System.out.println("4. Remove Item from Cart");
            System.out.println("5. Checkout");
            System.out.println("0. Back");
            System.out.print("\nEnter your choice: ");

            int choice = FastInput.readInt();

            switch (choice) {
                case 1 -> browseItems();
                case 2 -> addToCart();
                case 3 -> viewCart();
                case 4 -> removeFromCart();
                case 5 -> checkout(studentId);
                case 0 -> { cart.clear(); return; }
                default -> System.out.println("Invalid choice!");
            }

            System.out.println("\nPress Enter to continue...");
            FastInput.readLine();
        }
    }

    private void browseItems() {
        System.out.println("\nAvailable Items:");
        inventoryController.showInventory();
    }

    private void addToCart() {
        browseItems();
        System.out.print("Enter Item ID: ");
        String id = FastInput.readLine();
        Item item = inventoryController.getItem(id);
        if (item == null) { System.out.println("Item not found!"); return; }

        System.out.printf("Enter quantity (Available: %d): ", item.getQuantity());
        int qty = FastInput.readInt();
        if (qty <= 0 || qty > item.getQuantity()) { System.out.println("Invalid quantity!"); return; }

        cart.addItem(id, item.getName(), qty, item.getPrice());
        System.out.println("✓ Item added to cart!");
    }

    private void viewCart() {
        if (cart.isEmpty()) { System.out.println("Your cart is empty."); return; }

        System.out.println("\nYour Cart:");
        System.out.println("Item ID  Name                 Qty x Price = Subtotal");
        System.out.println("----------------------------------------------------");
        for (CartItem ci : cart.getItems()) System.out.println(ci);
        System.out.printf("TOTAL: $%.2f\n", cart.getTotal());
    }

    private void removeFromCart() {
        if (cart.isEmpty()) { System.out.println("Cart is empty!"); return; }
        viewCart();
        System.out.print("Enter Item ID to remove: ");
        String id = FastInput.readLine();
        cart.removeItem(id);
        System.out.println("✓ Item removed!");
    }

    private void checkout(String studentId) {
        if (cart.isEmpty()) { System.out.println("Cart is empty!"); return; }
        double total = cart.getTotal();
        double balance = balanceController.getBalance(studentId);

        System.out.printf("Total: $%.2f, Your Balance: $%.2f\n", total, balance);
        System.out.println("1. Pay from Balance");
        System.out.println("2. Buy on Credit (Add to Dues)");
        System.out.println("0. Cancel");
        System.out.print("Choice: ");
        int choice = FastInput.readInt();

        if (choice == 0) return;

        boolean useCredit = choice == 2;

        if (!useCredit && balance < total) {
            System.out.println("Insufficient balance!");
            return;
        }

        for (CartItem ci : cart.getItems()) {
            purchaseController.purchase(studentId, ci.getItemId(), ci.getQuantity(), useCredit);
        }

        if (!useCredit) balanceController.deductBalance(studentId, total);

        System.out.println("✓ Purchase successful!");
        cart.clear();
    }
}
