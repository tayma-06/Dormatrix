package controllers.dashboard;

import cli.dashboard.*;
import controllers.authentication.AuthController;
import models.users.User;
import libraries.collections.MyString;
import utils.ConsoleUtil;

public class MainDashboardController {
    private final AuthController authController = new AuthController();

    public MainDashboardController() {
        authController.createDefaultAdmin();
    }

    public void handleRoleInput(int choice, MyString username, MyString password) {
        MyString role = getRoleFromChoice(choice);
        boolean authenticated = authController.authenticateUser(username, password, role);

        if (!authenticated) {
            System.out.println();
            System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
            System.out.println("║ Invalid username or password!                                       ║");
            System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
            System.out.println();
            ConsoleUtil.pause();
            return;
        }

        System.out.println();
        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ Login successful!                                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        ConsoleUtil.pause();

        User loggedInUser = authController.getUserByUsername(username, role);
        Dashboard dashboard = getDashboardForRole(choice);

        if (dashboard != null) {
            if (loggedInUser != null) {
                dashboard.show(loggedInUser.getName());
            } else {
                dashboard.show(username.getValue());
            }
        } else {
            System.out.println("Dashboard not available for this role.");
            ConsoleUtil.pause();
        }
    }

    private Dashboard getDashboardForRole(int choice) {
        return switch (choice) {
            case 1 -> new StudentDashboard();
            case 2 -> new AttendantDashboard();
            case 3 -> new MaintenanceWorkerDashboard();
            case 4 -> new StoreInChargeDashboard();
            case 5 -> new HallOfficeDashboard();
            case 6 -> new AdminDashboard();
            case 7 -> new CafeteriaManagerDashboard();
            default -> null;
        };
    }

    private MyString getRoleFromChoice(int choice) {
        return switch (choice) {
            case 1 -> new MyString("STUDENT");
            case 2 -> new MyString("HALL_ATTENDANT");
            case 3 -> new MyString("MAINTENANCE_WORKER");
            case 4 -> new MyString("STORE_IN_CHARGE");
            case 5 -> new MyString("HALL_OFFICER");
            case 6 -> new MyString("ADMIN");
            case 7 -> new MyString("CAFETERIA_MANAGER");
            default -> new MyString("UNKNOWN");
        };
    }
}