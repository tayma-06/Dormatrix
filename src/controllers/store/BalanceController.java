package controllers.store;

import models.store.StudentBalance;
import java.io.*;

public class BalanceController {
    private final String FILE = "data/inventories/balances.txt";

    public double getBalance(String studentId) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                StudentBalance balance = StudentBalance.fromString(line);
                if (balance.getStudentId().equals(studentId)) {
                    return balance.getBalance();
                }
            }
        } catch (IOException e) {
            // File might not exist yet
        }
        return 0.0;
    }

    public void addBalance(String studentId, double amount) {
        try {
            File f = new File(FILE);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            BufferedReader br = new BufferedReader(new FileReader(FILE));
            StringBuilder sb = new StringBuilder();
            boolean found = false;
            String line;

            while ((line = br.readLine()) != null) {
                StudentBalance balance = StudentBalance.fromString(line);
                if (balance.getStudentId().equals(studentId)) {
                    balance.addBalance(amount);
                    found = true;
                }
                sb.append(balance).append("\n");
            }
            br.close();

            if (!found) {
                sb.append(new StudentBalance(studentId, amount)).append("\n");
            }

            PrintWriter pw = new PrintWriter(new FileWriter(FILE));
            pw.print(sb);
            pw.close();

        } catch (IOException e) {
            System.out.println("Error updating balance: " + e.getMessage());
        }
    }

    public boolean deductBalance(String studentId, double amount) {
        try {
            File f = new File(FILE);
            if (!f.exists()) {
                return false; // No balance file means no balance
            }

            BufferedReader br = new BufferedReader(new FileReader(FILE));
            StringBuilder sb = new StringBuilder();
            boolean found = false;
            boolean success = false;
            String line;

            while ((line = br.readLine()) != null) {
                StudentBalance balance = StudentBalance.fromString(line);
                if (balance.getStudentId().equals(studentId)) {
                    found = true;
                    success = balance.deductBalance(amount);
                }
                sb.append(balance).append("\n");
            }
            br.close();

            if (found && success) {
                PrintWriter pw = new PrintWriter(new FileWriter(FILE));
                pw.print(sb);
                pw.close();
                return true;
            }

            return false;

        } catch (IOException e) {
            System.out.println("Error deducting balance: " + e.getMessage());
            return false;
        }
    }

    public void displayBalance(String studentId) {
        double balance = getBalance(studentId);
        System.out.println("--------------------------------------------");
        System.out.println("|           Account Balance                |");
        System.out.println("--------------------------------------------");
        System.out.printf("  Student ID: %s\n", studentId);
        System.out.printf("  Balance:    $%.2f\n", balance);
        System.out.println("--------------------------------------------");
    }
}