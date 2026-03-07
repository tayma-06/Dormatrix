package controllers.complaint;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import libraries.collections.MyString;

import models.complaints.Complaint;
import models.enums.ComplaintStatus;

import module.complaint.ComplaintModule;
import repo.file.FileComplaintRepository;
import repo.file.FileMaintenanceWorkerRepository;

public class AttendantComplaintController {

    private final ComplaintModule module =
            new ComplaintModule(new FileComplaintRepository(), new FileMaintenanceWorkerRepository());
    private final FileComplaintRepository repo = new FileComplaintRepository();

    public MyArrayList<Complaint> all(){ return module.findAll(); }
    public MyArrayList<Complaint> pending(){ return module.findPending(); }
    public MyArrayList<Complaint> mishaps(){ return module.findMishaps(); }

//    public boolean reassign(String cid, String wid){ return module.reassignComplaint(cid, wid); }
//    public boolean retryAutoAssign(String cid){ return module.retryAutoAssign(cid); }
//    public boolean resolve(String cid, String note){ return module.resolveByAttendant(cid, note); }

    public MyArrayList<Complaint> byRoom(String roomId){
        MyArrayList<Complaint> all = repo.findAll();
        MyArrayList<Complaint> out = new MyArrayList<>();
        for (int i = 0; i < all.size(); i++){
            Complaint c = all.get(i);
            if (new MyString(c.getStudentRoomNo()).trim().equals(new MyString(roomId).trim())) out.add(c);
        }
        return out;
    }
}
