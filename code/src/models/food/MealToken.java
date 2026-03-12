package models.food;

import utils.TimeManager;
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

    public TokenStatus getStatus() {
        LocalDate today = TimeManager.nowDate();
        if (status == TokenStatus.ACTIVE && date.isBefore(today)) {
            return TokenStatus.EXPIRED;
        }
        return status;
    }

    public void setStatus(TokenStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return tokenId + "|" + studentId + "|" + type + "|" + date + "|" + status;
    }

    public static MealToken fromString(String line) {
        String[] parts = line.split("\\|");
        return new MealToken(
                parts[0],
                parts[1],
                MealType.valueOf(parts[2]),
                LocalDate.parse(parts[3]),
                TokenStatus.valueOf(parts[4])
        );
    }

    public String getTokenId() { return tokenId; }
    public LocalDate getDate() { return date; }
    public MealType getType() { return type; }
    public String getStudentId() { return studentId; }

    public boolean isUsed() {
        return getStatus() == TokenStatus.USED;
    }
}
