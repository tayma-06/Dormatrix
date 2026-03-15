package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.profile.EditProfileCLI;
import cli.views.store.*;

public class StoreInChargeDashboardController {
    MainDashboard mainDashboard = new MainDashboard();
    InventoryManagementView inventoryManagement = new InventoryManagementView();
    ShoppingCartView purchaseView = new ShoppingCartView();
    SalesSummaryView salesSummary = new SalesSummaryView();

    public void handleInput(int choice, String username) {
        switch(choice) {
            case 1:
                // Inventory Management with full CRUD operations
                inventoryManagement.show();
                break;

            case 2:
                // Purchase Item (can also be used by store-in-charge for walk-in purchases)
                System.out.print("Enter Student ID for purchase: ");
                String studentId = utils.FastInput.readLine();
                purchaseView.show(studentId);
                break;

            case 3:
                // Sales Summary with various report options
                salesSummary.show();
                break;

            case 4:
                EditProfileCLI editProfileCLI = new EditProfileCLI(username, "STORE_IN_CHARGE");
                editProfileCLI.start();
                break;

            case 0:
                mainDashboard.show();
                break;

            default:
                System.out.println("Invalid choice. Please try again.");
        }

        if (choice != 0) {
            System.out.println("\nPress Enter to continue...");
            utils.FastInput.readLine();
        }
    }
}