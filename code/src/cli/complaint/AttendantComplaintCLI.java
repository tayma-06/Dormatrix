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
import utils.BackgroundFiller;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import static utils.TerminalUI.*;
import static utils.TerminalUIExtras.tArrowSelect;
import static utils.TerminalUIExtras.tCustomInputRow;

public class AttendantComplaintCLI {

    private final ComplaintView view = new ComplaintView();
    private final ComplaintForm form = new ComplaintForm(Input.SC);

    private static final TerminalUI.MenuItem[] MENU = {
            new TerminalUI.MenuItem(1, "View all complaints"),
            new TerminalUI.MenuItem(2, "View pending complaints"),
            new TerminalUI.MenuItem(3, "View complaints by room"),
            new TerminalUI.MenuItem(4, "View complaints by complaint id"),
            new TerminalUI.MenuItem(5, "Reassign complaint (manual worker id)"),
            new TerminalUI.MenuItem(6, "Resolve complaint"),
            new TerminalUI.MenuItem(0, "Back"),
    };

    private final ComplaintModule module =
            new ComplaintModule(new FileComplaintRepository(), new FileMaintenanceWorkerRepository());

    private final AttendantComplaintController controller = new AttendantComplaintController();

    public void start() {
        while (true) {
            try {
                ConsoleUtil.clearScreen();
                BackgroundFiller.applyAttendantTheme();
                TerminalUI.setActiveTheme(
                        utils.ConsoleColors.fgRGB(40, 220, 210),
                        utils.ConsoleColors.ThemeText.ATTENDANT_TEXT,
                        utils.ConsoleColors.bgRGB(0, 28, 26)
                );
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());

                TerminalUI.drawDashboard(
                        "COMPLAINT (ATTENDANT)", "",
                        MENU,
                        utils.ConsoleColors.ThemeText.ATTENDANT_TEXT,
                        utils.ConsoleColors.fgRGB(40, 220, 210),
                        null, 3
                );

                int ch = TerminalUI.readChoiceArrow();

                if (ch == 0) {
                    ConsoleUtil.clearScreen();
                    return;
                }

                // clear before sub-screen
                ConsoleUtil.clearScreen();
                BackgroundFiller.applyAttendantTheme();
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                TerminalUI.at(2, 1);

                if (ch == 1) {
                    view.attendantList(module.findAll());
                    ConsoleUtil.pause();

                } else if (ch == 2) {
                    view.attendantList(module.findPending());
                    ConsoleUtil.pause();

                } else if (ch == 3) {
                    MyArrayList<String> rooms = controller.roomNumbers();
                    if (rooms.size() == 0) {
                        tBoxTop();
                        tBoxLine("No rooms with complaints found.");
                        tBoxBottom();
                        tPause();
                        continue;
                    }

                    // Build numbered labels
                    String[] roomLabels = new String[rooms.size()];
                    for (int i = 0; i < rooms.size(); i++) {
                        roomLabels[i] = String.format("%-5s%s", "[" + (i + 1) + "]", rooms.get(i));
                    }

                    int idx;
                    try { idx = tArrowSelect("ROOM NUMBERS WITH COMPLAINTS", roomLabels); }
                    catch (InterruptedException e) { continue; }
                    if (idx < 0 || idx >= rooms.size()) continue;

                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyAttendantTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);

                    view.attendantList(controller.byRoom(rooms.get(idx)));
                    tPause();

                } else if (ch == 4) {
                    MyArrayList<String> ids = controller.allComplaintIds();
                    if (ids.size() == 0) {
                        tBoxTop();
                        tBoxLine("No complaints found.");
                        tBoxBottom();
                        tPause();
                        continue;
                    }

                    String[] idLabels = new String[ids.size()];
                    for (int i = 0; i < ids.size(); i++) {
                        idLabels[i] = String.format("%-5s%s", "[" + (i + 1) + "]", ids.get(i));
                    }

                    int idx;
                    try { idx = tArrowSelect("COMPLAINT IDS", idLabels); }
                    catch (InterruptedException e) { continue; }
                    if (idx < 0 || idx >= ids.size()) continue;

                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyAttendantTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);

                    MyOptional<Complaint> cOpt = controller.findById(ids.get(idx));
                    if (cOpt.isEmpty()) {
                        tError("Invalid complaint ID.");
                        tPause();
                        continue;
                    }

                    MyArrayList<Complaint> out = new MyArrayList<>();
                    out.add(cOpt.get());
                    view.attendantList(out);
                    tPause();

                } else if (ch == 5) {
                    MyArrayList<Complaint> pending = controller.pending();
                    if (pending.size() == 0) {
                        tBoxTop();
                        tBoxLine("No pending complaints found.");
                        tBoxBottom();
                        tPause();
                        continue;
                    }

                    String[] pendingLabels = new String[pending.size()];
                    for (int i = 0; i < pending.size(); i++) {
                        Complaint c = pending.get(i);
                        String wid = c.getAssignedWorkerId();
                        if (wid == null || wid.trim().isEmpty()) wid = "(none)";
                        pendingLabels[i] = String.format("%-5s%-10s | %-14s | %s",
                                "[" + (i + 1) + "]",
                                c.getComplaintId(),
                                c.getCategory().name(),
                                wid);
                    }

                    int idx;
                    try { idx = tArrowSelect("PENDING COMPLAINTS", pendingLabels); }
                    catch (InterruptedException e) { continue; }
                    if (idx < 0 || idx >= pending.size()) continue;

                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyAttendantTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);

