package controllers.complaint;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;

import models.complaints.Complaint;
import models.enums.ComplaintCategory;
import models.users.StudentPublicInfo;

import module.complaint.ComplaintModule;
import repo.file.FileComplaintRepository;
import repo.file.FileMaintenanceWorkerRepository;

public class StudentComplaintController {
    private static final String STUDENT_FILE = "data/users/students.txt";

    private final ComplaintModule module =
            new ComplaintModule(new FileComplaintRepository(), new FileMaintenanceWorkerRepository());
    private final FileComplaintRepository repo = new FileComplaintRepository();

    public MyOptional<Complaint> file(String studentIdentifier, ComplaintCategory cat, String desc){
        MyOptional<StudentPublicInfo> info = resolveStudentPublicInfo(studentIdentifier);
        if (info.isEmpty()) return MyOptional.empty();
        return MyOptional.of(module.fileComplaint(info.get(), cat, desc));
    }

    public MyArrayList<Complaint> myComplaints(String studentIdentifier){
        MyOptional<StudentPublicInfo> info = resolveStudentPublicInfo(studentIdentifier);
        if (info.isEmpty()) return new MyArrayList<>();
        return repo.findByStudentId(info.get().getStudentId());
    }

    public MyOptional<Complaint> track(String complaintId){
        return repo.findById(complaintId);
    }

    private MyOptional<StudentPublicInfo> resolveStudentPublicInfo(String target){
        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))){
            String line;
            while((line = br.readLine()) != null){
                String[] p = line.split("\\|", -1);
                if (p.length < 2) continue;

                String id = p[0].trim().replace("\uFEFF", "");
                String name = p[1].trim();
                String room = (p.length > 7) ? p[7].trim() : "UNASSIGNED";
                if (room.isEmpty()) room = "UNASSIGNED";

                if (id.equals(target.trim()) || name.equalsIgnoreCase(target.trim())){
                    return MyOptional.of(new StudentPublicInfo(id, name, room));
                }
            }
        } catch (IOException e){
            return MyOptional.empty();
        }
        return MyOptional.empty();
    }
}
