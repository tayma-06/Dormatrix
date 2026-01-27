package models.store;

public class DueRecord {
    public String studentId;
    public double amount;

    public DueRecord(String studentId, double amount) {
        this.studentId = studentId;
        this.amount = amount;
    }

    public String toString() {
        return studentId + "," + amount;
    }

    public static DueRecord fromString(String line) {
        String[] p = line.split(",");
        return new DueRecord(p[0], Double.parseDouble(p[1]));
    }

    public String getStudentId() {
        return studentId;
    }

    public void addAmount(double amount) {
        this.amount += amount;
    }

    public double getAmount() {
        return amount;
    }
}
