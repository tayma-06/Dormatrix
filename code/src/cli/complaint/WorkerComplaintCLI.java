package cli.complaint;

import cli.views.complaint.ComplaintView;
import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import models.complaints.Complaint;
import models.enums.ComplaintStatus;
import repo.file.FileComplaintRepository;
import utils.*;
import utils.TerminalUI.MenuItem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static utils.TerminalUI.*;
import static utils.TerminalUIExtras.*;

public class WorkerComplaintCLI {

    private static final String WORKER_FILE = "data/users/maintenance_workers.txt";

    private final ComplaintView view = new ComplaintView();
    private final FileComplaintRepository repo = new FileComplaintRepository();

    private static final MenuItem[] MENU = {
            new MenuItem(1, "Update Progress"),
            new MenuItem(0, "Back"),
    };

    public void start(String workerIdentifier) {
        while (true) {
            try {
                String wid = resolveWorkerId(workerIdentifier);
                if (wid == null) {
                    tError("Could not identify worker.");
                    tPause();
                    return;
                }

                // ── Draw menu first ───────────────────────────────────
                ConsoleUtil.clearScreen();
                BackgroundFiller.applyMaintenanceTheme();
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());

                drawDashboard(
                        "TASK OPTIONS", "Select an action below",
                        MENU,
                        TerminalUI.getActiveTextColor(),
                        TerminalUI.getActiveBoxColor(),
                        null, 3
                );

                int ch = readChoiceArrow();
                if (ch == 0) return;

                // ── Clear before sub-screen ───────────────────────────
                ConsoleUtil.clearScreen();
                BackgroundFiller.applyMaintenanceTheme();
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                TerminalUI.at(2, 1);

                if (ch == 1) {
                    MyArrayList<Complaint> list = repo.findUnresolvedByAssignedWorker(wid);

                    if (list == null || list.size() == 0) {
                        tBoxTop();
                        tBoxTitle("ASSIGNED TASKS");
                        tBoxSep();
                        tBoxLine("No unresolved tasks assigned to you.");
                        tBoxBottom();
                        tPause();
                        continue;
                    }

                    // Show task list then picker
                    view.workerList(list);

                    String[] taskLabels = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        Complaint c = list.get(i);
                        taskLabels[i] = String.format("%-5s%-10s | %-15s | Room %s",
                                "[" + (i + 1) + "]",
                                c.getComplaintId(),
                                c.getCategory().name(),
                                c.getStudentRoomNo());
                    }

                    int idx;
                    try { idx = tArrowSelect("SELECT COMPLAINT TO UPDATE", taskLabels); }
                    catch (InterruptedException e) { continue; }
                    if (idx < 0) continue;

                    Complaint selected = list.get(idx);

                    if (selected.getStatus().equals(ComplaintStatus.RESOLVED)) {
                        tError("Cannot update a RESOLVED complaint.");
                        tPause();
                        continue;
                    }

                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyMaintenanceTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);

                    tBoxTop();
                    tBoxTitle("UPDATE PROGRESS");
                    tBoxSep();
                    tBoxLine("Complaint : " + selected.getComplaintId());
                    tBoxLine("Category  : " + selected.getCategory().name());
                    tBoxLine("Room      : " + selected.getStudentRoomNo());
                    tBoxSep();
                    tBoxLine("  [ESC] Cancel and go back", ConsoleColors.fgRGB(160, 150, 60));
                    tBoxSep();
                    tCustomInputRow("Progress Note : ");
                    String note = readLineOrEsc();
                    if (note == null) continue;

                    if (note.isEmpty()) {
                        tError("Note cannot be empty.");
                        tPause();
                        continue;
                    }

                    update(wid, selected.getComplaintId(), note);
                    tPause();
                }

            } catch (Exception e) {
                TerminalUI.cleanup();
                System.err.println("[WorkerComplaintCLI] " + e.getMessage());
            }
        }
    }

    private void update(String workerId, String complaintId, String note) {
        MyOptional<Complaint> cOpt = repo.findById(complaintId);
        if (cOpt.isEmpty()) { tError("Invalid complaint ID."); return; }

        Complaint c = cOpt.get();

        if (c.getAssignedWorkerId() == null ||
                !c.getAssignedWorkerId().trim().equals(workerId.trim())) {
            tError("You are not assigned to this complaint.");
            return;
        }

        c.setStatus(ComplaintStatus.IN_PROGRESS);
        c.appendTagNote("WORKER_PROGRESS:" + (note == null ? "" : note));
        boolean ok = repo.update(c);
        if (ok) new repo.file.FileWorkerVisitRepository().markDone(complaintId);

        if (ok) { tBoxTop(); tBoxLine("Updated successfully."); tBoxBottom(); }
        else tError("Failed to update.");
    }

    private int getCursorRow() {
        // Estimate based on task list size — tBoxTop/Sep/Bottom take rows
        return 25;
    }

    private String resolveWorkerId(String target) {
        try (BufferedReader br = new BufferedReader(new FileReader(WORKER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length < 2) continue;
                String id   = parts[0].trim().replace("\uFEFF", "");
                String name = parts[1].trim();
                if (id.equals(target.trim()) || name.equalsIgnoreCase(target.trim())) return id;
            }
        } catch (IOException e) { return null; }
        return null;
    }
}