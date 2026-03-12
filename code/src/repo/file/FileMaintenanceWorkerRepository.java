package repo.file;

import com.sun.tools.javac.Main;
import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import libraries.collections.MyString;
import models.enums.WorkerField;
import models.users.MaintenanceWorker;
import repo.MaintenanceWorkerRepository;
import libraries.file.TextFile;
import libraries.file.FilePaths;
import java.io.*;

public class FileMaintenanceWorkerRepository implements MaintenanceWorkerRepository{
    public FileMaintenanceWorkerRepository(){
        ensureFile(FilePaths.MAINTENANCE_WORKERS);
    }

    @Override
    public MyOptional<MaintenanceWorker> findById(String workerId){
        MyArrayList<MaintenanceWorker> all = findAll();
        for (int i =0; i < all.size(); i++){
            MaintenanceWorker w = all.get(i);
            if(msEquals(w.getId(), workerId)) return MyOptional.of(w);
        }
        return MyOptional.empty();
    }

    @Override
    public MyArrayList<MaintenanceWorker> findAll() {
        MyArrayList<MaintenanceWorker> out = new MyArrayList<>();

        MyArrayList<String> lines = readAllLines(FilePaths.MAINTENANCE_WORKERS);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || new MyString(line).trim().isEmpty()) continue;

            MyArrayList<String> p = TextFile.split(line);
            if (p.size() < 6) continue;

            String id = p.get(0);
            String name = p.get(1);
            String role = p.get(2);

            String passwordHash;
            String phone;
            WorkerField field;

            if (p.size() >= 7) {
                passwordHash = p.get(4);
                phone = p.get(5);
                field = WorkerField.valueOf(p.get(6));
            }
            else {
                passwordHash = p.get(3);
                phone = p.get(4);
                field = WorkerField.valueOf(p.get(5));
            }

            out.add(new MaintenanceWorker(id, name, role, passwordHash, phone, field));
        }

        return out;
    }

    private String toLine(MaintenanceWorker w) {
        return TextFile.join(
                w.getId(),
                w.getName(),
                w.getRole(),
                w.getPasswordHash(),
                w.getPhoneNumber(),
                w.getField().name()
        );
    }

    private void ensureFile(String filePath) {
        try {
            File dir = new File(FilePaths.DATA_DIR);
            if (!dir.exists()) dir.mkdirs();

            File f = new File(filePath);
            if (!f.exists()) f.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void appendLine(String path, String line) {
        try (FileWriter fw = new FileWriter(path, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(line);
            bw.newLine();
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
            return lines;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean msEquals(String a, String b) {
        if (a == null || b == null) return a == b;
        return new MyString(a).equals(new MyString(b));
    }
}
