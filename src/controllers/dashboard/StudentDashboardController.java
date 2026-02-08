package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.dashboard.room.StudentRoomDashboard;
import controllers.dashboard.room.StudentRoomDashboardController; // Added import
import cli.views.MessageView;
import cli.forms.MealTokenPurchase;
import cli.views.StoreLedgerView;
import controllers.room.RoomService;
import cli.dashboard.FacilityDashboard;
import controllers.facilities.*;

public class StudentDashboardController {

    private final MainDashboard mainDashboard;
    private final MealTokenPurchase mealTokenPurchase;
    private final StoreLedgerView storeLedgerView;
    private final StudentRoomDashboard studentRoomDashboard;
    private final MessageView msg;
    private final StudyRoomController studyRoomController;
    private final FridgeController fridgeController;
    private final LaundryController laundryController;
    private final FacilityDashboard facilityDashboard;
    private final RoomService roomService; // Renamed for consistency

    public StudentDashboardController() {
        // STEP 1: Initialize Service first
        this.roomService = new RoomService();

        // STEP 2: Initialize Dashboards that depend on the Service
        // We pass a new DashboardController to the Dashboard
        this.studentRoomDashboard = new StudentRoomDashboard(new StudentRoomDashboardController(roomService));

        // STEP 3: Initialize everything else
        this.mainDashboard = new MainDashboard();
        this.mealTokenPurchase = new MealTokenPurchase();
        this.storeLedgerView = new StoreLedgerView();
        this.msg = new MessageView();
        this.studyRoomController = new StudyRoomController();
        this.fridgeController = new FridgeController();
        this.laundryController = new LaundryController();
        this.facilityDashboard = new FacilityDashboard();
    }

    public void handleInput(int choice, String username) {
        switch (choice) {
            case 1:
                // FIX: Instead of calling roomService directly,
                // we call the Dashboard's show() method.
                studentRoomDashboard.show(username);
                break;

            case 2:
                facilityDashboard.showMenu(username, studyRoomController, fridgeController, laundryController);
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
    }
}