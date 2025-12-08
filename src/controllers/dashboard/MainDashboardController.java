package controllers.dashboard;

import cli.dashboard.*;

public class MainDashboardController {

    public void handleRoleInput(String choice, String username, String password) {
        // TODO: Authenticate user with AuthController (check username, password, role)
        boolean authenticated = true; // For now, assume authenticated

        if (!authenticated) {
            System.out.println("Invalid username or password!\n");
            return;
        }

        switch (choice) {
            case "1":
                new StudentDashboard().show(username);
                break;
//            case "2":
//                new AttendantDashboard().show(username);
//                break;
//            case "3":
//                new MaintenanceDashboard().show(username);
//                break;
//            case "4":
//                new StoreDashboard().show(username);
//                break;
//            case "5":
//                new HallOfficeDashboard().show(username);
//                break;
//            case "6":
//                new AdminDashboard().show(username);
//                break;
            default:
                System.out.println("Invalid choice. Please select a valid role.\n");
        }
    }
}
