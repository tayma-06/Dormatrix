package controllers.dashboard;
import cli.dashboard.*;
import controllers.authentication.AuthController;
import controllers.authentication.AccountManager;
import libraries.collections.MyString;
public class MainDashboardController {
    private final AuthController authController = new AuthController();
    private final AccountManager accountManager = new AccountManager();
    public MainDashboardController() {
        authController.createDefaultAdmin();
    }
    public void handleRoleInput(int choice, MyString username, MyString password) {
        MyString role = getRoleFromChoice(choice);
        boolean authenticated = authController.authenticateUser(username, password, role);
        if (!authenticated) {
            System.out.println("\n-----------------------------------------------------------------------");
            System.out.println("| Invalid username or password!                                       |");
            System.out.println("-----------------------------------------------------------------------\n");
            return;
        }
        System.out.println("\n-----------------------------------------------------------------------");
        System.out.println("| Login successful!                                                    |");
        System.out.println("-----------------------------------------------------------------------\n");
        Dashboard dashboard = getDashboardForRole(choice);
        if (dashboard != null) {
            dashboard.show(username.getValue());
        } else {
            System.out.println("Dashboard not available for this role.");
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
            default -> new MyString("UNKNOWN");
        };
    }
}