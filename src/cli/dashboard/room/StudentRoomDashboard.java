package cli.dashboard.room;

import cli.dashboard.Dashboard;
import cli.views.room.StudentRoomView;
import controllers.dashboard.room.StudentRoomDashboardController;
import libraries.collections.MyArrayList;
import libraries.collections.MyString;
import models.complaints.Complaint;
import models.room.Room;
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
            int choice = view.show(roomNumber, room);
            if (choice == 0) return;
            if (choice == 1) {
                if (roomNumber.equals("UNASSIGNED") || roomNumber.equals("N/A")) {
                    System.out.println("\n>> You do not have a room assigned yet.");
                } else {
                    showComplaints(roomNumber);
                }
                System.out.print("Press Enter to return...");
                FastInput.readLine();
            }
        }
    }

    private void showComplaints(String roomNumber) {
        MyArrayList<Complaint> list = controller.getComplaints(roomNumber);
        System.out.println("\n------------------ COMPLAINTS FOR ROOM " + roomNumber + " ------------------");
        if (list.size() == 0) {
            System.out.println("(No complaints found for this room)");
        } else {
            for (int i = 0; i < list.size(); i++) {
                Complaint c = list.get(i);
                String wid = c.getAssignedWorkerId();
                boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();

                System.out.println(
                        "ID: " + c.getComplaintId()
                                + " | Status: " + c.getStatus().name()
                                + " | Worker: " + (blank ? "(none)" : wid)
                                + " | Cat: " + c.getCategory().name()
                );
            }
        }
        System.out.println("-----------------------------------------------------------------------\n");
    }
}