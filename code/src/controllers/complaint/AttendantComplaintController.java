package controllers.complaint;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;

import models.complaints.Complaint;
import models.complaints.ComplaintPolicy;
import models.enums.ComplaintStatus;
import models.enums.WorkerField;
import models.users.MaintenanceWorker;

import module.complaint.ComplaintModule;
import repo.file.FileComplaintRepository;
import repo.file.FileMaintenanceWorkerRepository;

public class AttendantComplaintController {

    private final ComplaintModule module =
            new ComplaintModule(new FileComplaintRepository(), new FileMaintenanceWorkerRepository());

    private final FileComplaintRepository repo = new FileComplaintRepository();
    private final FileMaintenanceWorkerRepository workerRepo = new FileMaintenanceWorkerRepository();
    private final ComplaintPolicy policy = new ComplaintPolicy();

    public MyArrayList<Complaint> all(){ return module.findAll(); }
    public MyArrayList<Complaint> pending(){ return module.findPending(); }
    public MyArrayList<Complaint> unresolved(){ return module.findPending(); }
    public MyArrayList<Complaint> mishaps(){ return module.findMishaps(); }

    public MyOptional<Complaint> findById(String complaintId){
        return repo.findById(complaintId);
    }

    public boolean isResolved(String complaintId){
        MyOptional<Complaint> cOpt = repo.findById(complaintId);
        if (cOpt.isEmpty()) return false;
        return cOpt.get().getStatus() == ComplaintStatus.RESOLVED;
    }

    public MyArrayList<Complaint> byRoom(String roomId){
        MyArrayList<Complaint> all = repo.findAll();
        MyArrayList<Complaint> out = new MyArrayList<>();

        if (roomId == null) return out;

        for (int i = 0; i < all.size(); i++){
            Complaint c = all.get(i);
            String room = c.getStudentRoomNo();
            if (room != null && room.trim().equalsIgnoreCase(roomId.trim())) {
                out.add(c);
            }
        }
        return out;
    }

    public MyArrayList<String> roomNumbers(){
        MyArrayList<String> out = new MyArrayList<>();
        MyArrayList<Complaint> all = repo.findAll();

        for (int i = 0; i < all.size(); i++){
            String room = all.get(i).getStudentRoomNo();
            if (room == null || room.trim().isEmpty()) continue;
            addUnique(out, room.trim());
        }
        return out;
    }

    public MyArrayList<String> allComplaintIds(){
        MyArrayList<String> out = new MyArrayList<>();
        MyArrayList<Complaint> all = repo.findAll();

        for (int i = 0; i < all.size(); i++){
            String cid = all.get(i).getComplaintId();
            if (cid == null || cid.trim().isEmpty()) continue;
            out.add(cid.trim());
        }
        return out;
    }

    public MyArrayList<String> unresolvedComplaintIds(){
        MyArrayList<String> out = new MyArrayList<>();
        MyArrayList<Complaint> all = module.findPending();

        for (int i = 0; i < all.size(); i++){
            String cid = all.get(i).getComplaintId();
            if (cid == null || cid.trim().isEmpty()) continue;
            out.add(cid.trim());
        }
        return out;
    }

    public MyArrayList<MaintenanceWorker> reassignWorkerChoices(String complaintId){
        MyArrayList<MaintenanceWorker> out = new MyArrayList<>();
        MyOptional<Complaint> cOpt = repo.findById(complaintId);
        if (cOpt.isEmpty()) return out;

        Complaint c = cOpt.get();
        WorkerField expected = policy.recommendedWorkerField(c.getCategory());
        String currentWorkerId = c.getAssignedWorkerId();

        MyArrayList<MaintenanceWorker> allWorkers = workerRepo.findAll();
        for (int i = 0; i < allWorkers.size(); i++){
            MaintenanceWorker w = allWorkers.get(i);
            if (w.getField() != expected) continue;

            if (currentWorkerId != null &&
                    !currentWorkerId.trim().isEmpty() &&
                    w.getId() != null &&
                    w.getId().trim().equalsIgnoreCase(currentWorkerId.trim())) {
                continue;
            }

            out.add(w);
        }

        return out;
    }

    public boolean workerMatchesComplaintType(String complaintId, String workerId){
        MyOptional<Complaint> cOpt = repo.findById(complaintId);
        MyOptional<MaintenanceWorker> wOpt = workerRepo.findById(workerId);

        if (cOpt.isEmpty() || wOpt.isEmpty()) return false;

        WorkerField expected = policy.recommendedWorkerField(cOpt.get().getCategory());
        return wOpt.get().getField() == expected;
    }

    private void addUnique(MyArrayList<String> list, String value){
        for (int i = 0; i < list.size(); i++){
            String cur = list.get(i);
            if (cur != null && cur.trim().equalsIgnoreCase(value.trim())) return;
        }
        list.add(value);
    }
}