package models.miscellaneous;

public class FoundItem {
    private String id;
    private String name;
    private String description;
    private String locationFound;
    private boolean isClaimed;
    private String claimantId;

    public FoundItem(String id, String name, String description, String locationFound) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.locationFound = locationFound;
        this.isClaimed = false;
        this.claimantId = "None"; // Default value when unclaimed
    }

    // Converts the object to a comma-separated string for easy file saving
    @Override
    public String toString() {
        return id + "," + name + "," + description + "," + locationFound + "," + isClaimed + "," + claimantId;
    }
}