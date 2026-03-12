package repo.file;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import libraries.file.TextFile;
import models.contacts.EmergencyContactEntry;
import utils.FeaturePaths;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileEmergencyContactRepository {

    public FileEmergencyContactRepository() {
        ensureFile(FeaturePaths.EMERGENCY_CONTACTS);
    }

    public MyArrayList<EmergencyContactEntry> findAll() {
        MyArrayList<EmergencyContactEntry> out = new MyArrayList<>();
        MyArrayList<String> lines = readAllLines(FeaturePaths.EMERGENCY_CONTACTS);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.trim().isEmpty()) continue;

            MyArrayList<String> p = TextFile.split(line);
            if (p.size() < 6) continue;

            out.add(new EmergencyContactEntry(
                    p.get(0),
                    p.get(1),
                    p.get(2),
                    p.get(3),
                    p.get(4),
                    p.get(5)
            ));
        }

        return out;
    }

    public MyOptional<EmergencyContactEntry> findByKey(String key) {
        MyArrayList<EmergencyContactEntry> all = findAll();

        for (int i = 0; i < all.size(); i++) {
            EmergencyContactEntry e = all.get(i);
            if (same(e.getKey(), key)) return MyOptional.of(e);
        }

        return MyOptional.empty();
    }

    public void upsert(EmergencyContactEntry entry) {
        MyArrayList<EmergencyContactEntry> all = findAll();
        boolean updated = false;

        for (int i = 0; i < all.size(); i++) {
            EmergencyContactEntry e = all.get(i);
            if (same(e.getKey(), entry.getKey())) {
                all.set(i, entry);
                updated = true;
                break;
            }
        }

        if (!updated) all.add(entry);
        writeAll(all);
    }

    public boolean delete(String key) {
        MyArrayList<EmergencyContactEntry> all = findAll();
        MyArrayList<EmergencyContactEntry> kept = new MyArrayList<>();
        boolean found = false;

        for (int i = 0; i < all.size(); i++) {
            EmergencyContactEntry e = all.get(i);
            if (same(e.getKey(), key)) {
                found = true;
            } else {
                kept.add(e);
            }
        }

        if (found) writeAll(kept);
        return found;
    }

    private void writeAll(MyArrayList<EmergencyContactEntry> all) {
        try (FileWriter fw = new FileWriter(FeaturePaths.EMERGENCY_CONTACTS, false);
             BufferedWriter bw = new BufferedWriter(fw)) {
            for (int i = 0; i < all.size(); i++) {
                EmergencyContactEntry e = all.get(i);
                bw.write(TextFile.join(
                        e.getKey(),
                        e.getLabel(),
                        e.getContactName() == null ? "" : e.getContactName(),
                        e.getPhone() == null ? "" : e.getPhone(),
                        e.getNote() == null ? "" : e.getNote(),
                        e.getUpdatedBy() == null ? "" : e.getUpdatedBy()
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
