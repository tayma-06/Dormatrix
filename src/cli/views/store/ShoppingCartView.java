package cli.views.store;

import controllers.store.InventoryController;
import controllers.store.PurchaseController;
import controllers.store.BalanceController;
import exceptions.InsufficientInventoryException;
import models.store.ShoppingCart;
import models.store.CartItem;
import models.store.Item;
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

            // Show current balance
            double balance = balanceController.getBalance(studentId);
            System.out.printf("  Your Balance: $%.2f\n", balance);
            System.out.println("--------------------------------------------------------------------");

            System.out.println("\n1. Browse Items");
            System.out.println("2. Add Item to Cart");
            System.out.println("3. View Cart");
            System.out.println("4. Remove Item from Cart");
            System.out.println("5. Checkout");
            System.out.println("6. Search Items");
            System.out.println("0. Back");
            System.out.print("\nEnter your choice: ");

            int choice = FastInput.readInt();

            switch (choice) {
                case 1:
                    browseItems();
                    break;
                case 2:
                    addToCart();
                    break;
                case 3:
                    viewCart();
                    break;
                case 4:
                    removeFromCart();
                    break;
                case 5:
                    checkout(studentId);
                    break;
                case 6:
                    searchItems();
                    break;
                case 0:
                    cart.clear();
                    return;
                default:
                    System.out.println("Invalid choice!");
            }

            System.out.println("\nPress Enter to continue...");
            FastInput.readLine();
        }
    }

    private void browseItems() {
        System.out.println("\n====================================================================");
        System.out.println("|                      AVAILABLE ITEMS                             |");
        System.out.println("====================================================================");
        inventoryController.showInventory();
    }

    private void addToCart() {
        System.out.println("\n--------------------------------------------------------------------");
        System.out.println("|                    ADD ITEM TO CART                              |");
        System.out.println("--------------------------------------------------------------------");

        inventoryController.showInventory();

        System.out.print("\nEnter Item ID: ");
        String itemId = FastInput.readLine();

        Item item = inventoryController.getItem(itemId);
        if (item == null) {
            System.out.println("Error: Item not found!");
            return;
        }

        System.out.printf("Item: %s - $%.2f (Available: %d)\n",
                item.getName(), item.getPrice(), item.getQuantity());

        System.out.print("Enter Quantity: ");
        int qty = FastInput.readInt();

        if (qty <= 0) {
            System.out.println("Error: Invalid quantity!");
            return;
        }

        if (qty > item.getQuantity()) {
            System.out.println("Error: Insufficient stock! Available: " + item.getQuantity());
            return;
        }

        cart.addItem(itemId, item.getName(), qty, item.getPrice());
        System.out.println("✓ Item added to cart!");
    }

    private void viewCart() {
        System.out.println("\n====================================================================");
        System.out.println("|                       YOUR CART                                  |");
        System.out.println("====================================================================");

        if (cart.isEmpty()) {
            System.out.println("  Your cart is empty.");
            return;
        }

        System.out.println("  Item ID    Item Name            Qty x Price    = Subtotal");
        System.out.println("--------------------------------------------------------------------");

        CartItem[] items = cart.getItems();
        for (CartItem item : items) {
            System.out.println("  " + item);
        }

        System.out.println("====================================================================");
        System.out.printf("  TOTAL: $%.2f\n", cart.getTotal());
        System.out.println("====================================================================");
    }

    private void removeFromCart() {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty!");
            return;
        }

        viewCart();

        System.out.print("\nEnter Item ID to remove: ");
        String itemId = FastInput.readLine();

        cart.removeItem(itemId);
        System.out.println("✓ Item removed from cart!");
    }

    private void checkout(String studentId) {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty!");
            return;
        }

        viewCart();

        System.out.println("\n--------------------------------------------------------------------");
        System.out.println("|                        CHECKOUT                                  |");
        System.out.println("--------------------------------------------------------------------");

        double total = cart.getTotal();
        double balance = balanceController.getBalance(studentId);

        System.out.printf("  Total Amount: $%.2f\n", total);
        System.out.printf("  Your Balance: $%.2f\n", balance);
        System.out.println();

        System.out.println("Payment Options:");
        System.out.println("1. Pay from Balance");
        System.out.println("2. Buy on Credit (Add to Dues)");
        System.out.println("0. Cancel");
        System.out.print("\nEnter choice: ");

        int choice = FastInput.readInt();

        if (choice == 0) {
            System.out.println("Checkout cancelled.");
            return;
        }

        boolean useCredit = (choice == 2);

        if (!useCredit && balance < total) {
            System.out.println("\n✗ Insufficient balance! Please add money to your account or use credit.");
            return;
        }

        // Process each item in cart
        try {
            CartItem[] items = cart.getItems();
            for (CartItem cartItem : items) {
                purchaseController.purchase(
                        studentId,
                        cartItem.getItemId(),
                        cartItem.getQuantity(),
                        useCredit
                );
            }

            // Deduct from balance if not using credit
            if (!useCredit) {
                balanceController.deductBalance(studentId, total);
            }

            System.out.println("\n====================================================================");
            System.out.println("  ✓ PURCHASE SUCCESSFUL!");
            System.out.println("====================================================================");
            System.out.printf("  Total Paid: $%.2f\n", total);
            if (useCredit) {
                System.out.println("  Payment Method: Credit (Added to dues)");
            } else {
                System.out.println("  Payment Method: Balance");
                System.out.printf("  Remaining Balance: $%.2f\n", balance - total);
            }
            System.out.println("====================================================================");

            cart.clear();

        } catch (InsufficientInventoryException e) {
            System.out.println("\n✗ Purchase failed: " + e.getMessage());
            System.out.println("Please update your cart and try again.");
        }
    }

    private void searchItems() {
        System.out.println("\n--------------------------------------------------------------------");
        System.out.println("|                      SEARCH ITEMS                                |");
        System.out.println("--------------------------------------------------------------------");

        System.out.print("Enter search term: ");
        String searchTerm = FastInput.readLine();

        Item[] results = inventoryController.searchByName(searchTerm);

        System.out.println("\n====================================================================");
        System.out.println("|                      SEARCH RESULTS                              |");
        System.out.println("====================================================================");
        inventoryController.showItems(results);
    }
}