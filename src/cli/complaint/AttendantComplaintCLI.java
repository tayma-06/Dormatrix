package cli.complaint;

import cli.Input;
import cli.forms.ComplaintForm;
import cli.views.ComplaintView;

import libraries.collections.MyArrayList;

import models.complaints.Complaint;

import module.complaint.ComplaintModule;
import repo.file.FileComplaintRepository;
import repo.file.FileMaintenanceWorkerRepository;

public class AttendantComplaintCLI {

    private final ComplaintView view = new ComplaintView();
    private final ComplaintForm form = new ComplaintForm(Input.SC);

    private final ComplaintModule module =
            new ComplaintModule(new FileComplaintRepository(), new FileMaintenanceWorkerRepository());

    private final FileComplaintRepository repo = new FileComplaintRepository();

    public void start(){
        while(true){
            view.attendantMenu();
            int ch = form.readInt();
            if (ch == 0) return;

            if (ch == 1) view.list(module.findAll());
            else if (ch == 2) view.list(module.findPending());
            else if (ch == 3) view.list(module.findMishaps());
            else if (ch == 4){
                String cid = form.readNonEmpty("Complaint ID: ");
                String wid = form.readNonEmpty("Worker ID: ");
                view.msg(module.reassignComplaint(cid, wid) ? "Reassigned successfully." : "Failed.");
            } else if (ch == 5){
                String cid = form.readNonEmpty("Complaint ID: ");
                view.msg(module.retryAutoAssign(cid) ? "Auto-assign attempted." : "Failed.");
            } else if (ch == 6){
                String cid = form.readNonEmpty("Complaint ID: ");
                String note = form.readLine("Resolution note: ");
                view.msg(module.resolveByAttendant(cid, note) ? "Resolved successfully." : "Failed.");
            } else if (ch == 7){
                String room = form.readNonEmpty("Enter room number: ");
                MyArrayList<Complaint> all = repo.findAll();
                MyArrayList<Complaint> out = new MyArrayList<>();
                for (int i = 0; i < all.size(); i++){
                    if (all.get(i).getStudentRoomNo().trim().equals(room.trim())) out.add(all.get(i));
                }
                view.list(out);
            } else {
                view.error("Invalid choice.");
            }
        }
    }
}
