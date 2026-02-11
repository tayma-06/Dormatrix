package models.store;

public class CartItem {
    private String itemId;
    private String itemName;
    private int quantity;
    private double unitPrice;

    public CartItem(String itemId, String itemName, int quantity, double unitPrice) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getSubtotal() { return quantity * unitPrice; }

    @Override
    public String toString() {
        return String.format("%-10s %-20s %5d x $%6.2f = $%8.2f",
                itemId, itemName, quantity, unitPrice, getSubtotal());
    }
}
