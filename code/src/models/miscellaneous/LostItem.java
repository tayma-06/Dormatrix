package models.miscellaneous;

public class LostItem {
    private String id;
    private String name;
    private String description;
    private String reporterId;
    private String date;

    public LostItem(String id, String name, String description, String reporterId, String date) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.reporterId = reporterId;
        this.date = date;
    }

    // Converts the object to a comma-separated string for easy file saving
    @Override
    public String toString() {
        return id + "," + name + "," + description + "," + reporterId + "," + date;
    }
}