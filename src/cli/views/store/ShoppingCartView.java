package cli.views.store;

import controllers.store.InventoryController;
import controllers.store.PurchaseController;
import controllers.balance.BalanceController;
import controllers.store.DueController;
import models.store.Item;
import models.store.ShoppingCart;
import models.store.CartItem;
import utils.ConsoleUtil;
import utils.FastInput;

public class ShoppingCartView {

    private final InventoryController inventoryController;
    private final PurchaseController purchaseController;
    private final BalanceController balanceController;
    private static DueController dueController = null;

    public ShoppingCartView() {
        this.inventoryController = new InventoryController();
        this.purchaseController = new PurchaseController(inventoryController);
        this.balanceController = new BalanceController();
        this.dueController = new DueController();
    }

    public static void show(String studentId) {
        ShoppingCart cart = new ShoppingCart();

        while (true) {
            ConsoleUtil.clearScreen();
            displayHeader(studentId);
            displayCart(cart);
            displayMenu();

            String choice = FastInput.readLine().toUpperCase();

            switch (choice) {
                case "1":
                    browseAndAddItems(cart);
                    break;
                case "2":
                    addItemToCart(cart);
                    break;
                case "3":
                    removeItemFromCart(cart);
                    break;
                case "4":
                    viewCartDetails(cart);
                    break;
                case "5":
                    processCheckout(studentId, cart);
                    break;
                case "6":
                    cart.clear();
                    System.out.println("✓ Cart cleared!");
                    break;
                case "0":
                    ConsoleUtil.clearScreen();
                    return;
                default:
                    System.out.println("✗ Invalid choice!");
            }

            if (!choice.equals("0")) {
                System.out.println("\nPress Enter to continue...");
                FastInput.readLine();
            }
        }
    }

    private static void displayHeader(String studentId) {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("|                         SHOPPING CART                               |");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.printf("  Student ID: %s\n", studentId);

        double balance = BalanceController.getBalance(studentId);
        double dues = DueController.getDue(studentId);

        System.out.printf("  Balance:    $%.2f", balance);
        if (dues > 0) {
            System.out.printf(" | Dues: $%.2f\n", dues);
        } else {
            System.out.println();
        }
        System.out.println("═══════════════════════════════════════════════════════════════════════");
    }

    private static void displayCart(ShoppingCart cart) {
        if (cart.isEmpty()) {
            System.out.println("  🛒 Your cart is empty");
        } else {
            System.out.printf("  🛒 Cart: %d item(s) | Total: $%.2f\n",
                    cart.getItemCount(), cart.getTotal());
        }
        System.out.println("═══════════════════════════════════════════════════════════════════════");
    }

    private static void displayMenu() {
        System.out.println("\nOptions:");
        System.out.println("  [1] Browse Inventory");
        System.out.println("  [2] Add Item to Cart");
        System.out.println("  [3] Remove Item from Cart");
        System.out.println("  [4] View Cart Details");
        System.out.println("  [5] Checkout");
        System.out.println("  [6] Clear Cart");
        System.out.println("  [0] Back");
        System.out.print("\nEnter your choice: ");
    }

    private static void browseAndAddItems(ShoppingCart cart) {
        InventoryListView inventoryView = new InventoryListView(cart);
        inventoryView.showWithCartOptions();
    }

    private static void addItemToCart(ShoppingCart cart) {
        System.out.println("\n--- Add Item to Cart ---");

        System.out.print("Enter Item ID: ");
        String itemId = FastInput.readLine();

        Item item = InventoryController.getItem(itemId);
        if (item == null) {
            System.out.println("✗ Item not found!");
            return;
        }

        if (item.getQuantity() <= 0) {
            System.out.println("✗ Item out of stock!");
            return;
        }

        System.out.printf("Item: %s - $%.2f (Available: %d)\n",
                item.getName(), item.getPrice(), item.getQuantity());

        System.out.print("Enter Quantity: ");
        int qty = FastInput.readInt();

        if (qty <= 0) {
            System.out.println("✗ Invalid quantity!");
            return;
        }

        if (qty > item.getQuantity()) {
            System.out.println("✗ Insufficient stock! Available: " + item.getQuantity());
            return;
        }

        cart.addItem(itemId, item.getName(), qty, item.getPrice());
        System.out.println("✓ Added to cart!");
        System.out.printf("  Cart Total: $%.2f\n", cart.getTotal());
    }

