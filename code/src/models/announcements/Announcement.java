package models.announcements;

public class Announcement {

    private final String announcementId;
    private final String authorName;
    private final String title;
    private final String body;
    private final String createdAt;

    public Announcement(String announcementId, String authorName, String title, String body, String createdAt) {
        this.announcementId = announcementId;
        this.authorName = authorName;
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
    }

    public String getAnnouncementId() { return announcementId; }
    public String getAuthorName() { return authorName; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getCreatedAt() { return createdAt; }
}