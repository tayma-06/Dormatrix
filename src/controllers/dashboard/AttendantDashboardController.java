package controllers.dashboard;

import cli.dashboard.MainDashboard;

public class AttendantDashboardController {
    MainDashboard mainDashboard = new MainDashboard();

    public void handleInput(int choice, String username){
        switch (choice)
        {
            case 1:
                System.out.println("Handling Student Complaints...");
                break;
            case 2:
                System.out.println("Handling Worker Schedule...");
                break;
            case 0:
                mainDashboard.show();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
}