    private static void removeItemFromCart(ShoppingCart cart) {
        if (cart.isEmpty()) {
            System.out.println("✗ Cart is empty!");
            return;
        }

        System.out.println("\n--- Remove Item from Cart ---");
        viewCartDetails(cart);

        System.out.print("\nEnter Item ID to remove: ");
        String itemId = FastInput.readLine();

        cart.removeItem(itemId);
        System.out.println("✓ Item removed from cart!");
    }

    private static void viewCartDetails(ShoppingCart cart) {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("|                          CART DETAILS                               |");
        System.out.println("═══════════════════════════════════════════════════════════════════════");

        if (cart.isEmpty()) {
            System.out.println("  Your cart is empty.");
            System.out.println("═══════════════════════════════════════════════════════════════════════");
            return;
        }

        System.out.println("  Item ID    Item Name            Qty x Price    = Subtotal");
        System.out.println("═══════════════════════════════════════════════════════════════════════");

        for (CartItem item : cart.getItems()) {
            System.out.println("  " + item);
        }

        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.printf("  TOTAL: $%.2f (%d items)\n", cart.getTotal(), cart.getItemCount());
        System.out.println("═══════════════════════════════════════════════════════════════════════");
    }

    private static void processCheckout(String studentId, ShoppingCart cart) {
        if (cart.isEmpty()) {
            System.out.println("✗ Cart is empty! Add items before checkout.");
            return;
        }

        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("|                            CHECKOUT                                 |");
        System.out.println("═══════════════════════════════════════════════════════════════════════");

        // Display cart summary
        System.out.println("  Item ID    Item Name            Qty x Price    = Subtotal");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        for (CartItem item : cart.getItems()) {
            System.out.println("  " + item);
        }
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.printf("  TOTAL AMOUNT: $%.2f\n", cart.getTotal());
        System.out.println("═══════════════════════════════════════════════════════════════════════");

        // Display payment info
        double balance = BalanceController.getBalance(studentId);
        double currentDues = dueController.getDue(studentId);

        System.out.printf("\n  Your Balance: $%.2f\n", balance);
        if (currentDues > 0) {
            System.out.printf("  Current Dues: $%.2f\n", currentDues);
        }

        // Payment method selection
        System.out.println("\nPayment Method:");
        System.out.println("  [1] Pay with Balance");
        System.out.println("  [2] Buy on Credit (Add to Dues)");
        System.out.println("  [0] Cancel");
        System.out.print("\nChoose payment method: ");

        String paymentChoice = FastInput.readLine();

        boolean useCredit;
        switch (paymentChoice) {
            case "1":
                useCredit = false;
                if (balance < cart.getTotal()) {
                    System.out.printf("\n✗ Insufficient balance! You need $%.2f more.\n",
                            cart.getTotal() - balance);
                    System.out.println("  Please add balance or choose credit option.");
                    return;
                }
                break;
            case "2":
                useCredit = true;
                System.out.println("\n⚠ This will be added to your dues.");
                System.out.print("Confirm? (y/n): ");
                String confirm = FastInput.readLine();
                if (!confirm.equalsIgnoreCase("y")) {
                    System.out.println("✗ Checkout cancelled.");
                    return;
                }
                break;
            case "0":
                System.out.println("✗ Checkout cancelled.");
                return;
            default:
                System.out.println("✗ Invalid payment method!");
                return;
        }

        // Process the purchase
        boolean success = PurchaseController.purchaseCart(studentId, cart.getItems(), useCredit);

        if (success) {
            cart.clear();
            System.out.println("\n✓ Thank you for your purchase!");
        } else {
            System.out.println("\n✗ Purchase failed! Please try again.");
        }
    }
}
