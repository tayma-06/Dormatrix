package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.routine.AttendantRoutineCLI;
import cli.views.LostFoundView;
import libraries.collections.MyArrayList;
import libraries.collections.MyString;
import models.complaints.Complaint;
import utils.TerminalUI;

public class AttendantDashboardController {
    private final MainDashboard mainDashboard = new MainDashboard();

    public void handleInput(int choice, String username){
        switch (choice)
        {
//            case 1:
//                System.out.println("Handling Student Complaints...");
//                complaintsMenu();
//                break;
            case 2:
                TerminalUI.tPrint("Handling Worker Schedule...");
                break;
            // Inside your handleInput(int choice, String username) method:

            case 3: // Lost & Found
                LostFoundView attendantLfView = new LostFoundView();
                // Pass 'true' because the Hall Attendant IS allowed to add found items
                attendantLfView.showMainBoard(username, true);
                break;
            case 4:
                new AttendantRoutineCLI().show();
                break;
            case 0:
                mainDashboard.show();
                break;
            default:
                TerminalUI.tError("Invalid choice. Please try again.");
        }
    }

    private void printList(MyArrayList<Complaint> list){
        if (list.size() == 0){
            TerminalUI.tEmpty();
            TerminalUI.tPrint("(No complaints found)");
            TerminalUI.tEmpty();
            return;
        }

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("LIST");
        TerminalUI.tBoxSep();
        for (int i = 0; i < list.size(); i++){
            Complaint c = list.get(i);
            String wid = c.getAssignedWorkerId();
            boolean blank = (wid == null) || new MyString(wid).trim().isEmpty();
            TerminalUI.tBoxLine(
                    "ID: " + c.getComplaintId()
                            + " | Student: " + c.getStudentId()
                            + " | Room: " + c.getStudentRoomNo()
                            + " | Cat: " + c.getCategory().name()
                            + " | Status: " + c.getStatus().name()
                            + " | Worker: " + (blank ? "(none)" : wid)
                            + " | Priority: " + c.getPriority().name());
        }
        TerminalUI.tBoxBottom();
        TerminalUI.tEmpty();
    }
}