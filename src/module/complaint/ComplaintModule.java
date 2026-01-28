package module.complaint;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import models.complaints.Complaint;
import models.complaints.ComplaintPolicy;
import models.enums.ComplaintCategory;
import models.enums.ComplaintStatus;
import models.users.StudentPublicInfo;
import repo.ComplaintRepository;
import repo.MaintenanceWorkerRepository;
import module.complaint.ComplaintService;
import libraries.file.FilePaths;

public class ComplaintModule {
    private final ComplaintService complaintService;

    public ComplaintModule(ComplaintRepository complaintRepo, MaintenanceWorkerRepository workerRepo){
        ComplaintPolicy policy = new ComplaintPolicy();
        this.complaintService = new ComplaintService(complaintRepo, workerRepo, policy);
        this.complaintService.initIdGeneratorFromFile(FilePaths.COMPLAINTS);
    }

    public Complaint fileComplaint(StudentPublicInfo info, ComplaintCategory category, String desc){
        return complaintService.createComplaint(info, category, desc);
    }

    public MyOptional<Complaint> findById(String complaintId){
        return complaintService.findById(complaintId);
    }

    public MyArrayList<Complaint> findAll(){
        return complaintService.findAll();
    }

    public MyArrayList<Complaint> findPending(){
        return complaintService.findPending();
    }

    public MyArrayList<Complaint> findMishaps(){
        return complaintService.findMishaps();
    }

//    public boolean updateStatus(String complaintId, ComplaintStatus status){
//        return complaintService.updateStatus(complaintId, status);
//    }
//
//    public MyArrayList<Complaint> findByStudentId(String studentId){
//        return complaintService.findByStudentId(studentId);
//    }
//
//    public MyArrayList<Complaint> findByAssignedWorker(String workerId){
//        return complaintService.findByAssignedWorker(workerId);
//    }
//
//    public MyArrayList<Complaint> findUnassigned(){
//        return complaintService.findUnassigned();
//    }

    public boolean reassignComplaint(String complaintId, String workerId){
        return complaintService.reassignComplaint(complaintId, workerId);
    }

    public boolean retryAutoAssign(String complaintId){
        return complaintService.retryAutoAssign(complaintId);
    }

    public boolean resolveByAttendant(String complaintId, String note){
        return complaintService.resolveByAttendant(complaintId, note);
    }

}
