package cli.complaint;

import cli.Input;
import cli.forms.complaint.ComplaintForm;
import cli.views.complaint.ComplaintView;

import libraries.collections.MyArrayList;

import libraries.collections.MyOptional;
import models.complaints.Complaint;

import models.enums.WorkerField;
import models.users.MaintenanceWorker;
import module.complaint.ComplaintModule;
import repo.file.FileComplaintRepository;
import repo.file.FileMaintenanceWorkerRepository;
import utils.ConsoleUtil;

public class AttendantComplaintCLI {

    private final ComplaintView view = new ComplaintView();
    private final ComplaintForm form = new ComplaintForm(Input.SC);

    private final ComplaintModule module
            = new ComplaintModule(new FileComplaintRepository(), new FileMaintenanceWorkerRepository());

    private final FileComplaintRepository repo = new FileComplaintRepository();
    private final FileMaintenanceWorkerRepository workers = new FileMaintenanceWorkerRepository();

    public void start() {
        while (true) {
            ConsoleUtil.clearScreen();
            view.attendantMenu();
            int ch = form.readInt();
            if (ch == 0) {
                ConsoleUtil.clearScreen();
                return;
            }

            if (ch == 1) {
                view.attendantList(module.findAll());
                ConsoleUtil.pause();
            } else if (ch == 2) {
                view.attendantList(module.findPending());
                ConsoleUtil.pause();
            } //            else if (ch == 3) view.list(module.findMishaps());
            else if (ch == 5) {
                String cid = form.readNonEmpty("Complaint ID: ");
                MyOptional<Complaint> cOpt = repo.findById(cid);
                if (cOpt.isEmpty()) {
                    view.error("Invalid complaint ID.");
                    ConsoleUtil.pause();
                    continue;
                }
                String wid = form.readNonEmpty("Worker ID: ");
                if (isWorkerFieldMatch(cid, wid)) {
                    view.msg(module.reassignComplaint(cid, wid) ? "Reassigned successfully." : "Failed.");
                } else {
                    view.msg("The worker is not from the same WorkerField as the original worker. Please choose a different worker.");
                }
                ConsoleUtil.pause();
            } else if (ch == 6) {
                String cid = form.readNonEmpty("Complaint ID: ");
                MyOptional<Complaint> cOpt = repo.findById(cid);
                if (cOpt.isEmpty()) {
                    view.error("Invalid complaint ID.");
                    ConsoleUtil.pause();
                    continue;
                }
                String note = form.readLine("Resolution note: ");
                view.msg(module.resolveByAttendant(cid, note) ? "Resolved successfully." : "Failed.");
                ConsoleUtil.pause();
            } else if (ch == 3) {
                String room = form.readNonEmpty("Enter room number: ");
                MyArrayList<Complaint> all = repo.findAll();
                MyArrayList<Complaint> out = new MyArrayList<>();
                for (int i = 0; i < all.size(); i++) {
                    if (all.get(i).getStudentRoomNo().trim().equals(room.trim())) {
                        out.add(all.get(i));
                    }
                }
                view.attendantList(out);
                ConsoleUtil.pause();
            } else if (ch == 4) {
                String cid = form.readNonEmpty("Complaint ID: ");
                MyOptional<Complaint> cOpt = repo.findById(cid);
                if (cOpt.isEmpty()) {
                    view.error("Invalid complaint ID.");
                    ConsoleUtil.pause();
                    continue;
                }
                Complaint c = cOpt.get();
                MyArrayList<Complaint> out = new MyArrayList<>();
                out.add(c);
                view.attendantList(out);
                ConsoleUtil.pause();

            } else {
                view.error("Invalid choice.");
                ConsoleUtil.pause();
            }
        }
    }

    private boolean isWorkerFieldMatch(String complaintId, String workerId) {
        MyOptional<Complaint> complaintOpt = repo.findById(complaintId);
        if (complaintOpt.isEmpty()) {
            return false; // If complaint not found, return false
        }

        Complaint complaint = complaintOpt.get();
        String originalWorkerId = complaint.getAssignedWorkerId();

        if (originalWorkerId == null || originalWorkerId.trim().isEmpty()) {
            return true; // No worker assigned yet, so allow assignment
        }

        // Fetch the original worker and the new worker
        MyOptional<MaintenanceWorker> originalWorkerOpt = workers.findById(originalWorkerId);
        MyOptional<MaintenanceWorker> newWorkerOpt = workers.findById(workerId);

        if (originalWorkerOpt.isEmpty() || newWorkerOpt.isEmpty()) {
            return false; // If either worker is not found, return false
        }

        // Check if both workers belong to the same WorkerField
        WorkerField originalWorkerField = originalWorkerOpt.get().getField();
        WorkerField newWorkerField = newWorkerOpt.get().getField();

        return originalWorkerField == newWorkerField;
    }
}
