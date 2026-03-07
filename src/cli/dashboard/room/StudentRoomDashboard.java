package cli.dashboard.room;

import cli.dashboard.Dashboard;
import cli.views.room.StudentRoomView;
import controllers.dashboard.room.StudentRoomDashboardController;
import libraries.collections.MyArrayList;
import libraries.collections.MyString;
import models.complaints.Complaint;
import models.room.Room;
import utils.ConsoleUtil;
import utils.FastInput;

public class StudentRoomDashboard implements Dashboard {

    private final StudentRoomDashboardController controller;
    private final StudentRoomView view;

    public StudentRoomDashboard(StudentRoomDashboardController controller) {
        this.controller = controller;
        this.view = new StudentRoomView();
    }

    @Override
    public void show(String studentIdentifier) {
        String roomNumber = controller.getStudentRoomNumber(studentIdentifier);
        Room room = controller.getRoomDetails(roomNumber);

        while (true) {
            ConsoleUtil.clearScreen();
            int choice = view.show(roomNumber, room);
            if (choice == 0) {
                ConsoleUtil.clearScreen();
                return;
            }
            if (choice == 1) {
                if (roomNumber.equals("UNASSIGNED") || roomNumber.equals("N/A")) {
                    System.out.println("\n>> You do not have a room assigned yet.");
                } else {
                    showComplaints(roomNumber);
                }
                ConsoleUtil.pause();
            }
        }
    }

    public void showComplaints(String roomNumber) {
        MyArrayList<Complaint> list = controller.getComplaints(roomNumber);

        System.out.println("╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                          COMPLAINTS FOR ROOM " + roomNumber + "                       ║");
        System.out.println("╠════════════════════╦════════════╦═════════════════╦════════════════════╣");
        System.out.println("║ ID                 ║ Status     ║ Assigned Worker ║ Category           ║");
        System.out.println("╠════════════════════╬════════════╬═════════════════╬════════════════════╣");
        if (list.size() == 0) {
            System.out.println(String.format("║ %-18s ║ %-10s ║ %-15s ║ %-18s ║", "(none)", "(none)", "(none)", "(none)"));
        } else {
            for (int i = 0; i < list.size(); i++) {
                Complaint c = list.get(i);
                String wid = c.getAssignedWorkerId();
                boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();

                System.out.println(String.format(
                        "║ %-18s ║ %-10s ║ %-15s ║ %-18s ║",
                        c.getComplaintId(),
                        c.getStatus().name(),
                        (blank ? "(none)" : wid),
                        c.getCategory().name()
                ));
            }
        }

        System.out.println("╠════════════════════╩════════════╩═════════════════╩════════════════════╣");
        System.out.println("║ Press Enter to continue...                                             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");

        FastInput.readLine();
    }

//    private void showComplaints(String roomNumber) {
//        MyArrayList<Complaint> list = controller.getComplaints(roomNumber);
//        System.out.println("\n------------------ COMPLAINTS FOR ROOM " + roomNumber + " ------------------");
//        if (list.size() == 0) {
//            System.out.println("(No complaints found for this room)");
//        } else {
//            for (int i = 0; i < list.size(); i++) {
//                Complaint c = list.get(i);
//                String wid = c.getAssignedWorkerId();
//                boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();
//
//                System.out.println(
//                        "ID: " + c.getComplaintId()
//                                + " | Status: " + c.getStatus().name()
//                                + " | Worker: " + (blank ? "(none)" : wid)
//                                + " | Cat: " + c.getCategory().name()
//                );
//            }
//        }
//        System.out.println("-----------------------------------------------------------------------\n");
//    }
}
