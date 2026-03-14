package controllers.dashboard;

import cli.dashboard.food.CafeteriaService;
import cli.dashboard.food.SystemOperationService;
import cli.profile.EditProfileCLI;

public class CafeteriaDashboardController {
    private final CafeteriaService menuUI = new CafeteriaService();
    private final SystemOperationService opsUI = new SystemOperationService();

    public void handleAction(int choice, String username) {
        switch (choice) {
            case 1: menuUI.showWeeklyMenuUI(); break;
            case 2: menuUI.showSpecialEventUI(); break;
            case 3: opsUI.showTokenVerificationUI(); break;
            case 4: opsUI.showRamadanToggleUI(); break;
            case 5: EditProfileCLI editProfileCLI = new EditProfileCLI(username, "CAFETERIA_MANAGER");
            editProfileCLI.start();
            break;
            default: System.out.println("Invalid Selection!");
        }
    }
}