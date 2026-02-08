package models.store;

public class StudentBalance {
    private String studentId;
    private double balance;

    public StudentBalance(String studentId, double balance) {
        this.studentId = studentId;
        this.balance = balance;
    }

    public String getStudentId() { return studentId; }
    public double getBalance() { return balance; }

    public void addBalance(double amount) { this.balance += amount; }
    public boolean deductBalance(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    public String toFileString() {
        return studentId + "," + balance;
    }

    public static StudentBalance fromString(String line) {
        if (line == null || line.isEmpty()) return null;
        String[] parts = line.split(",");
        if (parts.length < 2) return null;
        try {
            return new StudentBalance(parts[0], Double.parseDouble(parts[1]));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

