package repo.file;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import libraries.file.TextFile;
import models.schedule.WorkerVisitEntry;
import utils.FeaturePaths;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;

public class FileWorkerVisitRepository {

    public FileWorkerVisitRepository() {
        ensureFile(FeaturePaths.WORKER_VISITS);
    }

    public MyArrayList<WorkerVisitEntry> findAll() {
        MyArrayList<WorkerVisitEntry> out = new MyArrayList<>();
        MyArrayList<String> lines = readAllLines(FeaturePaths.WORKER_VISITS);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.trim().isEmpty()) continue;

            MyArrayList<String> p = TextFile.split(line);
            if (p.size() < 8) continue;

            out.add(new WorkerVisitEntry(
                    p.get(0),
                    p.get(1),
                    p.get(2),
                    p.get(3),
                    DayOfWeek.valueOf(p.get(4)),
                    Integer.parseInt(p.get(5)),
                    p.get(6),
                    p.get(7)
            ));
        }

        return out;
    }

    public MyOptional<WorkerVisitEntry> findByComplaintId(String complaintId) {
        MyArrayList<WorkerVisitEntry> all = findAll();

        for (int i = 0; i < all.size(); i++) {
            WorkerVisitEntry e = all.get(i);
            if (same(e.getComplaintId(), complaintId)) {
                return MyOptional.of(e);
            }
        }

        return MyOptional.empty();
    }

    public MyArrayList<WorkerVisitEntry> findByWorkerId(String workerId) {
        MyArrayList<WorkerVisitEntry> out = new MyArrayList<>();
        MyArrayList<WorkerVisitEntry> all = findAll();

        for (int i = 0; i < all.size(); i++) {
            WorkerVisitEntry e = all.get(i);
            if (same(e.getWorkerId(), workerId)) out.add(e);
        }

        return out;
    }

    public boolean hasConflict(String workerId, DayOfWeek day, int slotIndex, String ignoredComplaintId) {
        MyArrayList<WorkerVisitEntry> all = findByWorkerId(workerId);

        for (int i = 0; i < all.size(); i++) {
            WorkerVisitEntry e = all.get(i);
            if (!"PLANNED".equalsIgnoreCase(e.getStatus())) continue;
            if (ignoredComplaintId != null && same(e.getComplaintId(), ignoredComplaintId)) continue;
            if (e.getDay() == day && e.getSlotIndex() == slotIndex) return true;
        }

        return false;
    }

    public void upsert(WorkerVisitEntry entry) {
        MyArrayList<WorkerVisitEntry> all = findAll();
        boolean updated = false;

        for (int i = 0; i < all.size(); i++) {
            WorkerVisitEntry e = all.get(i);
            if (same(e.getComplaintId(), entry.getComplaintId())) {
                all.set(i, entry);
                updated = true;
                break;
            }
        }

        if (!updated) all.add(entry);
        writeAll(all);
    }

    public boolean markDone(String complaintId) {
        MyArrayList<WorkerVisitEntry> all = findAll();
        boolean found = false;

        for (int i = 0; i < all.size(); i++) {
            WorkerVisitEntry e = all.get(i);
            if (same(e.getComplaintId(), complaintId)) {
                all.set(i, new WorkerVisitEntry(
                        e.getComplaintId(),
                        e.getWorkerId(),
                        e.getStudentId(),
                        e.getRoomNo(),
                        e.getDay(),
                        e.getSlotIndex(),
                        "DONE",
                        e.getNote()
                ));
                found = true;
                break;
            }
        }

        if (found) writeAll(all);
        return found;
    }

    private void writeAll(MyArrayList<WorkerVisitEntry> all) {
        try (FileWriter fw = new FileWriter(FeaturePaths.WORKER_VISITS, false);
             BufferedWriter bw = new BufferedWriter(fw)) {
            for (int i = 0; i < all.size(); i++) {
                WorkerVisitEntry e = all.get(i);
                bw.write(TextFile.join(
                        e.getComplaintId(),
                        e.getWorkerId(),
                        e.getStudentId(),
                        e.getRoomNo(),
                        e.getDay().name(),
                        String.valueOf(e.getSlotIndex()),
                        e.getStatus(),
                        e.getNote() == null ? "" : e.getNote()
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