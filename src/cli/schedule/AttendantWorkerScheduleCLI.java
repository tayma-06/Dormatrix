package cli.schedule;

import cli.Input;
import cli.forms.complaint.ComplaintForm;
import cli.views.complaint.ComplaintView;
import controllers.schedule.WorkerScheduleController;
import libraries.collections.MyOptional;
import models.schedule.WorkerVisitEntry;
import utils.BackgroundFiller;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;
import static utils.TerminalUI.*;
import static utils.TerminalUIExtras.*;

import java.time.DayOfWeek;
import java.util.Scanner;

import static utils.TerminalUI.tSubDashboard;

public class AttendantWorkerScheduleCLI {

    private final WorkerScheduleController controller = new WorkerScheduleController();
    private final Scanner sc = Input.SC;
    private final ComplaintView view = new ComplaintView();
    private final ComplaintForm form = new ComplaintForm(Input.SC);

    private static final MenuItem[] MENU = {
            new MenuItem(1, "View unresolved complaints"),
            new MenuItem(2, "Preview student routine from complaint"),
            new MenuItem(3, "Auto-schedule a complaint visit"),
            new MenuItem(4, "Manually schedule a complaint visit"),
            new MenuItem(5, "View worker weekly schedule"),
            new MenuItem(0, "Back"),
    };
    public void show(String username) {
        while (true) {
            try {
                ConsoleUtil.clearScreen();
                BackgroundFiller.applyAttendantTheme();
                System.out.print(HIDE_CUR);

                TerminalUI.at(3, 1);

                int menuStartRow = 4;
                int promptRow = drawDashboard(
                        "WORKER SCHEDULING",
                        "Attendant: " + username,
                        MENU,
                        TerminalUI.getActiveTextColor(),
                        TerminalUI.getActiveBoxColor(),
                        null,
                        menuStartRow
                );

                System.out.print(SHOW_CUR);
                int choice = readChoiceArrow();


                switch (choice) {
                    case 0 -> { return; }
                    case 1 -> handleViewUnresolvedComplaints();
                    case 2 -> handlePreviewStudentRoutine();
                    case 3 -> handleAutoScheduleComplaint();
                    case 4 -> handleManualScheduleComplaint();
                    case 5 -> handleViewWorkerSchedule();
                    default -> {
                        tError("Invalid choice.");
                        tPause();
                    }
                }

            } catch (Exception e) {
                cleanup();
                System.err.println("[AttendantWorkerScheduleCLI] " + e.getMessage());
                tPause();
            }

//
        }
    }

    private void handleViewUnresolvedComplaints() {
        clearAndRefresh();
        System.out.println(controller.renderPendingComplaintList());
        tPause();
    }

    /**
     * Shows an arrow-key picker of unresolved complaints, then displays
     * the masked student routine for the selected one.
     */
    private void handlePreviewStudentRoutine() {
        clearAndRefresh();
        String cid = pickComplaint();
        if (cid == null) return;

        clearAndRefresh();
        System.out.println(controller.renderMaskedRoutineForComplaint(cid));
        tPause();
    }

//    private void handleAutoScheduleComplaint() {
//        clearAndRefresh();
//        tSubDashboard("UNRESOLVED COMPLAINT IDs", controller.renderUnresolvedComplaintIdsOnly());
//        String cid = FastInput.readNonEmptyLine();
//        if (!validateComplaint(cid)) return;
//
//        MyOptional<WorkerVisitEntry> visitOpt = controller.autoPlanComplaint(cid);
//        if (visitOpt.isPresent()) {
//            WorkerVisitEntry v = visitOpt.get();
//
//            String result = new StringBuilder()
//                    .append("Scheduled: ").append(v.getDay().name())
//                    .append(" ").append(WorkerScheduleController.SLOT_LABELS[v.getSlotIndex()])
//                    .append(" | Worker ").append(v.getWorkerId())
//                    .append(" | Room ").append(v.getRoomNo())
//                    .toString();
////            System.out.println(result);
//
//            TerminalUI.at(17, 1);
//            TerminalUI.tBoxTop();
//            TerminalUI.tBoxLine(result);
//            TerminalUI.tBoxBottom();
//
////            System.out.println("Scheduled: " + v.getDay().name() + " " +
////                    WorkerScheduleController.SLOT_LABELS[v.getSlotIndex()] +
////                    " | Worker " + v.getWorkerId() +
////                    " | Room " + v.getRoomNo());
//            tPause();
//        } else {
//            System.out.println("Could not auto-schedule. The complaint may be unassigned or every suitable slot is busy.");
//            tPause();
//        }
//    }

