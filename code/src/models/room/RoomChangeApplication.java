package models.room;

import models.enums.RoomChangeApplicationStatus;

public class RoomChangeApplication {

    private final String applicationId;
    private final String studentId;
    private final String studentName;
    private final String currentRoom;
    private final String requestedRoom;
    private final String reason;
    private RoomChangeApplicationStatus status;
    private final String submittedAt;
    private String reviewedBy;
    private String reviewNote;
    private String reviewedAt;

    public RoomChangeApplication(
            String applicationId,
            String studentId,
            String studentName,
            String currentRoom,
            String requestedRoom,
            String reason,
            RoomChangeApplicationStatus status,
            String submittedAt,
            String reviewedBy,
            String reviewNote,
            String reviewedAt
    ) {
        this.applicationId = safe(applicationId);
        this.studentId = safe(studentId);
        this.studentName = safe(studentName);
        this.currentRoom = safe(currentRoom);
        this.requestedRoom = safe(requestedRoom);
        this.reason = safe(reason);
        this.status = status == null ? RoomChangeApplicationStatus.PENDING : status;
        this.submittedAt = safe(submittedAt);
        this.reviewedBy = safe(reviewedBy);
        this.reviewNote = safe(reviewNote);
        this.reviewedAt = safe(reviewedAt);
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    public String getRequestedRoom() {
        return requestedRoom;
    }

    public String getReason() {
        return reason;
    }

    public RoomChangeApplicationStatus getStatus() {
        return status;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public String getReviewedAt() {
        return reviewedAt;
    }

    public void setStatus(RoomChangeApplicationStatus status) {
        this.status = status == null ? RoomChangeApplicationStatus.PENDING : status;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = safe(reviewedBy);
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = safe(reviewNote);
    }

    public void setReviewedAt(String reviewedAt) {
        this.reviewedAt = safe(reviewedAt);
    }

    public String toFileString() {
        return esc(applicationId) + "|"
                + esc(studentId) + "|"
                + esc(studentName) + "|"
                + esc(currentRoom) + "|"
                + esc(requestedRoom) + "|"
                + esc(reason) + "|"
                + status.name() + "|"
                + esc(submittedAt) + "|"
                + esc(reviewedBy) + "|"
                + esc(reviewNote) + "|"
                + esc(reviewedAt);
    }

    public static RoomChangeApplication fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] parts = line.split("\\|", -1);
        if (parts.length < 11) {
            return null;
        }

        try {
            return new RoomChangeApplication(
                    parts[0],
                    parts[1],
                    parts[2],
                    parts[3],
                    parts[4],
                    parts[5],
                    RoomChangeApplicationStatus.valueOf(parts[6]),
                    parts[7],
                    parts[8],
                    parts[9],
                    parts[10]
            );
        } catch (Exception e) {
            return null;
        }
    }

    private static String esc(String s) {
        return safe(s).replace('|', '/');
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}