package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.forms.MealTokenPurchase;
//import cli.views.StoreLedgerView;

public class StudentDashboardController {

    private final MainDashboard mainDashboard;
    private final MealTokenPurchase mealTokenPurchase;
    //private final StoreLedgerView storeLedgerView;

    public StudentDashboardController() {
        this.mainDashboard = new MainDashboard();
        this.mealTokenPurchase = new MealTokenPurchase();
        //this.storeLedgerView = new StoreLedgerView();
    }

    public void handleInput(int choice, String username) {
        switch (choice) {

            case 1:
                showRoomInfo(username);
                break;

            case 2:
                openFacilityBooking(username);
                break;

            case 3:
                mealTokenPurchase.show(username);   
                break;

            case 4:
                // storeLedgerView.show(username);     // ✅ Store & Dues Module
                // break;

            case 5:
                accessLostAndFound();
                break;

            case 6:
                submitComplaint(username);
                break;

            case 7:
                fetchAnnouncements();
                break;

            case 0:
                mainDashboard.show();
                break;

            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private void showRoomInfo(String username) {
        System.out.println("Displaying Room Information for " + username + "...\n");
    }

    private void openFacilityBooking(String username) {
        System.out.println("Opening Facility Booking for " + username + "...\n");
    }

    private void accessLostAndFound() {
        System.out.println("Accessing Lost & Found...\n");
    }

    private void submitComplaint(String username) {
        System.out.println("Submitting Complaint for " + username + "...\n");
    }

    private void fetchAnnouncements() {
        System.out.println("Fetching Announcements...\n");
    }
}
