package repo.file;

import libraries.collections.MyArrayList;
import libraries.file.TextFile;
import models.announcements.Announcement;
import utils.FeaturePaths;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileAnnouncementRepository {

    public FileAnnouncementRepository() {
        ensureFile(FeaturePaths.ANNOUNCEMENTS);
    }

    public void save(Announcement announcement) {
        try (FileWriter fw = new FileWriter(FeaturePaths.ANNOUNCEMENTS, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(TextFile.join(
                    announcement.getAnnouncementId(),
                    announcement.getAuthorName(),
                    announcement.getTitle(),
                    announcement.getBody(),
                    announcement.getCreatedAt(),
                    announcement.getExpiresAt()   // ← new field
            ));
            bw.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MyArrayList<Announcement> findAll() {
        MyArrayList<Announcement> out = new MyArrayList<>();
        MyArrayList<String> lines = readAllLines(FeaturePaths.ANNOUNCEMENTS);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.trim().isEmpty()) continue;

            MyArrayList<String> p = TextFile.split(line);
            if (p.size() < 5) continue;

            // handle old 5-field records gracefully — expiresAt defaults to ""
            String expiresAt = p.size() >= 6 ? p.get(5) : "";

            out.add(new Announcement(
                    p.get(0), p.get(1), p.get(2), p.get(3), p.get(4), expiresAt
            ));
        }

        return out;
    }

    public boolean update(Announcement updated) {
        MyArrayList<Announcement> all = findAll();
        boolean found = false;

        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getAnnouncementId().equals(updated.getAnnouncementId())) {
                all.set(i, updated);
                found = true;
                break;
            }
        }
        if (!found) return false;

        try (FileWriter fw = new FileWriter(FeaturePaths.ANNOUNCEMENTS, false);
             BufferedWriter bw = new BufferedWriter(fw)) {
            for (int i = 0; i < all.size(); i++) {
                Announcement a = all.get(i);
                bw.write(TextFile.join(
                        a.getAnnouncementId(), a.getAuthorName(),
                        a.getTitle(), a.getBody(),
                        a.getCreatedAt(), a.getExpiresAt()
                ));
                bw.newLine();
            }
            return true;
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
}