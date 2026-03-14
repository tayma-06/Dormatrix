package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.dashboard.room.StudentRoomDashboard;
import cli.profile.EditProfileCLI;
import cli.routine.AttendantRoutineCLI;
import cli.schedule.AttendantWorkerScheduleCLI;
import cli.announcement.AttendantAnnouncementCLI;
import cli.contacts.AttendantEmergencyContactsCLI;
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
    private final ComplaintModule complaints
            = new ComplaintModule(new FileComplaintRepository(), new FileMaintenanceWorkerRepository());

    private final RoomController roomController = new RoomController();

    private final Scanner sc = new Scanner(System.in);

    public void handleInput(int choice, String username) {
        switch (choice) {
//            case 1:
//                System.out.println("Handling Student Complaints...");
//                complaintsMenu();
//                break;
            case 2:
                new AttendantWorkerScheduleCLI().show(username);
                break;

            case 3: // Lost & Found
                LostFoundView attendantLfView = new LostFoundView();
                // Pass 'true' because the Hall Attendant IS allowed to add found items
                attendantLfView.showMainBoard(username, true);
                break;
            case 4:
                new AttendantRoutineCLI().show();
                break;
            case 5:
                new AttendantAnnouncementCLI().show(username);
                break;
            case 6:
                new AttendantEmergencyContactsCLI().show(username);
                break;
            case 7:
                new EditProfileCLI(username, "HALL_ATTENDANT").start();
                break;
            case 0:
                mainDashboard.show();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

//    private void printList(MyArrayList<Complaint> list) {
//        if (list.size() == 0) {
//            System.out.println("\n(No complaints found)\n");
//            return;
//        }
//
//        System.out.println("\n------------------ LIST ------------------");
//        for (int i = 0; i < list.size(); i++) {
//            Complaint c = list.get(i);
//            String wid = c.getAssignedWorkerId();
//            boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();
//            System.out.println(
//                    "ID: " + c.getComplaintId()
//                    + " | Student: " + c.getStudentId()
//                    + " | Room: " + c.getStudentRoomNo()
//                    + " | Cat: " + c.getCategory().name()
//                    + " | Status: " + c.getStatus().name()
//                    + " | Worker: " + (blank ? "(none)" : wid)
//                    + " | Priority: " + c.getPriority().name()
//            );
//        }
//        System.out.println("------------------------------------------\n");
//    }
}
