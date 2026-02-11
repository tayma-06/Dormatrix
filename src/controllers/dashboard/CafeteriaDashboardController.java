package controllers.dashboard;

import controllers.food.MenuManagementController;
import controllers.food.OperationController;

public class CafeteriaDashboardController {
    private final MenuManagementController menuController = new MenuManagementController();
    private final OperationController operationController = new OperationController();

    public void handleAction(int choice) {
        switch (choice) {
            case 1 -> menuController.manageWeeklyMenu();
            case 2 -> menuController.manageSpecialEvent();
            case 3 -> operationController.processTokenVerification();
            case 4 -> operationController.manageRamadanMode();
            default -> System.out.println("Invalid Selection!");
        }
    }
}