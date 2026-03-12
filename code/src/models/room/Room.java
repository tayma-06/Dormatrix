package models.room;

public class Room {
    private String roomId;
    private int capacity;
    private int currentOccupancy;

    public Room(String roomId, int capacity, int currentOccupancy) {
        this.roomId = roomId;
        this.capacity = capacity;
        this.currentOccupancy = currentOccupancy;
    }

    public String getRoomId() { return roomId; }
    public int getCapacity() { return capacity; }
    public int getCurrentOccupancy() { return currentOccupancy; }

    public void setCurrentOccupancy(int currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }

    public boolean isAvailable() {
        return currentOccupancy < capacity;
    }

    public void incrementOccupancy() {
        if (currentOccupancy < capacity) currentOccupancy++;
    }

    public void decrementOccupancy() {
        if (currentOccupancy > 0) currentOccupancy--;
    }

    public String toFileString() {
        return roomId + "|" + capacity + "|" + currentOccupancy;
    }

    public static Room fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 3) return null;
        return new Room(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    @Override
    public String toString() {
        return "Room " + roomId + " [" + currentOccupancy + "/" + capacity + "] "
                + (isAvailable() ? "AVAILABLE" : "FULL");
    }
}
