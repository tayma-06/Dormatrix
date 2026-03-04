package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.dashboard.room.StudentRoomDashboard;
import cli.views.LostFoundView;
import controllers.dashboard.room.StudentRoomDashboardController;
import controllers.room.RoomService;
import module.complaint.ComplaintModule;
import repo.file.FileComplaintRepository;
import repo.file.FileMaintenanceWorkerRepository;
import libraries.collections.MyArrayList;
import libraries.collections.MyString;
import models.complaints.Complaint;
import controllers.room.RoomController;


import java.util.Scanner;

public class AttendantDashboardController {
    private final MainDashboard mainDashboard = new MainDashboard();

    // complaint system
    private final ComplaintModule complaints =
            new ComplaintModule(new FileComplaintRepository(), new FileMaintenanceWorkerRepository());

    private final RoomController roomController = new RoomController();

    private final Scanner sc = new Scanner(System.in);

    public void handleInput(int choice, String username){
        switch (choice)
        {
//            case 1:
//                System.out.println("Handling Student Complaints...");
//                complaintsMenu();
//                break;
            case 2:
                System.out.println("Handling Worker Schedule...");
                break;
            // Inside your handleInput(int choice, String username) method:

            case 3: // Lost & Found
                LostFoundView attendantLfView = new LostFoundView();
                // Pass 'true' because the Hall Attendant IS allowed to add found items
                attendantLfView.showMainBoard(username, true);
                break;
            case 0:
                mainDashboard.show();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

//    private void complaintsMenu(){
//        // consume newline leftover from dashboard's nextInt()
//        sc.nextLine();
//
//        while (true){
//            System.out.println("\n------------------ COMPLAINTS (ATTENDANT) ------------------");
//            System.out.println("1. View ALL complaints");
//            System.out.println("2. View PENDING (unassigned/SUBMITTED)");
//            System.out.println("3. View MISHAPS (unassigned / invalid worker / mismatch)");
//            System.out.println("4. Reassign complaint (manual worker id)");
//            System.out.println("5. Retry AUTO-ASSIGN for a complaint");
//            System.out.println("6. Resolve complaint (attendant override)");
//            System.out.println("7. View complaints by ROOM");
//            System.out.println("0. Back");
//            System.out.print("Enter choice: ");
//
//            String line = sc.nextLine();
//            int ch;
//            try { ch = Integer.parseInt(line.trim()); }
//            catch (Exception e){ System.out.println("Invalid input."); continue; }
//
//            if (ch == 0) return;
//
//            if (ch == 1){
//                printList(complaints.findAll());
//            } else if (ch == 2){
//                printList(complaints.findPending());
//            } else if (ch == 3){
//                printList(complaints.findMishaps());
//            } else if (ch == 4){
//                System.out.print("Complaint ID: ");
//                String cid = sc.nextLine().trim();
//                System.out.print("Worker ID: ");
//                String wid = sc.nextLine().trim();
//
//                boolean ok = complaints.reassignComplaint(cid, wid);
//                System.out.println(ok ? "Reassigned successfully." : "Failed (invalid complaintId/workerId).");
//            } else if (ch == 5){
//                System.out.print("Complaint ID: ");
//                String cid = sc.nextLine().trim();
//                boolean ok = complaints.retryAutoAssign(cid);
//                System.out.println(ok ? "Auto-assign attempted (check status/worker)." : "Failed (invalid complaintId).");
//            } else if (ch == 6){
//                System.out.print("Complaint ID: ");
//                String cid = sc.nextLine().trim();
//                System.out.print("Resolution note: ");
//                String note = sc.nextLine();
//
//                boolean ok = complaints.resolveByAttendant(cid, note);
//                System.out.println(ok ? "Resolved successfully." : "Failed (invalid complaintId).");
//            }else if (ch == 7) {
//                System.out.print("Enter room number: ");
//                String room = sc.nextLine().trim();
//                new StudentRoomDashboard(new StudentRoomDashboardController(new RoomService())).showComplaints(room);
//            }else {
//                System.out.println("Invalid choice.");
//            }
//        }
//    }

    private void printList(MyArrayList<Complaint> list){
        if (list.size() == 0){
            System.out.println("\n(No complaints found)\n");
            return;
        }

        System.out.println("\n------------------ LIST ------------------");
        for (int i = 0; i < list.size(); i++){
            Complaint c = list.get(i);
            String wid = c.getAssignedWorkerId();
            boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();
            System.out.println(
                    "ID: " + c.getComplaintId()
                            + " | Student: " + c.getStudentId()
                            + " | Room: " + c.getStudentRoomNo()
                            + " | Cat: " + c.getCategory().name()
                            + " | Status: " + c.getStatus().name()
                            + " | Worker: " + (blank ? "(none)" : wid)
                            + " | Priority: " + c.getPriority().name()
            );
        }
        System.out.println("------------------------------------------\n");
    }
}