    /**
     * Shows an arrow-key picker, then auto-schedules the selected complaint.
     */
    private void handleAutoScheduleComplaint() {
        clearAndRefresh();
        String cid = pickComplaint();
        if (cid == null) return;

        MyOptional<WorkerVisitEntry> visitOpt = controller.autoPlanComplaint(cid);
        clearAndRefresh();

        if (visitOpt.isPresent()) {
            WorkerVisitEntry v = visitOpt.get();
            String result = "Scheduled: " + v.getDay().name()
                    + "  " + WorkerScheduleController.SLOT_LABELS[v.getSlotIndex()]
                    + "  |  Worker " + v.getWorkerId()
                    + "  |  Room "   + v.getRoomNo();
            TerminalUI.tBoxTop();
            TerminalUI.tBoxLine(result);
            TerminalUI.tBoxBottom();
        } else {
            tError("Could not auto-schedule. Complaint may be unassigned or all slots are busy.");
        }
        tPause();
    }


//    private void handleManualScheduleComplaint() {
//        clearAndRefresh();
//        tSubDashboard("UNRESOLVED COMPLAINT IDs", controller.renderUnresolvedComplaintIdsOnly());
//        String cid = FastInput.readNonEmptyLine();
//        if (!validateComplaint(cid)) return;
//
//        System.out.println(controller.renderMaskedRoutineForComplaint(cid));
//        DayOfWeek day = readDay();
//        int slot = readSlot();
//        String note = readLine("Note (optional): ");
//        boolean ok = controller.manualPlanComplaint(cid, day, slot, note);
//        System.out.println(ok ? "Visit scheduled." : "Could not schedule. Student may be busy or worker already has a conflict.");
//        tPause();
//    }


    /**
     * Shows an arrow-key picker, displays the student routine, then lets the
     * attendant pick a day/slot manually.
     */
    private void handleManualScheduleComplaint() {
        clearAndRefresh();
        String cid = pickComplaint();
        if (cid == null) return;

        clearAndRefresh();
        System.out.println(controller.renderMaskedRoutineForComplaint(cid));

        DayOfWeek day  = readDay();
        int slot       = readSlot();
        String note    = readLine("Note (optional): ");

        boolean ok = controller.manualPlanComplaint(cid, day, slot, note);
        if (ok) {
            tBoxTop();
            tBoxLine("Visit scheduled successfully.");
            tBoxBottom();
        } else {
            tError("Could not schedule. Student may be busy or worker has a conflict.");
        }
        tPause();
    }



//    private void handleViewWorkerSchedule() {
//        clearAndRefresh();
//        tSubDashboard("WORKER IDs", controller.renderAllWorkerIds());
//        String workerToken = readNonEmpty("");
//        System.out.println(controller.renderWorkerWeek(workerToken));
//
//        tPause();
//    }

    private void handleViewWorkerSchedule() {
        clearAndRefresh();
        String[] workerIds = controller.renderAllWorkerIds();

        // Add numbers to labels
        String[] numbered = new String[workerIds.length];
        for (int i = 0; i < workerIds.length; i++) {
            numbered[i] = String.format("%-4s %s", (i + 1) + ".", workerIds[i]);
        }

        int idx;
        try {
            idx = tArrowSelect("WORKER IDs", numbered);
        } catch (InterruptedException e) {
            return;
        }
        if (idx < 0 || idx >= workerIds.length) return;

        clearAndRefresh();
        System.out.println(controller.renderWorkerWeek(workerIds[idx]));
        tPause();
    }


