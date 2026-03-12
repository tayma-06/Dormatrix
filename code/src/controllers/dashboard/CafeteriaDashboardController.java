package controllers.dashboard;

import cli.dashboard.food.CafeteriaService;
import cli.dashboard.food.SystemOperationService;

public class CafeteriaDashboardController {
    private final CafeteriaService menuUI = new CafeteriaService();
    private final SystemOperationService opsUI = new SystemOperationService();

    public void handleAction(int choice) {
        switch (choice) {
            case 1 -> menuUI.showWeeklyMenuUI();
            case 2 -> menuUI.showSpecialEventUI();
            case 3 -> opsUI.showTokenVerificationUI();
            case 4 -> opsUI.showRamadanToggleUI();
            default -> System.out.println("Invalid Selection!");
        }
    }
}