package cli.complaint;

import cli.Input;
import cli.forms.ComplaintForm;
import cli.views.ComplaintView;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;

import models.complaints.Complaint;

import repo.file.FileComplaintRepository;

public class WorkerComplaintCLI {

    private final ComplaintView view = new ComplaintView();
    private final ComplaintForm form = new ComplaintForm(Input.SC);
    private final FileComplaintRepository repo = new FileComplaintRepository();

    public void start(String workerIdentifier){
        while(true){
            view.workerMenu();
            int ch = form.readInt();
            if (ch == 0) return;

            if (ch == 1){
                MyArrayList<Complaint> q = repo.findByAssignedWorker(workerIdentifier);
                view.list(q);
            } else if (ch == 2){
                String cid = form.readNonEmpty("Complaint ID: ");
                String note = form.readLine("Progress note: ");
                update(workerIdentifier, cid, note, false);
            } else if (ch == 3){
                String cid = form.readNonEmpty("Complaint ID: ");
                String note = form.readLine("Completion note: ");
                update(workerIdentifier, cid, note, true);
            } else {
                view.error("Invalid choice.");
            }
        }
    }

    private void update(String workerId, String complaintId, String note, boolean complete){
        MyOptional<Complaint> cOpt = repo.findById(complaintId);
        if (cOpt.isEmpty()) { view.error("Invalid complaint ID."); return; }

        Complaint c = cOpt.get();
        if (c.getAssignedWorkerId() == null || !c.getAssignedWorkerId().trim().equals(workerId.trim())){
            view.error("You are not assigned to this complaint.");
            return;
        }

        c.setStatus(complete ? models.enums.ComplaintStatus.RESOLVED : models.enums.ComplaintStatus.IN_PROGRESS);
        c.appendTagNote((complete ? "WORKER_DONE:" : "WORKER_PROGRESS:") + (note == null ? "" : note));

        view.msg(repo.update(c) ? "Updated successfully." : "Failed to update file.");
    }
}
