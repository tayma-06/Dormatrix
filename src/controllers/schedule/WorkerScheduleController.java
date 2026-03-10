package controllers.schedule;

import controllers.routine.RoutineController;
import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import models.complaints.Complaint;
import models.enums.ComplaintStatus;
import models.enums.WorkerField;
import models.schedule.WorkerVisitEntry;
import models.users.MaintenanceWorker;
import repo.file.FileComplaintRepository;
import repo.file.FileMaintenanceWorkerRepository;
import repo.file.FileWorkerVisitRepository;
import utils.TimeManager;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;

public class WorkerScheduleController {

    public static final String[] SLOT_LABELS = {
            "08-10", "10-12", "12-14", "14-16", "16-18", "18-20"
    };

    private static final int AUTO_SLOT_LATE_1 = 4;   // 16-18
    private static final int AUTO_SLOT_LATE_2 = 5;   // 18-20

    private static final int TIME_COL_WIDTH = 10;
    private static final int CELL_WIDTH = 12;

    private final FileComplaintRepository complaintRepo = new FileComplaintRepository();
    private final FileMaintenanceWorkerRepository workerRepo = new FileMaintenanceWorkerRepository();
    private final FileWorkerVisitRepository visitRepo = new FileWorkerVisitRepository();
    private final RoutineController routineController = new RoutineController();

    public MyOptional<WorkerVisitEntry> autoPlanComplaint(String complaintId) {
        MyOptional<Complaint> cOpt = complaintRepo.findById(complaintId);
        if (cOpt.isEmpty()) return MyOptional.empty();

        Complaint c = cOpt.get();
        if (c.getStatus() == ComplaintStatus.RESOLVED) return MyOptional.empty();

        String workerId = c.getAssignedWorkerId();
        if (workerId == null || workerId.trim().isEmpty()) return MyOptional.empty();
        if (workerRepo.findById(workerId).isEmpty()) return MyOptional.empty();

        WorkerVisitEntry previousVisit = null;
        MyOptional<WorkerVisitEntry> oldVisitOpt = visitRepo.findByComplaintId(complaintId);
        if (oldVisitOpt.isPresent()) previousVisit = oldVisitOpt.get();

        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            DayOfWeek day = TimeManager.nowDay().plus(dayOffset);
            int[] candidateSlots = autoCandidateSlots(dayOffset);

            for (int i = 0; i < candidateSlots.length; i++) {
                int slot = candidateSlots[i];

                if (visitRepo.hasConflict(workerId, day, slot, c.getComplaintId())) continue;
                if (routineController.isBusyForAttendantWindowExceptComplaint(
                        c.getStudentId(), day, slot, c.getComplaintId())) continue;

                WorkerVisitEntry entry = new WorkerVisitEntry(
                        c.getComplaintId(),
                        workerId,
                        c.getStudentId(),
                        c.getStudentRoomNo(),
                        day,
                        slot,
                        "PLANNED",
                        "AUTO"
                );

                saveOrRescheduleVisit(c, previousVisit, entry);
                return MyOptional.of(entry);
            }
        }