    // ── Arrow-key complaint picker ────────────────────────────────

    /**
     * Presents an interactive arrow-key list of unresolved complaints.
     *
     * Title:  "UNRESOLVED COMPLAINTS"   (no "IDs")
     * Rows:   "1.  C-3  |  ELECTRICAL  |  Room 101"
     * Prompt: "Your choice no: "  (shown in the hint bar)
     *
     * @return the selected complaint ID string, or null if cancelled / invalid.
     */
    private String pickComplaint() {
        String[] labels = controller.getUnresolvedComplaintLabels();
        String[] ids    = controller.renderUnresolvedComplaintIdsOnly();

        if (ids.length == 0) {
            clearAndRefresh();
            tBoxTop();
            tBoxTitle("UNRESOLVED COMPLAINTS");
            tBoxSep();
            tBoxLine("No unresolved complaints found.");
            tBoxBottom();
            tPause();
            return null;
        }

        int idx;
        try {
            idx = tArrowSelect("UNRESOLVED COMPLAINTS", labels);
        } catch (InterruptedException e) {
            return null;
        }

        if (idx < 0 || idx >= ids.length) return null;

        String cid = ids[idx];

        // Validate after selection (defensive — list should only contain valid IDs)
        if (!controller.complaintExists(cid)) {
            tError("Invalid complaint ID.");
            tPause();
            return null;
        }
        if (controller.isResolvedComplaint(cid)) {
            tError("This complaint is already resolved.");
            tPause();
            return null;
        }

        return cid;
    }


    private boolean validateComplaint(String cid) {
        if (!controller.complaintExists(cid)) {
            view.error("Invalid complaint ID.");
            ConsoleUtil.pause();
            return false;
        }
        if (controller.isResolvedComplaint(cid)) {
            view.error("This complaint is already resolved.");
            ConsoleUtil.pause();
            return false;
        }
        return true;
    }

    private int readInt() {
        while (true) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;
            try {
                return Integer.parseInt(line);
            } catch (Exception e) {
                System.out.print("Invalid number. Enter again: ");
            }
        }
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (!line.isEmpty()) return line;
            System.out.println("Input can not be empty.");
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private DayOfWeek readDay() {
        while (true) {
            System.out.println("Choose day:");
            System.out.println("[1] Monday  [2] Tuesday  [3] Wednesday  [4] Thursday");
            System.out.println("[5] Friday  [6] Saturday [7] Sunday");
            System.out.print("Day: ");
            int x = readInt();

            switch (x) {
                case 1: return DayOfWeek.MONDAY;
                case 2: return DayOfWeek.TUESDAY;
                case 3: return DayOfWeek.WEDNESDAY;
                case 4: return DayOfWeek.THURSDAY;
                case 5: return DayOfWeek.FRIDAY;
                case 6: return DayOfWeek.SATURDAY;
                case 7: return DayOfWeek.SUNDAY;
                default: System.out.println("Invalid day.");
            }
        }
    }

    private int readSlot() {
        while (true) {
            System.out.println("Choose work slot:");
            for (int i = 0; i < WorkerScheduleController.SLOT_LABELS.length; i++) {
                System.out.println("[" + (i + 1) + "] " + WorkerScheduleController.SLOT_LABELS[i]);
            }
            System.out.print("Slot: ");
            int x = readInt();

            if (x >= 1 && x <= WorkerScheduleController.SLOT_LABELS.length) {
                return x - 1;
            }
            System.out.println("Invalid slot.");
        }
    }


    private void clearAndRefresh() {
        ConsoleUtil.clearScreen();
        BackgroundFiller.applyAttendantTheme();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);
    }
}