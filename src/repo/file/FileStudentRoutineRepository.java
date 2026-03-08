package repo.file;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import libraries.file.TextFile;
import models.routine.StudentRoutineEntry;
import utils.FeaturePaths;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;

public class FileStudentRoutineRepository {

    public FileStudentRoutineRepository() {
        ensureFile(FeaturePaths.STUDENT_ROUTINES);
    }

    public MyArrayList<StudentRoutineEntry> findAll() {
        MyArrayList<StudentRoutineEntry> out = new MyArrayList<>();
        MyArrayList<String> lines = readAllLines(FeaturePaths.STUDENT_ROUTINES);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.trim().isEmpty()) continue;

            MyArrayList<String> p = TextFile.split(line);
            if (p.size() < 4) continue;

            String studentId = p.get(0);
            DayOfWeek day = DayOfWeek.valueOf(p.get(1));
            int slotIndex = Integer.parseInt(p.get(2));
            String content = p.get(3);

            out.add(new StudentRoutineEntry(studentId, day, slotIndex, content));
        }

        return out;
    }

    public MyArrayList<StudentRoutineEntry> findByStudentId(String studentId) {
        MyArrayList<StudentRoutineEntry> out = new MyArrayList<>();
        MyArrayList<StudentRoutineEntry> all = findAll();

        for (int i = 0; i < all.size(); i++) {
            StudentRoutineEntry e = all.get(i);
            if (same(e.getStudentId(), studentId)) out.add(e);
        }

        return out;
    }

    public MyOptional<StudentRoutineEntry> findOne(String studentId, DayOfWeek day, int slotIndex) {
        MyArrayList<StudentRoutineEntry> all = findAll();

        for (int i = 0; i < all.size(); i++) {
            StudentRoutineEntry e = all.get(i);
            if (same(e.getStudentId(), studentId) && e.getDay() == day && e.getSlotIndex() == slotIndex) {
                return MyOptional.of(e);
            }
        }

        return MyOptional.empty();
    }

    public void upsert(StudentRoutineEntry entry) {
        MyArrayList<StudentRoutineEntry> all = findAll();
        boolean updated = false;

        for (int i = 0; i < all.size(); i++) {
            StudentRoutineEntry e = all.get(i);
            if (same(e.getStudentId(), entry.getStudentId())
                    && e.getDay() == entry.getDay()
                    && e.getSlotIndex() == entry.getSlotIndex()) {
                all.set(i, entry);
                updated = true;
                break;
            }
        }

        if (!updated) all.add(entry);
        writeAll(all);
    }

    public void deleteSlot(String studentId, DayOfWeek day, int slotIndex) {
        MyArrayList<StudentRoutineEntry> all = findAll();
        MyArrayList<StudentRoutineEntry> kept = new MyArrayList<>();

        for (int i = 0; i < all.size(); i++) {
            StudentRoutineEntry e = all.get(i);
            boolean sameKey = same(e.getStudentId(), studentId)
                    && e.getDay() == day
                    && e.getSlotIndex() == slotIndex;
            if (!sameKey) kept.add(e);
        }

        writeAll(kept);
    }

    public boolean hasAnyEntry(String studentId) {
        MyArrayList<StudentRoutineEntry> all = findByStudentId(studentId);
        return all.size() > 0;
    }

    private void writeAll(MyArrayList<StudentRoutineEntry> all) {
        try (FileWriter fw = new FileWriter(FeaturePaths.STUDENT_ROUTINES, false);
             BufferedWriter bw = new BufferedWriter(fw)) {
            for (int i = 0; i < all.size(); i++) {
                StudentRoutineEntry e = all.get(i);
                bw.write(TextFile.join(
                        e.getStudentId(),
                        e.getDay().name(),
                        String.valueOf(e.getSlotIndex()),
                        e.getContent() == null ? "" : e.getContent()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MyArrayList<String> readAllLines(String path) {
        MyArrayList<String> lines = new MyArrayList<>();

        try (FileReader fr = new FileReader(path);
             BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return lines;
    }

    private void ensureFile(String filePath) {
        try {
            File f = new File(filePath);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            if (!f.exists()) f.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean same(String a, String b) {
        if (a == null || b == null) return a == b;
        return a.trim().equalsIgnoreCase(b.trim());
    }
}