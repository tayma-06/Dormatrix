package controllers.dashboard;

public class StudentDashboardController {

    public boolean handleInput(int choice, String username) {
        switch (choice) {
            case 1:
                System.out.println("Displaying Room Info...\n");
                break;
            case 2:
                System.out.println("Opening Facility Booking...\n");
                break;
            case 3:
                System.out.println("Processing Meal Token Purchase...\n");
                break;
            case 4:
                System.out.println("Viewing Store Purchases & Dues...\n");
                break;
            case 5:
                System.out.println("Accessing Lost & Found...\n");
                break;
            case 6:
                System.out.println("Submitting Complaint...\n");
                break;
            case 7:
                System.out.println("Fetching Announcements...\n");
                break;
            case 0:
                return false;
            default:
                System.out.println("Invalid choice. Please try again.\n");
        }
        return true;
    }
}
