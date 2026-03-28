package models.food;

import utils.TimeManager;

import java.time.LocalDate;
import java.time.LocalTime;

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
        if (status == TokenStatus.ACTIVE && shouldBeExpiredNow()) {
            return TokenStatus.EXPIRED;
        }
        return status;
    }

    public boolean refreshStatus() {
        if (status == TokenStatus.ACTIVE && shouldBeExpiredNow()) {
            status = TokenStatus.EXPIRED;
            return true;
        }
        return false;
    }

    private boolean shouldBeExpiredNow() {
        LocalDate today = TimeManager.nowDate();

        if (date.isBefore(today)) {
            return true;
        }

        if (!date.equals(today)) {
            return false;
        }

        if (type == MealType.NONE) {
            return false;
        }

        LocalTime cutoff = TimeManager.getMealEndTime(type);
        return TimeManager.nowTime().isAfter(cutoff);
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