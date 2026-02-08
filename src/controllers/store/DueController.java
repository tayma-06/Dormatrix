package controllers.store;

import models.store.DueRecord;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DueController {
    private static final String FILE = "data/inventories/dues.txt";

    public static double getDue(String studentId) {
        List<DueRecord> list = readAll();
        for (DueRecord dr : list) {
            if (dr.getStudentId().equals(studentId)) return dr.getAmount();
        }
        return 0.0;
    }

    public static void addDue(String studentId, double amount) {
        List<DueRecord> list = readAll();
        boolean found = false;
        for (DueRecord dr : list) {
            if (dr.getStudentId().equals(studentId)) {
                dr.addAmount(amount);
                found = true;
                break;
            }
        }
        if (!found) list.add(new DueRecord(studentId, amount));
        writeAll(list);
    }

    public void payDue(String studentId) {
        List<DueRecord> list = readAll();
        list.removeIf(dr -> dr.getStudentId().equals(studentId));
        writeAll(list);
    }

    private static List<DueRecord> readAll() {
        List<DueRecord> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                DueRecord dr = DueRecord.fromString(line);
                if (dr != null) list.add(dr);
            }
        } catch (IOException ignored) { }
        return list;
    }

    private static void writeAll(List<DueRecord> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (DueRecord dr : list) pw.println(dr.toFileString());
        } catch (IOException ignored) { }
    }
}
