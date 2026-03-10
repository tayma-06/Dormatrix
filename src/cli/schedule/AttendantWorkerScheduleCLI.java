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
                int choice = FastInput.readInt();


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

//            System.out.println();
//            System.out.println("╔════════════════════════════════════════════════════╗");
//            System.out.println("║            WORKER SCHEDULING (ATTENDANT)           ║");
//            System.out.println("╠════════════════════════════════════════════════════╣");
//            System.out.println("║ [1] View unresolved complaints                     ║");
//            System.out.println("║ [2] Preview student routine from complaint         ║");
//            System.out.println("║ [3] Auto-schedule a complaint visit                ║");
//            System.out.println("║ [4] Manually schedule a complaint visit            ║");
//            System.out.println("║ [5] View worker weekly schedule                    ║");
//            System.out.println("║ [0] Back                                           ║");
//            System.out.println("╚════════════════════════════════════════════════════╝");
//            System.out.print("Enter choice:                            ");

//            int ch = readInt();
//            if (ch == 0) return;
//
//            if (ch == 1) {
//                clearAndRefresh();
//                System.out.println(controller.renderPendingComplaintList());
//            } else if (ch == 2) {
//                clearAndRefresh();
//                tSubDashboard("UNRESOLVED COMPLAINT IDs", controller.renderUnresolvedComplaintIdsOnly() );
////                view.msg(controller.renderUnresolvedComplaintIdsOnly());
//
//                String cid = FastInput.readNonEmptyLine();
//
//                if (!controller.complaintExists(cid)) {
//                    view.error("Invalid complaint ID.");
//                    ConsoleUtil.pause();
//                    continue;
//                }
//
//                if (controller.isResolvedComplaint(cid)) {
//                    view.error("This complaint is already resolved.");
//                    ConsoleUtil.pause();
//                    continue;
//                }
//                System.out.println(controller.renderMaskedRoutineForComplaint(cid));
//            } else if (ch == 3) {
//                clearAndRefresh();
//                tSubDashboard("UNRESOLVED COMPLAINT IDs", controller.renderUnresolvedComplaintIdsOnly() );
//
//                String cid = FastInput.readNonEmptyLine();
//
//                if (!controller.complaintExists(cid)) {
//                    view.error("Invalid complaint ID.");
//                    ConsoleUtil.pause();
//                    continue;
//                }
//
//                if (controller.isResolvedComplaint(cid)) {
//                    view.error("This complaint is already resolved.");
//                    ConsoleUtil.pause();
//                    continue;
//                }
//
//                MyOptional<WorkerVisitEntry> visitOpt = controller.autoPlanComplaint(cid);
//                if (visitOpt.isPresent()) {
//                    WorkerVisitEntry v = visitOpt.get();
//                    System.out.println("Scheduled: " + v.getDay().name() + " " +
//                            WorkerScheduleController.SLOT_LABELS[v.getSlotIndex()] +
//                            " | Worker " + v.getWorkerId() +
//                            " | Room " + v.getRoomNo());
//                } else {
//                    System.out.println("Could not auto-schedule. The complaint may be unassigned or every suitable slot is busy.");
//                }
//            } else if (ch == 4) {
//                clearAndRefresh();
//                tSubDashboard("UNRESOLVED COMPLAINT IDs", controller.renderUnresolvedComplaintIdsOnly() );
//
//                String cid = FastInput.readNonEmptyLine();
//
//                if (!controller.complaintExists(cid)) {
//                    view.error("Invalid complaint ID.");
//                    ConsoleUtil.pause();
//                    continue;
//                }
//
//                if (controller.isResolvedComplaint(cid)) {
//                    view.error("This complaint is already resolved.");
//                    ConsoleUtil.pause();
//                    continue;
//                }
//
//
//                System.out.println(controller.renderMaskedRoutineForComplaint(cid));
//                DayOfWeek day = readDay();
//                int slot = readSlot();
//                String note = readLine("Note (optional): ");
//                boolean ok = controller.manualPlanComplaint(cid, day, slot, note);
//                System.out.println(ok ? "Visit scheduled." : "Could not schedule. Student may be busy or worker already has a conflict.");
//            } else if (ch == 5) {
//                clearAndRefresh();
//                tSubDashboard("WORKER IDs", controller.renderAllWorkerIds());
//                String workerToken = readNonEmpty("Enter worker ID or name: ");
//                System.out.println(controller.renderWorkerWeek(workerToken));
//            } else {
//                System.out.println("Invalid choice.");
//            }
        }
    }

    private void handleViewUnresolvedComplaints() {
        clearAndRefresh();
        System.out.println(controller.renderPendingComplaintList());
        tPause();
    }

    private void handlePreviewStudentRoutine() {
        clearAndRefresh();
        tSubDashboard("UNRESOLVED COMPLAINT IDs", controller.renderUnresolvedComplaintIdsOnly());
        String cid = FastInput.readNonEmptyLine();
        if (!validateComplaint(cid)) return;
        System.out.println(controller.renderMaskedRoutineForComplaint(cid));
        tPause();
    }

    private void handleAutoScheduleComplaint() {
        clearAndRefresh();
        tSubDashboard("UNRESOLVED COMPLAINT IDs", controller.renderUnresolvedComplaintIdsOnly());
        String cid = FastInput.readNonEmptyLine();
        if (!validateComplaint(cid)) return;

        MyOptional<WorkerVisitEntry> visitOpt = controller.autoPlanComplaint(cid);
        if (visitOpt.isPresent()) {
            WorkerVisitEntry v = visitOpt.get();

            String result = new StringBuilder()
                    .append("Scheduled: ").append(v.getDay().name())
                    .append(" ").append(WorkerScheduleController.SLOT_LABELS[v.getSlotIndex()])
                    .append(" | Worker ").append(v.getWorkerId())
                    .append(" | Room ").append(v.getRoomNo())
                    .toString();
//            System.out.println(result);

            TerminalUI.at(17, 1);
            TerminalUI.tBoxTop();
            TerminalUI.tBoxLine(result);
            TerminalUI.tBoxBottom();

//            System.out.println("Scheduled: " + v.getDay().name() + " " +
//                    WorkerScheduleController.SLOT_LABELS[v.getSlotIndex()] +
//                    " | Worker " + v.getWorkerId() +
//                    " | Room " + v.getRoomNo());
            tPause();
        } else {
            System.out.println("Could not auto-schedule. The complaint may be unassigned or every suitable slot is busy.");
            tPause();
        }
    }

    private void handleManualScheduleComplaint() {
        clearAndRefresh();
        tSubDashboard("UNRESOLVED COMPLAINT IDs", controller.renderUnresolvedComplaintIdsOnly());
        String cid = FastInput.readNonEmptyLine();
        if (!validateComplaint(cid)) return;

        System.out.println(controller.renderMaskedRoutineForComplaint(cid));
        DayOfWeek day = readDay();
        int slot = readSlot();
        String note = readLine("Note (optional): ");
        boolean ok = controller.manualPlanComplaint(cid, day, slot, note);
        System.out.println(ok ? "Visit scheduled." : "Could not schedule. Student may be busy or worker already has a conflict.");
        tPause();
    }

    private void handleViewWorkerSchedule() {
        clearAndRefresh();
        tSubDashboard("WORKER IDs", controller.renderAllWorkerIds());
        String workerToken = readNonEmpty("");
        System.out.println(controller.renderWorkerWeek(workerToken));

        tPause();
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