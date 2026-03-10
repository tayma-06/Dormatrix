package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.schedule.WorkerVisitBoardCLI;
import controllers.authentication.AuthController;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

    private static final String WORKER_FILE = "data/users/maintenance_workers.txt";
    private final AuthController auth = new AuthController();
    private final Scanner sc = new Scanner(System.in);

    private final FileComplaintRepository complaintRepo = new FileComplaintRepository();
    private final FileMaintenanceWorkerRepository workerRepo = new FileMaintenanceWorkerRepository();

    public String getWorkerField(String username) {
        String wid = resolveWorkerId(username);
        MaintenanceWorker worker = workerRepo.findById(wid).orElse(null);

        // Return the worker's field or "Unknown" if the worker is not found
        if (worker != null) {
            return worker.getField().name();
        }
        return "Unknown";
    }

    // Returns false is user chooses to logout
    public void handleInput(int choice, String username)
    {

        String wid = resolveWorkerId(username);
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
            case 3:
                new WorkerVisitBoardCLI().show(wid);
                break;
            case 0:
                mainDashboard.show();
                break;
            default:
                System.out.println("Invalid choice. Please try again");
        }
    }

    private String resolveWorkerId(String target){
        try (BufferedReader br = new BufferedReader(new FileReader(WORKER_FILE))){
            String line;
            while((line = br.readLine()) != null){
                String[] parts = line.split("\\|", -1);
                if (parts.length < 2) continue;

                String id = parts[0].trim().replace("\uFEFF", "");
                String name = parts[1].trim();

                boolean matchesId = id.equals(target.trim());
                boolean matchesName = name.equalsIgnoreCase(target.trim());

                if (matchesId || matchesName) return id;
            }
        } catch (IOException e){
            return null;
        }
        return null;
    }


}
