package models.store;

public class ShoppingCart {
    private CartItem[] items;
    private int count;

    public ShoppingCart() {
        this.items = new CartItem[50];
        this.count = 0;
    }

    public void addItem(String itemId, String itemName, int quantity, double unitPrice) {
        // Check if item already exists in cart
        for (int i = 0; i < count; i++) {
            if (items[i].getItemId().equals(itemId)) {
                items[i].setQuantity(items[i].getQuantity() + quantity);
                return;
            }
        }
        // Add new item
        items[count++] = new CartItem(itemId, itemName, quantity, unitPrice);
    }

    public void removeItem(String itemId) {
        for (int i = 0; i < count; i++) {
            if (items[i].getItemId().equals(itemId)) {
                // Shift items left
                for (int j = i; j < count - 1; j++) {
                    items[j] = items[j + 1];
                }
                items[--count] = null;
                return;
            }
        }
    }

    public void updateQuantity(String itemId, int newQuantity) {
        for (int i = 0; i < count; i++) {
            if (items[i].getItemId().equals(itemId)) {
                if (newQuantity <= 0) {
                    removeItem(itemId);
                } else {
                    items[i].setQuantity(newQuantity);
                }
                return;
            }
        }
    }

    public CartItem[] getItems() {
        CartItem[] result = new CartItem[count];
        System.arraycopy(items, 0, result, 0, count);
        return result;
    }

    public int getItemCount() {
        return count;
    }

    public double getTotal() {
        double total = 0;
        for (int i = 0; i < count; i++) {
            total += items[i].getSubtotal();
        }
        return total;
    }

    public void clear() {
        items = new CartItem[50];
        count = 0;
    }

    public boolean isEmpty() {
        return count == 0;
    }
}