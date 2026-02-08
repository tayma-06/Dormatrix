package models.food;

import java.time.LocalDate;

public class MealToken {
    private String tokenId;
    private String studentId;
    private MealType type;
    private LocalDate date;
    private TokenStatus status;

    public MealToken(String tokenId, String studentId, MealType type, LocalDate date, TokenStatus status) {
        this.tokenId = tokenId;
        this.studentId = studentId;
        this.type = type;
        this.date = date;
        this.status = status;
    }

    // Logic to auto-expire tokens if the date has passed
    public TokenStatus getStatus() {
        if (status == TokenStatus.ACTIVE && date.isBefore(LocalDate.now())) {
            return TokenStatus.EXPIRED;
        }
        return status;
    }

    public void setStatus(TokenStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        // We save the Status name (ACTIVE/USED/EXPIRED) instead of a boolean
        return tokenId + "|" + studentId + "|" + type + "|" + date + "|" + status;
    }

    public static MealToken fromString(String line) {
        String[] parts = line.split("\\|");
        return new MealToken(
                parts[0],
                parts[1],
                MealType.valueOf(parts[2]),
                LocalDate.parse(parts[3]),
                TokenStatus.valueOf(parts[4]) // Parses ACTIVE, USED, or EXPIRED
        );
    }

    // Getters
    public String getTokenId() { return tokenId; }
    public LocalDate getDate() { return date; }
    public MealType getType() { return type; }
    public String getStudentId() { return studentId; }

    // Compatibility helper for your controller
    public boolean isUsed() {
        return this.status != TokenStatus.ACTIVE;
    }
}