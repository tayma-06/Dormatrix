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
import java.util.ArrayList;
import java.util.List;

public class WorkerScheduleController {

    public static final String[] SLOT_LABELS = {
            "08-10", "10-12", "12-14", "14-16", "16-18", "18-20"
    };

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
        String workerId = c.getAssignedWorkerId();

        if (workerId == null || workerId.trim().isEmpty()) return MyOptional.empty();
        MyOptional<MaintenanceWorker> workerOpt = workerRepo.findById(workerId);
        if (workerOpt.isEmpty()) return MyOptional.empty();

        MaintenanceWorker worker = workerOpt.get();
        List<DayOfWeek> orderedDays = orderedSuggestionDays(worker);

        for (int i = 0; i < orderedDays.size(); i++) {
            DayOfWeek day = orderedDays.get(i);
            for (int slot = 0; slot < SLOT_LABELS.length; slot++) {
                if (routineController.isBusyForAttendantWindow(c.getStudentId(), day, slot)) continue;
                if (visitRepo.hasConflict(workerId, day, slot, c.getComplaintId())) continue;

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

                visitRepo.upsert(entry);
                c.appendTagNote("VISIT:" + day.name() + ":" + SLOT_LABELS[slot]);
                complaintRepo.update(c);
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

        if (visitRepo.hasConflict(workerId, day, slotIndex, c.getComplaintId())) return false;
        if (routineController.isBusyForAttendantWindow(c.getStudentId(), day, slotIndex)) return false;

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

        visitRepo.upsert(entry);
        c.appendTagNote("VISIT:" + day.name() + ":" + SLOT_LABELS[slotIndex]);
        return complaintRepo.update(c);
    }

    public String renderPendingComplaintList() {
        MyArrayList<Complaint> all = complaintRepo.findAll();
        StringBuilder sb = new StringBuilder();

        sb.append("PENDING / UNRESOLVED COMPLAINTS\n");
        sb.append("--------------------------------------------------------------\n");

        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            Complaint c = all.get(i);
            if (c.getStatus() == ComplaintStatus.RESOLVED) continue;

            found = true;
            String worker = c.getAssignedWorkerId();
            if (worker == null || worker.trim().isEmpty()) worker = "(none)";

            sb.append("Complaint: ").append(c.getComplaintId())
                    .append(" | Room: ").append(c.getStudentRoomNo())
                    .append(" | Category: ").append(c.getCategory().name())
                    .append(" | Worker: ").append(worker)
                    .append(" | Status: ").append(c.getStatus().name())
                    .append("\n");
        }

        if (!found) sb.append("(No unresolved complaints)\n");
        return sb.toString();
    }

    public String renderMaskedRoutineForComplaint(String complaintId) {
        MyOptional<Complaint> cOpt = complaintRepo.findById(complaintId);
        if (cOpt.isEmpty()) return "Complaint not found.";
        Complaint c = cOpt.get();
        return routineController.renderMaskedRoutineForStudent(c.getStudentId());
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
        return visitRepo.markDone(complaintId);
    }

    public boolean isDefaultDutyDay(MaintenanceWorker worker, DayOfWeek day) {
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return true;
        if (worker.getField() == WorkerField.CLEANING &&
                (day == DayOfWeek.TUESDAY || day == DayOfWeek.THURSDAY)) {
            return true;
        }
        return false;
    }

    private List<DayOfWeek> orderedSuggestionDays(MaintenanceWorker worker) {
        DayOfWeek start = TimeManager.nowDay();
        List<DayOfWeek> nextSeven = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            nextSeven.add(start.plus(i));
        }

        List<DayOfWeek> preferred = new ArrayList<>();
        List<DayOfWeek> fallback = new ArrayList<>();

        for (int i = 0; i < nextSeven.size(); i++) {
            DayOfWeek day = nextSeven.get(i);
            if (isDefaultDutyDay(worker, day)) preferred.add(day);
            else fallback.add(day);
        }

        preferred.addAll(fallback);
        return preferred;
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