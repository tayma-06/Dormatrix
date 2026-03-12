package controllers.dashboard;

import cli.complaint.StudentComplaintCLI;
import cli.dashboard.MainDashboard;
import cli.forms.food.MealTokenPurchase;
import cli.routine.StudentRoutineCLI;
import cli.announcement.AnnouncementBoardCLI;
import cli.contacts.StudentEmergencyContactsCLI;
import cli.views.store.*;
import controllers.room.RoomController;
import cli.dashboard.FacilityDashboard;
import controllers.facilities.*;
import cli.views.LostFoundView;

public class StudentDashboardController {

    private final MainDashboard mainDashboard;
    private final MealTokenPurchase mealTokenPurchase;
    private final StoreLedgerView storeLedgerView;
    private final RoomController roomController;
    private final StudyRoomController studyRoomController;
    private final FridgeController fridgeController;
    private final LaundryController laundryController;
    private final FacilityDashboard facilityDashboard;
    private final StudentRoutineCLI studentRoutineCLI;

    public StudentDashboardController() {
        this.mealTokenPurchase = new MealTokenPurchase();
        this.mainDashboard = new MainDashboard();
        this.storeLedgerView = new StoreLedgerView();
        this.roomController = new RoomController();
        this.studyRoomController = new StudyRoomController();
        this.fridgeController = new FridgeController();
        this.laundryController = new LaundryController();
        this.facilityDashboard = new FacilityDashboard();
        this.studentRoutineCLI = new StudentRoutineCLI();
    }

    public void handleInput(int choice, String username) {
        switch (choice) {
            case 1:
                roomController.showStudentRoomDetails(username);
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
                // Call the Lost & Found board, passing 'false' to restrict "Add Found Item"
                new LostFoundView().showMainBoard(username, false);
                break;
            case 6:
                new StudentComplaintCLI().start(username);
                break;
            case 7:
                studentRoutineCLI.show(username);
                break;
            case 8:
                new AnnouncementBoardCLI().show();
                break;
            case 9:
                ShoppingCartView.show(username);
                break;
            case 10:
                new StudentEmergencyContactsCLI().show();
                break;
            case 0:
                mainDashboard.show();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
}
