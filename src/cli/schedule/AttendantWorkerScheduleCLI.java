package cli.schedule;

import cli.Input;
import controllers.schedule.WorkerScheduleController;
import libraries.collections.MyOptional;
import models.schedule.WorkerVisitEntry;

import java.time.DayOfWeek;
import java.util.Scanner;

public class AttendantWorkerScheduleCLI {

    private final WorkerScheduleController controller = new WorkerScheduleController();
    private final Scanner sc = Input.SC;

    public void show(String username) {
        while (true) {
            System.out.println();
            System.out.println("══════════════════════════════════════════════");
            System.out.println("        WORKER SCHEDULING (ATTENDANT)");
            System.out.println("══════════════════════════════════════════════");
            System.out.println("1. View unresolved complaints");
            System.out.println("2. Preview student routine from complaint");
            System.out.println("3. Auto-schedule a complaint visit");
            System.out.println("4. Manually schedule a complaint visit");
            System.out.println("5. View worker weekly schedule");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");

            int ch = readInt();
            if (ch == 0) return;

            if (ch == 1) {
                System.out.println(controller.renderPendingComplaintList());
            } else if (ch == 2) {
                String complaintId = readNonEmpty("Complaint ID: ");
                System.out.println(controller.renderMaskedRoutineForComplaint(complaintId));
            } else if (ch == 3) {
                String complaintId = readNonEmpty("Complaint ID: ");
                MyOptional<WorkerVisitEntry> visitOpt = controller.autoPlanComplaint(complaintId);
                if (visitOpt.isPresent()) {
                    WorkerVisitEntry v = visitOpt.get();
                    System.out.println("Scheduled: " + v.getDay().name() + " " +
                            WorkerScheduleController.SLOT_LABELS[v.getSlotIndex()] +
                            " | Worker " + v.getWorkerId() +
                            " | Room " + v.getRoomNo());
                } else {
                    System.out.println("Could not auto-schedule. The complaint may be unassigned or every suitable slot is busy.");
                }
            } else if (ch == 4) {
                String complaintId = readNonEmpty("Complaint ID: ");
                System.out.println(controller.renderMaskedRoutineForComplaint(complaintId));
                DayOfWeek day = readDay();
                int slot = readSlot();
                String note = readLine("Note (optional): ");
                boolean ok = controller.manualPlanComplaint(complaintId, day, slot, note);
                System.out.println(ok ? "Visit scheduled." : "Could not schedule. Student may be busy or worker already has a conflict.");
            } else if (ch == 5) {
                String workerToken = readNonEmpty("Enter worker ID or name: ");
                System.out.println(controller.renderWorkerWeek(workerToken));
            } else {
                System.out.println("Invalid choice.");
            }
        }
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
}