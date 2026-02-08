package models.store;

public class StudentBalance {
    private String studentId;
    private double balance;

    public StudentBalance(String studentId, double balance) {
        this.studentId = studentId;
        this.balance = balance;
    }

    public String getStudentId() {
        return studentId;
    }

    public double getBalance() {
        return balance;
    }

    public void addBalance(double amount) {
        this.balance += amount;
    }

    public boolean deductBalance(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return studentId + "," + balance;
    }

    public static StudentBalance fromString(String line) {
        String[] parts = line.split(",");
        return new StudentBalance(parts[0], Double.parseDouble(parts[1]));
    }
}
