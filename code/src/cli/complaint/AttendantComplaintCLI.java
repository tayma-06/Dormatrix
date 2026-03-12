package cli.complaint;

import cli.Input;
import cli.forms.complaint.ComplaintForm;
import cli.views.complaint.ComplaintView;
import controllers.complaint.AttendantComplaintController;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;

import models.complaints.Complaint;
import models.users.MaintenanceWorker;

import module.complaint.ComplaintModule;
import repo.file.FileComplaintRepository;
import repo.file.FileMaintenanceWorkerRepository;
import utils.ConsoleUtil;

public class AttendantComplaintCLI {

    private final ComplaintView view = new ComplaintView();
    private final ComplaintForm form = new ComplaintForm(Input.SC);

    private final ComplaintModule module =
            new ComplaintModule(new FileComplaintRepository(), new FileMaintenanceWorkerRepository());

    private final AttendantComplaintController controller = new AttendantComplaintController();

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

            } else if (ch == 3) {
                view.stringList("ROOM NUMBERS WITH COMPLAINTS", controller.roomNumbers());

                String room = form.readNonEmpty("Enter room number: ");
                view.attendantList(controller.byRoom(room));
                ConsoleUtil.pause();

            } else if (ch == 4) {
                view.stringList("COMPLAINT IDS", controller.allComplaintIds());

                String cid = form.readNonEmpty("Complaint ID: ");
                MyOptional<Complaint> cOpt = controller.findById(cid);

                if (cOpt.isEmpty()) {
                    view.error("Invalid complaint ID.");
                    ConsoleUtil.pause();
                    continue;
                }

                MyArrayList<Complaint> out = new MyArrayList<>();
                out.add(cOpt.get());
                view.attendantList(out);
                ConsoleUtil.pause();

            } else if (ch == 5) {
                view.reassignPreview(controller.pending());

                String cid = form.readNonEmpty("Complaint ID: ");
                MyOptional<Complaint> cOpt = controller.findById(cid);

                if (cOpt.isEmpty()) {
                    view.error("Invalid complaint ID.");
                    ConsoleUtil.pause();
                    continue;
                }

                if (controller.isResolved(cid)) {
                    view.error("This complaint is already resolved.");
                    ConsoleUtil.pause();
                    continue;
                }

                MyArrayList<MaintenanceWorker> choices = controller.reassignWorkerChoices(cid);
                view.workerChoices(choices);

                if (choices.size() == 0) {
                    view.error("No other workers of the correct type are available.");
                    ConsoleUtil.pause();
                    continue;
                }

                String wid = form.readNonEmpty("Worker ID: ");

                if (!controller.workerMatchesComplaintType(cid, wid)) {
                    view.error("This worker is not the correct type for this complaint.");
                    ConsoleUtil.pause();
                    continue;
                }

                view.msg(module.reassignComplaint(cid, wid) ? "Reassigned successfully." : "Failed.");
                ConsoleUtil.pause();

            } else if (ch == 6) {
                view.resolvePreview(controller.unresolved());

                String cid = form.readNonEmpty("Complaint ID: ");
                MyOptional<Complaint> cOpt = controller.findById(cid);

                if (cOpt.isEmpty()) {
                    view.error("Invalid complaint ID.");
                    ConsoleUtil.pause();
                    continue;
                }

                if (controller.isResolved(cid)) {
                    view.error("This complaint is already resolved.");
                    ConsoleUtil.pause();
                    continue;
                }

                String note = form.readLine("Resolution note: ");
                view.msg(module.resolveByAttendant(cid, note) ? "Resolved successfully." : "Failed.");
                ConsoleUtil.pause();

            } else {
                view.error("Invalid choice.");
                ConsoleUtil.pause();
            }
        }
    }
}