package controllers.dashboard;

import cli.dashboard.MainDashboard;
import controllers.authentication.AuthController;

import java.util.Scanner;

import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;
import libraries.collections.MyString;

import models.complaints.Complaint;
import models.enums.ComplaintStatus;
import models.users.MaintenanceWorker;

import repo.file.FileComplaintRepository;
import repo.file.FileMaintenanceWorkerRepository;

public class MaintenanceWorkerDashboardController {
    MainDashboard mainDashboard = new MainDashboard();

    private final AuthController auth = new AuthController();
    private final Scanner sc = new Scanner(System.in);

    private final FileComplaintRepository complaintRepo = new FileComplaintRepository();
    private final FileMaintenanceWorkerRepository workerRepo = new FileMaintenanceWorkerRepository();
    // Returns false is user chooses to logout
    public void handleInput(int choice, String username)
    {
        switch (choice)
        {
//            case 1:
//                System.out.println("Viewing work field...");
//                viewWorkField(username);
//                break;
//            case 2:
//                System.out.println("Viewing task queue...");
//                taskQueueMenu(username);
//                break;
//            case 3:
//                viewComplaintUpdates(username);
//                break;
            case 0:
                mainDashboard.show();
                break;
            default:
                System.out.println("Invalid choice. Please try again");
        }
    }


}
