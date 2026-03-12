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

    public void reduceQuantity(int amount) { this.quantity -= amount; }

    public String toFileString() {
        return itemId + "," + name + "," + quantity + "," + price;
    }

    public static Item fromString(String line) {
        if (line == null || line.isEmpty()) return null;
        String[] p = line.split(",");
        if (p.length < 4) return null;
        try {
            return new Item(p[0], p[1], Integer.parseInt(p[2]), Double.parseDouble(p[3]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void setQuantity(int newQuantity) {
        this.quantity = newQuantity;
    }

    public void setPrice(double newPrice) {
        this.price = newPrice;
    }
}

