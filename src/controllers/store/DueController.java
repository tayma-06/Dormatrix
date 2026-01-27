package controllers.store;

import models.store.DueRecord;
import java.io.*;

public class DueController {
    private final String FILE = "data/dues.txt";

    public double getDue(String studentId) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                DueRecord d = DueRecord.fromString(line);
                if (d.getStudentId().equals(studentId))
                    return d.getAmount();
            }
        } catch (IOException ignored) {}
        return 0;
    }

    public void payDue(String studentId) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                DueRecord d = DueRecord.fromString(line);
                if (!d.getStudentId().equals(studentId))
                    sb.append(d).append("\n");
            }
            br.close();

            PrintWriter pw = new PrintWriter(new FileWriter(FILE));
            pw.print(sb);
            pw.close();
        } catch (IOException ignored) {}
    }
}

