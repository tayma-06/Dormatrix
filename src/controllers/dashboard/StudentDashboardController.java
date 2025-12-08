package controllers.dashboard;

public class StudentDashboardController {

    // Returns false if user chooses to logout
    public boolean handleInput(String choice, String username) {
        switch (choice) {
            case "1":
                System.out.println("Displaying Room Info...\n");
                // TODO: call RoomController to fetch room info for this student
                break;
            case "2":
                System.out.println("Opening Facility Booking...\n");
                // TODO: StudyRoomController, FridgeController, LaundryController
                break;
            case "3":
                System.out.println("Processing Meal Token Purchase...\n");
                // TODO: MealTokenController
                break;
            case "4":
                System.out.println("Viewing Store Purchases & Dues...\n");
                // TODO: StoreController (Inventory + Dues)
                break;
            case "5":
                System.out.println("Accessing Lost & Found...\n");
                // TODO: LostFoundController
                break;
            case "6":
                System.out.println("Submitting Complaint...\n");
                // TODO: ComplaintController
                break;
            case "7":
                System.out.println("Fetching Announcements...\n");
                // TODO: EventController
                break;
            case "0":
                return false; // Logout
            default:
                System.out.println("Invalid choice. Please try again.\n");
        }
        return true;
    }
}
