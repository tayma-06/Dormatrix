package repo.file;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import models.enums.RoomChangeApplicationStatus;
import models.room.RoomChangeApplication;

import java.io.*;

public class FileRoomChangeApplicationRepository {

    private static final String FILE = "data/rooms/room_change_applications.txt";

    public FileRoomChangeApplicationRepository() {
        ensureFile();
    }

    public void save(RoomChangeApplication app) {
        ensureFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE, true))) {
            pw.println(app.toFileString());
        } catch (IOException ignored) {
        }
    }

    public void upsert(RoomChangeApplication app) {
        ensureFile();
        MyArrayList<RoomChangeApplication> all = findAll();
        boolean updated = false;

        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getApplicationId().equals(app.getApplicationId())) {
                all.set(i, app);
                updated = true;
                break;
            }
        }

        if (!updated) {
            all.add(app);
        }

        writeAll(all);
    }

    public MyArrayList<RoomChangeApplication> findAll() {
        ensureFile();
        MyArrayList<RoomChangeApplication> list = new MyArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                RoomChangeApplication app = RoomChangeApplication.fromFileString(line);
                if (app != null) {
                    list.add(app);
                }
            }
        } catch (IOException ignored) {
        }

        return list;
    }

    public MyOptional<RoomChangeApplication> findById(String applicationId) {
        MyArrayList<RoomChangeApplication> all = findAll();

        for (int i = 0; i < all.size(); i++) {
            RoomChangeApplication app = all.get(i);
            if (app.getApplicationId().equals(applicationId)) {
                return MyOptional.of(app);
            }
        }

        return MyOptional.empty();
    }

    public MyArrayList<RoomChangeApplication> findByStudentId(String studentId) {
        MyArrayList<RoomChangeApplication> all = findAll();
        MyArrayList<RoomChangeApplication> result = new MyArrayList<>();

        for (int i = 0; i < all.size(); i++) {
            RoomChangeApplication app = all.get(i);
            if (app.getStudentId().equals(studentId)) {
                result.add(app);
            }
        }

        return result;
    }

    public MyArrayList<RoomChangeApplication> findPending() {
        MyArrayList<RoomChangeApplication> all = findAll();
        MyArrayList<RoomChangeApplication> result = new MyArrayList<>();

        for (int i = 0; i < all.size(); i++) {
            RoomChangeApplication app = all.get(i);
            if (app.getStatus() == RoomChangeApplicationStatus.PENDING) {
                result.add(app);
            }
        }

        return result;
    }

    private void writeAll(MyArrayList<RoomChangeApplication> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (int i = 0; i < list.size(); i++) {
                pw.println(list.get(i).toFileString());
            }
        } catch (IOException ignored) {
        }
    }

    private void ensureFile() {
        File f = new File(FILE);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ignored) {
            }
        }
    }
}