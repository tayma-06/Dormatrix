package controllers.dashboard;

public class MaintenanceWorkerDashboardController {

    // Returns false is user chooses to logout
    public boolean handleInput(int choice, String username)
    {
        switch (choice)
        {
            case 1:     
                System.out.println("Viewing work field...");
                //TODO: call work field to show which field he works in
                break;
            case 2:
                System.out.println("Viewing task queue...");
                //TODO: call task queue for the particular worker type
                break;
            case 3:
                System.out.println("Viewing updated student comment...");
                //TODO: calls the student complaint class to show the updated status
                break;
            case 0:
                return false; // Logout
            default:
                System.out.println("Invalid choice. Please try again");
        }
        return true;
    }
}
