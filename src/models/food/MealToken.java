package models.food;

import java.time.LocalDate;

public class MealToken {
    private String tokenId;
    private String studentId;
    private LocalDate date;
    private TokenStatus status;

    public MealToken(String tokenId, String studentId) {
        this.tokenId = tokenId;
        this.studentId = studentId;
        this.date = LocalDate.now();
        this.status = TokenStatus.ACTIVE;
    }

    public String getTokenID(){
        return tokenId;
    }


    public boolean isExpired() {
        return !date.equals(LocalDate.now());
    }

    public void expire() {
        status = TokenStatus.EXPIRED;
    }

    @Override
    public String toString() {
        return tokenId + "," + studentId + "," + date + "," + status;
    }

    public static MealToken fromString(String line) {
        String[] p = line.split(",");
        MealToken t = new MealToken(p[0], p[1]);
        t.date = LocalDate.parse(p[2]);
        t.status = TokenStatus.valueOf(p[3]);
        return t;
    }

    public String getTokenId() {
        return tokenId;
    }
}

