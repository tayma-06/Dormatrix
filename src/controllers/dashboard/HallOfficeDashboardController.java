package controllers.dashboard;

public class HallOfficeDashboardController {
    public boolean handleInput(int choice, String username){
        switch (choice)
        {
            case 1:
                System.out.println("Updating Student Hall Room Info...");
                // TODO: updates the hall room of the student
                break;
            case 2:
                System.out.println("Viewing Student Complaints...");
                // TODO: views all the pending complaints of the students
                break;
            case 3: 
                System.out.println("Viewing Worker Schedule...");
                // TODO: views the worker schedules
                break;
            case 4:
                System.out.println("Handling The Tasks Of The Attendants...");
                // TODO: lets the office employees handle the tasks of the attendants
                break;
            case 0:
                return false;
            default:
                System.out.println("Invalid choice. Please try again.");

        }
        return true;
    }
}
