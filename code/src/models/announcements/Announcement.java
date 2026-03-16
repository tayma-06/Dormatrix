package models.announcements;

public class Announcement {

    private final String announcementId;
    private final String authorName;
    private final String title;
    private final String body;
    private final String createdAt;
    private final String expiresAt;  // ← new, empty = never expires

    public Announcement(String announcementId, String authorName, String title,
                        String body, String createdAt, String expiresAt) {
        this.announcementId = announcementId;
        this.authorName = authorName;
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt == null ? "" : expiresAt;
    }

    public String getAnnouncementId() { return announcementId; }
    public String getAuthorName()     { return authorName; }
    public String getTitle()          { return title; }
    public String getBody()           { return body; }
    public String getCreatedAt()      { return createdAt; }
    public String getExpiresAt()      { return expiresAt; }

    public boolean isExpired() {
        if (expiresAt == null || expiresAt.trim().isEmpty()) return false;
        try {
            java.time.LocalDate expiry = java.time.LocalDate.parse(expiresAt.trim());
            return java.time.LocalDate.now().isAfter(expiry);
        } catch (Exception e) {
            return false;
        }
    }
}