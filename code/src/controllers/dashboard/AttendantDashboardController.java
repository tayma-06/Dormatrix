package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.dashboard.room.StudentRoomDashboard;
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
        // Always clear before any sub-screen
        utils.ConsoleUtil.clearScreen();
        utils.BackgroundFiller.applyAttendantTheme();
        utils.TerminalUI.setActiveTheme(
                utils.ConsoleColors.fgRGB(40, 220, 210),
                utils.ConsoleColors.ThemeText.ATTENDANT_TEXT,
                utils.ConsoleColors.bgRGB(0, 28, 26)
        );
        utils.TerminalUI.fillBackground(utils.TerminalUI.getActiveBgColor());
        utils.TerminalUI.at(2, 1);

        switch (choice) {
            case 2:
                new AttendantWorkerScheduleCLI().show(username);
                break;
            case 3:
                LostFoundView attendantLfView = new LostFoundView();
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
            case 0:
                mainDashboard.show();
                break;
            default:
                utils.TerminalUI.tError("Invalid choice.");
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
