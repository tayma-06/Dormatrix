package controllers.dashboard;

public class AdminDashboardController {

    public boolean handleInput(int choice, String username) {
        switch (choice) {
            case 1:
                System.out.println("Creating a new account...");
                break;
            case 2:
                System.out.println("Deleting an account...");
                break;
            case 3:
                System.out.println("Viewing accounts grouped by role...");
                break;
            case 4:
                return false;
            default:
                System.out.println("Invalid choice. Please try again...");
        }
        return true;
    }
}
