package models.store;

public class ShoppingCart {
    private CartItem[] items;
    private int count;

    public ShoppingCart() {
        items = new CartItem[50];
        count = 0;
    }

    public void addItem(String itemId, String name, int quantity, double price) {
        for (int i = 0; i < count; i++) {
            if (items[i].getItemId().equals(itemId)) {
                items[i].setQuantity(items[i].getQuantity() + quantity);
                return;
            }
        }
        items[count++] = new CartItem(itemId, name, quantity, price);
    }

    public void removeItem(String itemId) {
        for (int i = 0; i < count; i++) {
            if (items[i].getItemId().equals(itemId)) {
                for (int j = i; j < count - 1; j++) items[j] = items[j + 1];
                items[--count] = null;
                return;
            }
        }
    }

    public CartItem[] getItems() {
        CartItem[] result = new CartItem[count];
        System.arraycopy(items, 0, result, 0, count);
        return result;
    }

    public double getTotal() {
        double total = 0;
        for (int i = 0; i < count; i++) total += items[i].getSubtotal();
        return total;
    }

    public boolean isEmpty() { return count == 0; }
    public void clear() { items = new CartItem[50]; count = 0; }

    public int getItemCount() {
        return count;
    }
}
