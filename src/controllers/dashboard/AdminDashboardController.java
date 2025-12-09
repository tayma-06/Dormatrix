package controllers.dashboard;

import cli.dashboard.MainDashboard;

public class AdminDashboardController
{
    MainDashboard mainDashboard = new MainDashboard();
    public void handleInput(int choice, String username)
    {
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
            case 0:
                mainDashboard.show();
                break;
            default:
                System.out.println("Invalid choice. Please try again...");
        }
    }
}
