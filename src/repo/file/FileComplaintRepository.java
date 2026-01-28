package repo.file;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import libraries.collections.MyString;
import models.complaints.Complaint;
import models.enums.ComplaintCategory;
import models.enums.ComplaintStatus;
import models.enums.PriorityLevel;
import repo.ComplaintRepository;
import libraries.file.TextFile;
import libraries.file.FilePaths;

import java.io.*;

public class FileComplaintRepository implements ComplaintRepository{
    public FileComplaintRepository() {
        ensureFile(FilePaths.COMPLAINTS);
    }

    @Override
    public void save(Complaint c){
        appendLine(FilePaths.COMPLAINTS, toLine(c));
    }

    @Override
    public boolean update(Complaint updated){
        MyArrayList<Complaint> all = findAll();
        boolean found = false;

        for (int i = 0; i < all.size(); i++){
            if (msEquals(all.get(i).getComplaintId(), updated.getComplaintId())){
                all.set(i, updated);
                found = true;
                break;
            }
        }
        if (!found) return false;

        writeAll(all);
        return true;
    }

    @Override
    public MyOptional<Complaint> findById(String complaintId){
        MyArrayList<Complaint> all = findAll();
        for (int i = 0; i < all.size(); i++)
        {
            Complaint c = all.get(i);
            if (msEquals(c.getComplaintId(), complaintId)) return MyOptional.of(c);
        }
        return MyOptional.empty();
    }

    @Override
    public MyArrayList<Complaint> findAll(){
        MyArrayList<Complaint> out = new MyArrayList<>();

        MyArrayList<String> lines = readAllLines(FilePaths.COMPLAINTS);
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i);
            if (line == null || new MyString(line).trim().isEmpty()) continue;

            MyArrayList<String> p = TextFile.split(line);

            if (p.size() == 10){
                out.add(new Complaint(
                        p.get(0),
                        p.get(1), p.get(2), p.get(3),
                        ComplaintCategory.valueOf(p.get(4)),
                        p.get(5),
                        ComplaintStatus.valueOf(p.get(6)),
                        p.get(7),
                        PriorityLevel.valueOf(p.get(8)),
                        p.get(9)
                ));
            }
        }

        return out;
    }

    @Override
    public MyArrayList<Complaint> findUnassigned(){
        MyArrayList<Complaint> out = new MyArrayList<>();
        MyArrayList<Complaint> all = findAll();

        for (int i = 0; i < all.size(); i++){
            Complaint c = all.get(i);
            String wid = c.getAssignedWorkerId();
            boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();
            if (blank) out.add(c);
        }

        return out;
    }

    @Override
    public MyArrayList<Complaint> findByStudentId(String studentId){
        MyArrayList<Complaint> out = new MyArrayList<>();
        MyArrayList<Complaint> all = findAll();

        for (int i = 0; i < all.size(); i++){
            Complaint c = all.get(i);
            if (msEquals(c.getStudentId(), studentId )) out.add(c);
        }

        return out;
    }

    @Override
    public MyArrayList<Complaint> findByAssignedWorker(String workerId){
        MyArrayList<Complaint> out = new MyArrayList<>();
        MyArrayList<Complaint> all = findAll();

        for (int i = 0; i < all.size(); i++)
        {
            Complaint c = all.get(i);
            if (msEquals(c.getAssignedWorkerId(), workerId)) out.add(c);
        }

        return out;
    }

    private String toLine(Complaint c){
        return TextFile.join(
                c.getComplaintId(),
                c.getStudentId(),
                c.getStudentName(),
                c.getStudentRoomNo(),
                c.getCategory().name(),
                c.getDescription(),
                c.getStatus().name(),
                c.getAssignedWorkerId() == null ? "" : c.getAssignedWorkerId(),
                c.getPriority().name(),
                c.getTags() == null ? "" : c.getTags()
        );
    }

    private void writeAll(MyArrayList<Complaint> all){
        try (FileWriter fw = new FileWriter(FilePaths.COMPLAINTS, false);
        BufferedWriter bw = new BufferedWriter(fw)){
            for (int i = 0; i < all.size(); i++){
                bw.write(toLine(all.get(i)));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureFile(String filePath){
        try{
            File f = new File(filePath);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            if (!f.exists()) f.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void appendLine(String path, String line){
        try (FileWriter fw = new FileWriter(path, true);
        BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private MyArrayList<String> readAllLines(String path) {
        MyArrayList<String> lines = new MyArrayList<>();
        try (FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr)){
            String line;
            while ((line = br.readLine()) != null){
                lines.add(line);
            }
            return lines;
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private boolean msEquals(String a, String b){
        if (a == null || b == null) return a == b;
        return new MyString(a).equals(new MyString(b));
    }

}