        return MyOptional.empty();
    }

    public boolean manualPlanComplaint(String complaintId, DayOfWeek day, int slotIndex, String note) {
        if (slotIndex < 0 || slotIndex >= SLOT_LABELS.length) return false;

        MyOptional<Complaint> cOpt = complaintRepo.findById(complaintId);
        if (cOpt.isEmpty()) return false;

        Complaint c = cOpt.get();
        if (c.getStatus() == ComplaintStatus.RESOLVED) return false;

        String workerId = c.getAssignedWorkerId();
        if (workerId == null || workerId.trim().isEmpty()) return false;

//        if (day == TimeManager.nowDay() && isSlotAlreadyStartedToday(slotIndex)) return false;
        if (visitRepo.hasConflict(workerId, day, slotIndex, c.getComplaintId())) return false;
        if (routineController.isBusyForAttendantWindowExceptComplaint(
                c.getStudentId(), day, slotIndex, c.getComplaintId())) return false;

        WorkerVisitEntry previousVisit = null;
        MyOptional<WorkerVisitEntry> oldVisitOpt = visitRepo.findByComplaintId(complaintId);
        if (oldVisitOpt.isPresent()) previousVisit = oldVisitOpt.get();

        WorkerVisitEntry entry = new WorkerVisitEntry(
                c.getComplaintId(),
                workerId,
                c.getStudentId(),
                c.getStudentRoomNo(),
                day,
                slotIndex,
                "PLANNED",
                note == null ? "" : note.trim()
        );

        saveOrRescheduleVisit(c, previousVisit, entry);
        return true;
    }

    public String renderPendingComplaintList() {
        MyArrayList<Complaint> all = complaintRepo.findAll();
        StringBuilder sb = new StringBuilder();

        // Column widths
        int idW     = 12;
        int roomW   = 6;
        int catW    = 16;
        int workerW = 12;
        int statusW = 14;

        // Row segment builder helper
        // top, mid (header divider), inner (row divider), bottom
        String top = "╔" + "═".repeat(idW+2) + "╦" + "═".repeat(roomW+2) + "╦" + "═".repeat(catW+2)
                + "╦" + "═".repeat(workerW+2) + "╦" + "═".repeat(statusW+2) + "╗\n";
        String mid = "╠" + "═".repeat(idW+2) + "╬" + "═".repeat(roomW+2) + "╬" + "═".repeat(catW+2)
                + "╬" + "═".repeat(workerW+2) + "╬" + "═".repeat(statusW+2) + "╣\n";
        String bot = "╚" + "═".repeat(idW+2) + "╩" + "═".repeat(roomW+2) + "╩" + "═".repeat(catW+2)
                + "╩" + "═".repeat(workerW+2) + "╩" + "═".repeat(statusW+2) + "╝\n";

        sb.append("PENDING / UNRESOLVED COMPLAINTS\n");
        sb.append(top);
        sb.append(String.format("║ %-"+idW+"s ║ %-"+roomW+"s ║ %-"+catW+"s ║ %-"+workerW+"s ║ %-"+statusW+"s ║\n",
                "Complaint ID", "Room", "Category", "Worker", "Status"));
        sb.append(mid);

        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            Complaint c = all.get(i);
            if (c.getStatus() == ComplaintStatus.RESOLVED) continue;

            found = true;
            String worker = c.getAssignedWorkerId();
            if (worker == null || worker.trim().isEmpty()) worker = "(none)";

            sb.append(String.format("║ %-"+idW+"s ║ %-"+roomW+"s ║ %-"+catW+"s ║ %-"+workerW+"s ║ %-"+statusW+"s ║\n",
                    truncate(c.getComplaintId(),    idW),
                    truncate(c.getStudentRoomNo(),  roomW),
                    truncate(c.getCategory().name(),catW),
                    truncate(worker,                workerW),
                    truncate(c.getStatus().name(),  statusW)));
        }

        if (!found) {
            int totalWidth = idW + roomW + catW + workerW + statusW + 14;
            sb.append(String.format("║ %-"+totalWidth+"s ║\n", "(No unresolved complaints)"));
        }

        sb.append(bot);
        return sb.toString();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    public String renderMaskedRoutineForComplaint(String complaintId) {
        MyOptional<Complaint> cOpt = complaintRepo.findById(complaintId);
        if (cOpt.isEmpty()) return "Complaint not found.";

        Complaint c = cOpt.get();
        StringBuilder sb = new StringBuilder();
        sb.append("Complaint ID : ").append(c.getComplaintId()).append("\n");
        sb.append("Category     : ").append(c.getCategory().name()).append("\n");
        sb.append("Room         : ").append(c.getStudentRoomNo()).append("\n");
        sb.append("Worker       : ")
                .append(c.getAssignedWorkerId() == null || c.getAssignedWorkerId().trim().isEmpty()
                        ? "(not assigned)"
                        : c.getAssignedWorkerId())
                .append("\n\n");
        sb.append(routineController.renderMaskedRoutineForStudent(c.getStudentId()));
        return sb.toString();
    }

    public String renderWorkerWeek(String workerToken) {
        MyOptional<MaintenanceWorker> workerOpt = resolveWorker(workerToken);
        if (workerOpt.isEmpty()) return "Could not resolve worker.";

        MaintenanceWorker worker = workerOpt.get();
        String[][] cells = new String[SLOT_LABELS.length][7];

        for (int r = 0; r < SLOT_LABELS.length; r++) {
            for (int c = 0; c < 7; c++) {
                DayOfWeek day = columnToDay(c);
                cells[r][c] = isDefaultDutyDay(worker, day) ? "DUTY" : "";
            }
        }

        MyArrayList<WorkerVisitEntry> visits = visitRepo.findByWorkerId(worker.getId());
        for (int i = 0; i < visits.size(); i++) {
            WorkerVisitEntry v = visits.get(i);
            if (!"PLANNED".equalsIgnoreCase(v.getStatus())) continue;

            int row = v.getSlotIndex();
            int col = dayToColumn(v.getDay());

            if (row >= 0 && row < SLOT_LABELS.length && col >= 0 && col < 7) {
                String label = v.getRoomNo();
                if (label == null || label.trim().isEmpty()) label = "VISIT";
                cells[row][col] = "RM " + label;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("WORKER WEEKLY SCHEDULE - ").append(worker.getName())
                .append(" (").append(worker.getField().name()).append(")\n");
        sb.append(buildBorder("┌", "┬", "┐")).append("\n");
        sb.append("│").append(center("TIME", TIME_COL_WIDTH));
        for (int i = 0; i < 7; i++) {
            DayOfWeek day = columnToDay(i);
            String label = dayShort(day);
            if (day == TimeManager.nowDay()) label = "*" + label + "*";
            sb.append("│").append(center(label, CELL_WIDTH));
        }
        sb.append("│\n");
        sb.append(buildBorder("├", "┼", "┤")).append("\n");

        for (int r = 0; r < SLOT_LABELS.length; r++) {
            sb.append("│").append(center(SLOT_LABELS[r], TIME_COL_WIDTH));
            for (int c = 0; c < 7; c++) {
                sb.append("│").append(pad(clip(cells[r][c], CELL_WIDTH - 2), CELL_WIDTH));
            }
            sb.append("│\n");
            if (r < SLOT_LABELS.length - 1) sb.append(buildBorder("├", "┼", "┤")).append("\n");
        }

        sb.append(buildBorder("└", "┴", "┘")).append("\n");
        sb.append("Legend: DUTY = default on-campus duty. RM xxx = scheduled complaint visit.\n");

        sb.append("Planned visits:\n");
        boolean hasPlanned = false;
        for (int i = 0; i < visits.size(); i++) {
            WorkerVisitEntry v = visits.get(i);
            if (!"PLANNED".equalsIgnoreCase(v.getStatus())) continue;
            hasPlanned = true;
            sb.append("- ")
                    .append(v.getDay().name())
                    .append(" ")
                    .append(SLOT_LABELS[v.getSlotIndex()])
                    .append(" | Complaint ")
                    .append(v.getComplaintId())
                    .append(" | Room ")
                    .append(v.getRoomNo())
                    .append("\n");
        }
        if (!hasPlanned) sb.append("- No planned visits\n");

        return sb.toString();
    }

    public boolean markVisitDone(String complaintId) {
        MyOptional<WorkerVisitEntry> visitOpt = visitRepo.findByComplaintId(complaintId);
        boolean ok = visitRepo.markDone(complaintId);

        if (ok && visitOpt.isPresent()) {
            WorkerVisitEntry v = visitOpt.get();
            routineController.clearComplaintVisitIfPresent(
                    v.getStudentId(), v.getDay(), v.getSlotIndex(), v.getComplaintId()
            );
        }

        return ok;
    }

    public boolean isDefaultDutyDay(MaintenanceWorker worker, DayOfWeek day) {
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return true;
        if (worker.getField() == WorkerField.CLEANING &&
                (day == DayOfWeek.TUESDAY || day == DayOfWeek.THURSDAY)) {
            return true;
        }
        return false;
    }

    private void saveOrRescheduleVisit(Complaint complaint,
                                       WorkerVisitEntry previousVisit,
                                       WorkerVisitEntry newVisit) {
        if (previousVisit != null) {
            routineController.clearComplaintVisitIfPresent(
                    previousVisit.getStudentId(),
                    previousVisit.getDay(),
                    previousVisit.getSlotIndex(),
                    previousVisit.getComplaintId()
            );
        }

        visitRepo.upsert(newVisit);

        routineController.writeComplaintVisit(
                complaint.getStudentId(),
                newVisit.getDay(),
                newVisit.getSlotIndex(),
                complaint.getComplaintId(),
                "Complaint Visit"
        );

        complaint.appendTagNote("VISIT:" + newVisit.getDay().name() + ":" + SLOT_LABELS[newVisit.getSlotIndex()]);
        complaintRepo.update(complaint);
    }

    private int[] autoCandidateSlots(int dayOffset) {
        if (dayOffset > 0) {
            return new int[]{AUTO_SLOT_LATE_1, AUTO_SLOT_LATE_2};
        }

        LocalTime now = TimeManager.nowTime();

        if (now.isBefore(LocalTime.of(16, 0))) {
            return new int[]{AUTO_SLOT_LATE_1, AUTO_SLOT_LATE_2};
        }

        if (!now.isAfter(LocalTime.of(18, 0))) {
            return new int[]{AUTO_SLOT_LATE_2};
        }

        return new int[0];
    }

    private boolean isSlotAlreadyStartedToday(int slotIndex) {
        LocalTime now = TimeManager.nowTime();
        LocalTime slotStart;

        switch (slotIndex) {
            case 0: slotStart = LocalTime.of(8, 0); break;
            case 1: slotStart = LocalTime.of(10, 0); break;
            case 2: slotStart = LocalTime.of(12, 0); break;
            case 3: slotStart = LocalTime.of(14, 0); break;
            case 4: slotStart = LocalTime.of(16, 0); break;
            default: slotStart = LocalTime.of(18, 0); break;
        }

        return now.isAfter(slotStart);
    }

    private MyOptional<MaintenanceWorker> resolveWorker(String workerToken) {
        if (workerToken == null || workerToken.trim().isEmpty()) return MyOptional.empty();

        MyArrayList<MaintenanceWorker> all = workerRepo.findAll();

        for (int i = 0; i < all.size(); i++) {
            MaintenanceWorker w = all.get(i);
            if (same(w.getId(), workerToken)) return MyOptional.of(w);
        }

        for (int i = 0; i < all.size(); i++) {
            MaintenanceWorker w = all.get(i);
            if (w.getName() != null && w.getName().trim().equalsIgnoreCase(workerToken.trim())) {
                return MyOptional.of(w);
            }
        }

        return MyOptional.empty();
    }

    public String[] renderAllWorkerIds()
    {
        MyArrayList<MaintenanceWorker> all = workerRepo.findAll();
        StringBuilder sb = new StringBuilder();
        ArrayList<String> workerIds = new ArrayList<>();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            MaintenanceWorker w = all.get(i);
            found = true;
            workerIds.add(w.getId());
        }
        String[] s = workerIds.toArray(String[]::new);
        if (!found) sb.append("(No worker listed)\n");
        return s;
    }


    public String[] renderUnresolvedComplaintIdsOnly() {
        MyArrayList<Complaint> all = complaintRepo.findAll();
        StringBuilder sb = new StringBuilder();

//        sb.append("UNRESOLVED COMPLAINT IDS\n");
//        sb.append("------------------------\n");

        ArrayList<String> ids = new ArrayList<>();
        boolean found = false;
        int j = 0;
        for (int i = 0; i < all.size(); i++) {
            Complaint c = all.get(i);
            if (c.getStatus() == ComplaintStatus.RESOLVED) continue;

            found = true;
            j++;
            ids.add(c.getComplaintId());
//            sb.append("["+(i+1)+"]").append(c.getComplaintId()).append("\t");
        }

        String[] s = ids.toArray(String[]::new);
        if (!found) sb.append("(No unresolved complaints)\n");
        return s;
    }

    public boolean complaintExists(String complaintId) {
        return complaintRepo.findById(complaintId).isPresent();
    }

    public boolean isResolvedComplaint(String complaintId) {
        MyOptional<Complaint> cOpt = complaintRepo.findById(complaintId);
        if (cOpt.isEmpty()) return false;
        return cOpt.get().getStatus() == ComplaintStatus.RESOLVED;
    }

    private int dayToColumn(DayOfWeek day) {
        switch (day) {
            case MONDAY: return 0;
            case TUESDAY: return 1;
            case WEDNESDAY: return 2;
            case THURSDAY: return 3;
            case FRIDAY: return 4;
            case SATURDAY: return 5;
            default: return 6;
        }
    }

    private DayOfWeek columnToDay(int column) {
        switch (column) {
            case 0: return DayOfWeek.MONDAY;
            case 1: return DayOfWeek.TUESDAY;
            case 2: return DayOfWeek.WEDNESDAY;
            case 3: return DayOfWeek.THURSDAY;
            case 4: return DayOfWeek.FRIDAY;
            case 5: return DayOfWeek.SATURDAY;
            default: return DayOfWeek.SUNDAY;
        }
    }

    private String dayShort(DayOfWeek day) {
        switch (day) {
            case MONDAY: return "MON";
            case TUESDAY: return "TUE";
            case WEDNESDAY: return "WED";
            case THURSDAY: return "THU";
            case FRIDAY: return "FRI";
            case SATURDAY: return "SAT";
            default: return "SUN";
        }
    }

    private String buildBorder(String left, String mid, String right) {
        StringBuilder sb = new StringBuilder();
        sb.append(left).append(repeat("─", TIME_COL_WIDTH));
        for (int i = 0; i < 7; i++) {
            sb.append(mid).append(repeat("─", CELL_WIDTH));
        }
        sb.append(right);
        return sb.toString();
    }

    private String clip(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        if (max <= 1) return s.substring(0, max);
        return s.substring(0, max - 1) + "…";
    }

    private String center(String s, int width) {
        if (s == null) s = "";
        if (s.length() > width) return s.substring(0, width);

        int left = (width - s.length()) / 2;
        int right = width - s.length() - left;
        return repeat(" ", left) + s + repeat(" ", right);
    }

    private String pad(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        return s + repeat(" ", width - s.length());
    }

    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }

    private boolean same(String a, String b) {
        if (a == null || b == null) return a == b;
        return a.trim().equalsIgnoreCase(b.trim());
    }
}
