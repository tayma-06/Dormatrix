package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.views.*;

public class StoreInChargeDashboardController {
    MainDashboard mainDashboard = new MainDashboard();
    PurchaseView purchaseView = new PurchaseView(); 
    InventoryListView inventoryList = new InventoryListView();

    public void handleInput(int choice, String username)
    {
        switch(choice)
        {
            case 1: 
                inventoryList.show();

                break;
            case 2:
                purchaseView.show();  

                break;

            case 3:
                System.out.println("Opening Sales Summary...");
                // TODO: goes to sales summary class to generate and show sales summary
                break;
            case 0:
                mainDashboard.show();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                
        }
    }
}
