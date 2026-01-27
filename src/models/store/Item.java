package models.store;

public class Item {

    private String itemId;
    private String name;
    private int quantity;
    private double price;

    public Item(String itemId, String name, int quantity, double price) {
        this.itemId = itemId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getItemId() { return itemId; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    public void reduceQuantity(int amount) {
        this.quantity -= amount;
    }

    @Override
    public String toString() {
        return itemId + "," + name + "," + quantity + "," + price;
    }

    public static Item fromString(String line) {
        String[] p = line.split(",");
        return new Item(p[0], p[1], Integer.parseInt(p[2]), Double.parseDouble(p[3]));
    }
}

