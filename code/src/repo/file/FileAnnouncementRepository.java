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
                    announcement.getCreatedAt()
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

            out.add(new Announcement(
                    p.get(0),
                    p.get(1),
                    p.get(2),
                    p.get(3),
                    p.get(4)
            ));
        }

        return out;
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