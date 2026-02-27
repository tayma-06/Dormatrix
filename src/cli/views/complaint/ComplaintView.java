package cli.views.complaint;

import libraries.collections.MyArrayList;
import libraries.collections.MyString;
import models.complaints.Complaint;

public class ComplaintView {

    public void studentMenu(){
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("|                      COMPLAINT(STUDENT)                             |");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("1. File a Complaint");
        System.out.println("2. View My Complaints");
//        System.out.println("3. Track Complaint by ID");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
    }

    public void attendantMenu(){
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("|                      COMPLAINT(ATTENDANT)                           |");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("1. View ALL complaints");
        System.out.println("2. View PENDING (unassigned/SUBMITTED)");
        System.out.println("3. Reassign complaint (manual worker id)");
        System.out.println("4. Resolve complaint (attendant override)");
        System.out.println("5. View complaints by ROOM");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
    }

    public void workerMenu(){
        System.out.println();
        System.out.println("════════════════════════════════════════════════════════════════════════");
        System.out.println("|                        TASK QUEUE(WORKER)                            |");
        System.out.println("════════════════════════════════════════════════════════════════════════");
        System.out.println("1. View Assigned Tasks");
        System.out.println("2. Update Progress");
        System.out.println("3. Mark Completed");
        System.out.println("0. Back");
        System.out.println();
        System.out.print("Enter choice: ");
    }

    public void msg(String s){ System.out.println(s); }
    public void error(String s){ System.out.println("Error: " + s); }

    public void filed(Complaint c){
        System.out.println("\nComplaint Filed Successfully!");
        System.out.println("Complaint ID : " + c.getComplaintId());
        System.out.println("Status       : " + c.getStatus().name());
        String wid = c.getAssignedWorkerId();
        boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();
        System.out.println("Assigned To  : " + (blank ? "(not assigned yet)" : wid));
        System.out.println();
    }

    public void list(MyArrayList<Complaint> list){
        if (list == null || list.size() == 0){
            System.out.println("\n(No complaints found)\n");
            return;
        }

        System.out.println("══════════════════════════════════════════════════════════════════════════");
        System.out.println("|                                LIST                                    |");
        System.out.println("══════════════════════════════════════════════════════════════════════════");
        System.out.println("║ ID                 ║ Status     ║ Assigned Worker ║ Category           ║");
        System.out.println("╠════════════════════╬════════════╬═════════════════╬════════════════════╣");
        for (int i = 0; i < list.size(); i++){
            Complaint c = list.get(i);
            String wid = c.getAssignedWorkerId();
            boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();
            System.out.println(
                    String.format("║ %-18s ║ %-10s ║ %-15s ║ %-18s ║",
                            c.getComplaintId(),
                            c.getStatus().name(),
                            (blank ? "(none)" : wid),
                            c.getCategory().name())
            );
        }
        System.out.println("╚════════════════════╩════════════╩═════════════════╩════════════════════╝");
    }

    public void details(Complaint c){
        String wid = c.getAssignedWorkerId();
        boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();

        System.out.println("\n------------------ COMPLAINT DETAILS ------------------");
        System.out.println("ID          : " + c.getComplaintId());
        System.out.println("Student ID  : " + c.getStudentId());
        System.out.println("Room        : " + c.getStudentRoomNo());
        System.out.println("Category    : " + c.getCategory().name());
        System.out.println("Priority    : " + c.getPriority().name());
        System.out.println("Status      : " + c.getStatus().name());
        System.out.println("Worker      : " + (blank ? "(none)" : wid));
        System.out.println("Description : " + c.getDescription());
        System.out.println("Notes/Tags  : " + (c.getTags() == null ? "" : c.getTags()));
        System.out.println("-------------------------------------------------------\n");
    }
}
