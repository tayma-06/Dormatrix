package controllers.dashboard;

import cli.dashboard.MainDashboard;

public class MaintenanceWorkerDashboardController {
    MainDashboard mainDashboard = new MainDashboard();

    // Returns false is user chooses to logout
    public void handleInput(int choice, String username)
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
                mainDashboard.show();
                break;
            default:
                System.out.println("Invalid choice. Please try again");
        }
    }
}
