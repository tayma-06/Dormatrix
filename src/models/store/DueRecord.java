package models.store;

public class DueRecord {
    private String studentId;
    private double amount;

    public DueRecord(String studentId, double amount) {
        this.studentId = studentId;
        this.amount = amount;
    }

    public String getStudentId() { return studentId; }
    public double getAmount() { return amount; }
    public void addAmount(double amount) { this.amount += amount; }

    public String toFileString() {
        return studentId + "," + amount;
    }

    public static DueRecord fromString(String line) {
        if (line == null || line.isEmpty()) return null;
        String[] parts = line.split(",");
        if (parts.length < 2) return null;
        try {
            return new DueRecord(parts[0], Double.parseDouble(parts[1]));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
