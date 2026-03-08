package cli.complaint;

import cli.Input;
import cli.forms.complaint.ComplaintForm;
import cli.views.complaint.ComplaintView;

import libraries.collections.MyOptional;

import models.complaints.Complaint;

import models.enums.ComplaintStatus;
import repo.file.FileComplaintRepository;
import utils.ConsoleUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class WorkerComplaintCLI {

    private static final String WORKER_FILE = "data/users/maintenance_workers.txt";

    private final ComplaintView view = new ComplaintView();
    private final ComplaintForm form = new ComplaintForm(Input.SC);
    private final FileComplaintRepository repo = new FileComplaintRepository();

    public void start(String workerIdentifier) {
        while (true) {
            ConsoleUtil.clearScreen();

            String wid = resolveWorkerId(workerIdentifier);
            if (wid == null) {
                view.error("Could not identify worker (name/id mismatch).");
                ConsoleUtil.pause();
                continue;
            }

            view.workerList(repo.findUnresolvedByAssignedWorker(wid));

            view.workerMenu();
            int ch = form.readInt();
            if (ch == 0) {
                ConsoleUtil.clearScreen();
                return;
            }

//            if (ch == 1){
//                view.workerList(repo.findByAssignedWorker(wid));
//
//            } else
//
            if (ch == 1) {
                String cid = form.readNonEmpty("Complaint ID: ");
                MyOptional<Complaint> cOpt = repo.findById(cid);
                if (cOpt.isEmpty()) {
                    view.error("Invalid complaint ID.");
                    ConsoleUtil.pause();
                    continue;
                }
                Complaint c = cOpt.get();
                if (c.getStatus().equals(ComplaintStatus.RESOLVED)) {
                    view.error("You can not write update on a RESOLVED complaint.");
                    ConsoleUtil.pause();
                    continue;
                }

                String note = form.readLine("Progress note: ");
                update(wid, cid, note);
                ConsoleUtil.pause();

//            } else if (ch == 3){
//                String cid = form.readNonEmpty("Complaint ID: ");
//                String note = form.readLine("Completion note: ");
//                update(wid, cid, note, true);
            } else {
                view.error("Invalid choice.");
                ConsoleUtil.pause();
            }
        }
    }

    private void update(String workerId, String complaintId, String note) {
        MyOptional<Complaint> cOpt = repo.findById(complaintId);
//        if (cOpt.isEmpty()) { view.error("Invalid complaint ID."); return; }

        Complaint c = cOpt.get();

        if (c.getAssignedWorkerId() == null || !c.getAssignedWorkerId().trim().equals(workerId.trim())) {
            view.error("You are not assigned to this complaint.");
            return;
        }

        c.setStatus(models.enums.ComplaintStatus.IN_PROGRESS);
        c.appendTagNote(("WORKER_PROGRESS:") + (note == null ? "" : note));

        boolean ok = repo.update(c);
        if (ok) {
            new repo.file.FileWorkerVisitRepository().markDone(complaintId);
        }
        view.msg(ok ? "Updated successfully." : "Failed to update file.");
    }

    // -------------------- Helper: NAME/ID -> Worker ID --------------------
    private String resolveWorkerId(String target) {
        try (BufferedReader br = new BufferedReader(new FileReader(WORKER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length < 2) {
                    continue;
                }

                String id = parts[0].trim().replace("\uFEFF", "");
                String name = parts[1].trim();

                boolean matchesId = id.equals(target.trim());
                boolean matchesName = name.equalsIgnoreCase(target.trim());

                if (matchesId || matchesName) {
                    return id;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}
