package controllers.dashboard;

public class AdminDashboardController {

    // Returns false if user logs out
    public boolean handleInput(String choice, String username) {
        switch (choice) {
            case "1":
                System.out.println("Creating a new account...\n");
                // TODO: call UserManager or AuthController to create account
                break;
            case "2":
                System.out.println("Deleting an account...\n");
                // TODO: call UserManager to delete account
                break;
            case "3":
                System.out.println("Viewing accounts grouped by role...\n");
                // TODO: UserManager lists users by role
                break;
            case "0":
                return false; // logout
            default:
                System.out.println("Invalid choice. Please try again.\n");
        }
        return true;
    }
}
