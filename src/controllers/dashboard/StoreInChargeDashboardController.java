package controllers.dashboard;

public class StoreInChargeDashboardController {
    // Returns false if user logs out
    public boolean handleInput(int choice, String username)
    {
        switch(choice)
        {
            case 1: 
                System.out.println("Opening Inventory List...");
                // TODO: call Inventory class to show inventory list
                break;
            case 2:
                System.out.println("Opening Purchase Record...");
                // TODO: goes to purchase record class to show purchase record
                break;
            case 3:
                System.out.println("Opening Sales Summary...");
                // TODO: goes to sales summary class to generate and show sales summary
                break;
            case 0:
                return false;
            default:
                System.out.println("Invalid choice. Please try again.");
                
        }
        return true;
    }
}
