package controllers.balance;

import models.store.StudentBalance;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BalanceController {
    private final String FILE = "data/inventories/balances.txt";

    public double getBalance(String studentId) {
        List<StudentBalance> list = readAll();
        for (StudentBalance sb : list) {
            if (sb.getStudentId().equals(studentId)) return sb.getBalance();
        }
        return 0.0; // default
    }

    public void addBalance(String studentId, double amount) {
        List<StudentBalance> list = readAll();
        boolean found = false;
        for (StudentBalance sb : list) {
            if (sb.getStudentId().equals(studentId)) {
                sb.addBalance(amount);
                found = true;
                break;
            }
        }
        if (!found) list.add(new StudentBalance(studentId, amount));
        writeAll(list);
    }

    public boolean deductBalance(String studentId, double amount) {
        List<StudentBalance> list = readAll();
        boolean success = false;
        for (StudentBalance sb : list) {
            if (sb.getStudentId().equals(studentId)) {
                success = sb.deductBalance(amount);
                break;
            }
        }
        if (success) writeAll(list);
        return success;
    }

    private List<StudentBalance> readAll() {
        List<StudentBalance> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                StudentBalance sb = StudentBalance.fromString(line);
                if (sb != null) list.add(sb);
            }
        } catch (IOException ignored) { }
        return list;
    }

    private void writeAll(List<StudentBalance> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (StudentBalance sb : list) pw.println(sb.toFileString());
        } catch (IOException ignored) { }
    }
}