                    Complaint selectedC = pending.get(idx);
                    String cid = selectedC.getComplaintId();

                    if (controller.isResolved(cid)) {
                        tError("This complaint is already resolved.");
                        tPause();
                        continue;
                    }

                    MyArrayList<MaintenanceWorker> choices = controller.reassignWorkerChoices(cid);
                    if (choices.size() == 0) {
                        tError("No other workers of the correct type are available.");
                        tPause();
                        continue;
                    }

                    String[] workerLabels = new String[choices.size()];
                    for (int i = 0; i < choices.size(); i++) {
                        MaintenanceWorker w = choices.get(i);
                        workerLabels[i] = String.format("%-5s%-10s | %-20s | %s",
                                "[" + (i + 1) + "]",
                                w.getId(),
                                w.getName(),
                                w.getField().name());
                    }

                    int widx;
                    try { widx = tArrowSelect("AVAILABLE WORKERS", workerLabels); }
                    catch (InterruptedException e) { continue; }
                    if (widx < 0 || widx >= choices.size()) continue;

                    String wid = choices.get(widx).getId();

                    boolean ok = module.reassignComplaint(cid, wid);
                    if (ok) {
                        tBoxTop();
                        tBoxLine("Reassigned successfully.");
                        tBoxBottom();
                    } else {
                        tError("Failed to reassign.");
                    }
                    tPause();

                } else if (ch == 6) {
                    MyArrayList<Complaint> unresolved = controller.unresolved();
                    if (unresolved.size() == 0) {
                        tBoxTop();
                        tBoxLine("No unresolved complaints found.");
                        tBoxBottom();
                        tPause();
                        continue;
                    }

                    String[] unresolvedLabels = new String[unresolved.size()];
                    for (int i = 0; i < unresolved.size(); i++) {
                        Complaint c = unresolved.get(i);
                        String wid = c.getAssignedWorkerId();
                        if (wid == null || wid.trim().isEmpty()) wid = "(none)";
                        unresolvedLabels[i] = String.format("%-5s%-10s | %s",
                                "[" + (i + 1) + "]",
                                c.getComplaintId(),
                                wid);
                    }

                    int idx;
                    try { idx = tArrowSelect("UNRESOLVED COMPLAINTS", unresolvedLabels); }
                    catch (InterruptedException e) { continue; }
                    if (idx < 0 || idx >= unresolved.size()) continue;

                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyAttendantTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);

                    String cid = unresolved.get(idx).getComplaintId();

                    if (controller.isResolved(cid)) {
                        tError("This complaint is already resolved.");
                        tPause();
                        continue;
                    }

                    tBoxTop();
                    tBoxTitle("RESOLVE COMPLAINT");
                    tBoxSep();
                    tBoxLine("Complaint: " + cid);
                    tBoxSep();
                    tCustomInputRow("Resolution note: ");
                    String note = FastInput.readLine().trim();

                    boolean ok = module.resolveByAttendant(cid, note);
                    if (ok) {
                        tBoxTop();
                        tBoxLine("Resolved successfully.");
                        tBoxBottom();
                    } else {
                        tError("Failed to resolve.");
                    }
                    tPause();

                } else {
                    view.error("Invalid choice.");
                    ConsoleUtil.pause();
                }

            } catch (Exception e) {
                TerminalUI.cleanup();
                System.err.println("[AttendantComplaintCLI] " + e.getMessage());
            }
        }
    }
}



