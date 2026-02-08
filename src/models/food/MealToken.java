package models.food;

import java.time.LocalDate;

public class MealToken {
    private String tokenId;
    private String studentId;
    private MealType type;
    private LocalDate date;
    private boolean isUsed;

    public MealToken(String tokenId, String studentId, MealType type, LocalDate date, boolean isUsed) {
        this.tokenId = tokenId;
        this.studentId = studentId;
        this.type = type;
        this.date = date;
        this.isUsed = isUsed;
    }

    @Override
    public String toString() {
        return tokenId + "|" + studentId + "|" + type + "|" + date + "|" + isUsed;
    }

    public static MealToken fromString(String line) {
        String[] parts = line.split("\\|");
        return new MealToken(parts[0], parts[1], MealType.valueOf(parts[2]),
                LocalDate.parse(parts[3]), Boolean.parseBoolean(parts[4]));
    }

    public String getTokenId() { return tokenId; }
    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }
    public LocalDate getDate() { return date; }

    public MealType getType() {
        return type;
    }

    public Object getStudentId() {
        return studentId;
    }
}