package repo.file;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import models.users.Student;
import models.users.StudentPublicInfo;
import utils.FeaturePaths;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileStudentDirectoryRepository {

    public FileStudentDirectoryRepository() {
        ensureFile(FeaturePaths.STUDENTS);
    }

    public MyArrayList<Student> findAll() {
        MyArrayList<Student> out = new MyArrayList<>();
        MyArrayList<String> lines = readAllLines(FeaturePaths.STUDENTS);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.trim().isEmpty()) continue;

            Student s = Student.fromFileString(line);
            if (s != null) out.add(s);
        }

        return out;
    }

    public MyOptional<StudentPublicInfo> findPublicInfoById(String studentId) {
        MyArrayList<Student> all = findAll();

        for (int i = 0; i < all.size(); i++) {
            Student s = all.get(i);
            if (same(s.getId(), studentId)) {
                return MyOptional.of(s.publicInfo());
            }
        }

        return MyOptional.empty();
    }

    public MyOptional<Student> findById(String studentId) {
        MyArrayList<Student> all = findAll();

        for (int i = 0; i < all.size(); i++) {
            Student s = all.get(i);
            if (same(s.getId(), studentId)) {
                return MyOptional.of(s);
            }
        }

        return MyOptional.empty();
    }

    public MyArrayList<StudentPublicInfo> findByRoom(String roomNumber) {
        MyArrayList<StudentPublicInfo> out = new MyArrayList<>();
        MyArrayList<Student> all = findAll();

        for (int i = 0; i < all.size(); i++) {
            Student s = all.get(i);
            if (s.getRoomNumber() != null && s.getRoomNumber().trim().equalsIgnoreCase(roomNumber.trim())) {
                out.add(s.publicInfo());
            }
        }

        return out;
    }

    public MyOptional<String> resolveStudentId(String dashboardToken) {
        if (dashboardToken == null || dashboardToken.trim().isEmpty()) {
            return MyOptional.empty();
        }

        MyArrayList<Student> all = findAll();

        for (int i = 0; i < all.size(); i++) {
            Student s = all.get(i);
            if (same(s.getId(), dashboardToken)) {
                return MyOptional.of(s.getId());
            }
        }

        for (int i = 0; i < all.size(); i++) {
            Student s = all.get(i);
            if (s.getName() != null && s.getName().trim().equalsIgnoreCase(dashboardToken.trim())) {
                return MyOptional.of(s.getId());
            }
        }

        return MyOptional.empty();
    }

    private boolean same(String a, String b) {
        if (a == null || b == null) return a == b;
        return a.trim().equalsIgnoreCase(b.trim());
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
}