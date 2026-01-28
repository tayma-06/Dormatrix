package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.views.MessageView;
import cli.forms.MealTokenPurchase;
import cli.views.StoreLedgerView;
import controllers.room.RoomController; // Import the updated RoomController

public class StudentDashboardController {

    private final MainDashboard mainDashboard;
    private final MealTokenPurchase mealTokenPurchase;
    private final StoreLedgerView storeLedgerView;
    private final RoomController roomController;
    private final MessageView msg;

    public StudentDashboardController() {
        this.mainDashboard = new MainDashboard();
        this.mealTokenPurchase = new MealTokenPurchase();
        this.storeLedgerView = new StoreLedgerView();
        this.roomController = new RoomController();
        this.msg = new MessageView();
    }

    public void handleInput(int choice, String username) {
        switch (choice) {
            case 1:
                roomController.showStudentRoomDetails(username);
                break;

            case 2:
                System.out.println(">> Feature [Facility Booking] is under development.");
                break;
            case 3:
                mealTokenPurchase.show(username);
                break;
            case 4:
                storeLedgerView.show(username);
                break;
            case 5:
                System.out.println(">> Feature [Lost & Found] is under development.");
                break;
//            case 6:
//                studentComplaintMenu(username);
//                break;
            case 7:
                System.out.println(">> Feature [Announcements] is under development.");
                break;
            case 0:
                mainDashboard.show();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }}
