package module.complaint;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import libraries.collections.MyString;
import models.complaints.Complaint;
import models.complaints.ComplaintIdGenerator;
import models.complaints.ComplaintPolicy;
import models.users.StudentPublicInfo;
import models.users.MaintenanceWorker;
import models.enums.ComplaintCategory;
import models.enums.ComplaintStatus;
import models.enums.WorkerField;
import repo.ComplaintRepository;
import repo.MaintenanceWorkerRepository;

public class ComplaintService {

    private final ComplaintRepository complaints;
    private final MaintenanceWorkerRepository workers;
    private final ComplaintPolicy comPolicy;

    public ComplaintService(ComplaintRepository complaints,
                            MaintenanceWorkerRepository workers,
                            ComplaintPolicy comPolicy){
        this.complaints = complaints;
        this.workers = workers;
        this.comPolicy = comPolicy;
    }

    public void initIdGeneratorFromFile(String complaintsFilePath) {
        ComplaintIdGenerator.initFromFile(complaintsFilePath);
    }

    public Complaint createComplaint(StudentPublicInfo info, ComplaintCategory cat, String desc){
        ComplaintPolicy.DormDecision decision = comPolicy.decide(cat, desc);

        Complaint c = Complaint.createNew(
                ComplaintIdGenerator.complaintId(),
                info,
                cat,
                desc,
                decision.getPriority(),
                decision.getTags()
        );

        try {
            MyOptional<String> chosenWorkerId = autoAssignWorker(cat);
            if (chosenWorkerId.isPresent()){
                c.assignTo(chosenWorkerId.get()); // status ASSIGNED
            }
        } catch (RuntimeException ex) {
            c.clearAssignment();
        }

        complaints.save(c);
        return c;
    }

    public boolean updateStatus(String complaintId, ComplaintStatus status){
        MyOptional<Complaint> cOpt = complaints.findById(complaintId);
        if (cOpt.isEmpty()) return false;

        Complaint c = cOpt.get();
        c.setStatus(status);
        return complaints.update(c);
    }

    private MyOptional<String> autoAssignWorker(ComplaintCategory cat){
        WorkerField field = comPolicy.recommendedWorkerField(cat);

        MyArrayList<MaintenanceWorker> all = workers.findAll();
        MaintenanceWorker best = null;
        int bestLoad = Integer.MAX_VALUE;

        for(int i = 0; i < all.size(); i++){
            MaintenanceWorker w = all.get(i);
            if (w.getField() != field) continue;

            int load = activeLoad(w);
            if (best == null || load < bestLoad){
                best = w;
                bestLoad = load;
            }
        }

        if (best == null) return MyOptional.empty();
        return MyOptional.of(best.getId());
    }

    private int activeLoad(MaintenanceWorker w){
        MyArrayList<Complaint> assigned = complaints.findByAssignedWorker(w.getId());

        int count = 0;
        for (int i = 0; i < assigned.size(); i++){
            Complaint c = assigned.get(i);
            if (c.getStatus() != ComplaintStatus.RESOLVED) count++;
        }
        return count;
    }

    private boolean msEquals(String a, String b){
        if (a == null || b == null) return a == b;
        return new MyString(a).equals(new MyString(b));
    }

    public MyOptional<Complaint> findById(String id){ return complaints.findById(id); }
//    public MyArrayList<Complaint> findByStudentId(String sid){ return complaints.findByStudentId(sid); }
//    public MyArrayList<Complaint> findByAssignedWorker(String wid){ return complaints.findByAssignedWorker(wid); }
//    public MyArrayList<Complaint> findUnassigned(){ return complaints.findUnassigned(); }
    public MyArrayList<Complaint> findAll(){ return complaints.findAll(); }

    public MyArrayList<Complaint> findPending(){
        MyArrayList<Complaint> out = new MyArrayList<>();
        MyArrayList<Complaint> all = complaints.findAll();

        for (int i = 0; i < all.size(); i++){
            Complaint c = all.get(i);
            boolean blankWorker = (c.getAssignedWorkerId() == null) || new MyString(c.getAssignedWorkerId()).trim().isEmpty();
            if (c.getStatus() == ComplaintStatus.SUBMITTED || blankWorker) out.add(c);
        }
        return out;
    }

    public MyArrayList<Complaint> findMishaps(){
        MyArrayList<Complaint> out = new MyArrayList<>();
        MyArrayList<Complaint> all = complaints.findAll();

        for (int i = 0; i < all.size(); i++){
            Complaint c = all.get(i);

            String wid = c.getAssignedWorkerId();
            boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();

            // Mishap 1: unassigned
            if (blank || c.getStatus() == ComplaintStatus.SUBMITTED){
                out.add(c);
                continue;
            }

            // Mishap 2: assigned worker id not found
            MyOptional<MaintenanceWorker> wOpt = workers.findById(wid);
            if (wOpt.isEmpty()){
                out.add(c);
                continue;
            }

            // Mishap 3: category-field mismatch
            WorkerField expected = comPolicy.recommendedWorkerField(c.getCategory());
            if (wOpt.get().getField() != expected){
                out.add(c);
            }
        }

        return out;
    }

    public boolean reassignComplaint(String complaintId, String workerId){
        MyOptional<Complaint> cOpt = complaints.findById(complaintId);
        if (cOpt.isEmpty()) return false;

        // validate worker exists
        if (workers.findById(workerId).isEmpty()) return false;

        Complaint c = cOpt.get();
        c.assignTo(workerId);
        c.appendTagNote("ATTENDANT_REASSIGNED_TO:" + workerId);
        return complaints.update(c);
    }

    public boolean retryAutoAssign(String complaintId){
        MyOptional<Complaint> cOpt = complaints.findById(complaintId);
        if (cOpt.isEmpty()) return false;

        Complaint c = cOpt.get();
        MyOptional<String> wid = autoAssignWorker(c.getCategory());

        if (wid.isEmpty()){
            c.clearAssignment();
            c.appendTagNote("ATTENDANT_AUTO_ASSIGN_FAILED");
            return complaints.update(c);
        }

        c.assignTo(wid.get());
        c.appendTagNote("ATTENDANT_AUTO_ASSIGNED_TO:" + wid.get());
        return complaints.update(c);
    }

    public boolean resolveByAttendant(String complaintId, String note){
        MyOptional<Complaint> cOpt = complaints.findById(complaintId);
        if (cOpt.isEmpty()) return false;

        Complaint c = cOpt.get();
        c.setStatus(ComplaintStatus.RESOLVED);
        c.appendTagNote("ATTENDANT_RESOLVED:" + (note == null ? "" : note));
        return complaints.update(c);
    }
}
