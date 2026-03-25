package models.miscellaneous;

public class FoundItem {
    private String id;
    private String lostItemId; // Links directly to the LostItem
    private boolean isClaimed;
    private String claimantId;

    public FoundItem(String id, String lostItemId) {
        this.id = id;
        this.lostItemId = lostItemId;
        this.isClaimed = false;
        this.claimantId = "None";
    }

    @Override
    public String toString() {
        // New shorter format: id, lostItemId, isClaimed, claimantId
        return id + "," + lostItemId + "," + isClaimed + "," + claimantId;
    }
}