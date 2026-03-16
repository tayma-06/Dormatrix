package controllers.dashboard;

import cli.announcement.AnnouncementBoardCLI;
import cli.complaint.StudentComplaintCLI;
import cli.contacts.StudentEmergencyContactsCLI;
import cli.dashboard.FacilityDashboard;
import cli.dashboard.MainDashboard;
import cli.dashboard.room.StudentRoomDashboard;
import cli.forms.food.MealTokenPurchase;
import cli.profile.EditProfileCLI;
import cli.routine.StudentRoutineCLI;
import cli.views.LostFoundView;
import cli.views.store.ShoppingCartView;
import cli.views.store.StoreLedgerView;
import controllers.dashboard.room.StudentRoomDashboardController;
import controllers.facilities.FridgeController;
import controllers.facilities.LaundryController;
import controllers.facilities.StudyRoomController;
import controllers.room.RoomService;

public class StudentDashboardController {

    private final MainDashboard mainDashboard;
    private final MealTokenPurchase mealTokenPurchase;
    private final StoreLedgerView storeLedgerView;
    private final StudyRoomController studyRoomController;
    private final FridgeController fridgeController;
    private final LaundryController laundryController;
    private final FacilityDashboard facilityDashboard;
    private final StudentRoutineCLI studentRoutineCLI;
    private EditProfileCLI editProfileCLI;

    public StudentDashboardController() {
        this.mealTokenPurchase = new MealTokenPurchase();
        this.mainDashboard = new MainDashboard();
        this.storeLedgerView = new StoreLedgerView();
        this.studyRoomController = new StudyRoomController();
        this.fridgeController = new FridgeController();
        this.laundryController = new LaundryController();
        this.facilityDashboard = new FacilityDashboard();
        this.studentRoutineCLI = new StudentRoutineCLI();
    }

    public void handleInput(int choice, String username) {
        switch (choice) {
            case 1:
                new StudentRoomDashboard(
                        new StudentRoomDashboardController(new RoomService())
                ).show(username);
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
            case 11:
                this.editProfileCLI = new EditProfileCLI(username, "STUDENT");
                editProfileCLI.start();
                break;
            case 0:
                mainDashboard.show();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
}