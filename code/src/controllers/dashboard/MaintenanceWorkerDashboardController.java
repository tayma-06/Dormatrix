package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.profile.EditProfileCLI;
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
        utils.ConsoleUtil.clearScreen();
        utils.BackgroundFiller.applyMaintenanceTheme();
        utils.TerminalUI.fillBackground(utils.TerminalUI.getActiveBgColor());
        utils.TerminalUI.at(2, 1);

        String wid = resolveWorkerId(username);

        switch (choice)
        {

            case 1:
                // Show worker field info
                String wfield = getWorkerField(username);
                utils.ConsoleUtil.clearScreen();
                utils.BackgroundFiller.applyMaintenanceTheme();
                utils.TerminalUI.fillBackground(utils.TerminalUI.getActiveBgColor());
                utils.TerminalUI.at(2, 1);

                utils.TerminalUI.tBoxTop();
                utils.TerminalUI.tBoxTitle("YOUR WORK FIELD");
                utils.TerminalUI.tBoxSep();
                utils.TerminalUI.tBoxLine("Worker ID   : " + wid);
                utils.TerminalUI.tBoxLine("Worker Name : " + username);
                utils.TerminalUI.tBoxLine("Work Field  : " + wfield);
                utils.TerminalUI.tBoxSep();
                utils.TerminalUI.tBoxLine(getFieldDescription(wfield));
                utils.TerminalUI.tBoxBottom();
                utils.TerminalUI.tPause();
                break;
            case 2:
                // View assigned tasks
                new cli.complaint.WorkerComplaintCLI().start(username);
                break;
//            case 3:
//                viewComplaintUpdates(username);
//                break;
            case 3:
                new WorkerVisitBoardCLI().show(wid);
                break;
            case 4:
                EditProfileCLI editProfileCLI = new EditProfileCLI(username, "MAINTENANCE_WORKER");
                editProfileCLI.start();
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

    private String getFieldDescription(String field) {
        switch (field) {
            case "ELECTRICIAN":    return "Handles electrical faults, wiring and power issues.";
            case "PLUMBER":        return "Handles plumbing, water leaks and pipe issues.";
            case "INTERNET_TECH":  return "Handles internet connectivity and network issues.";
            case "CLEANING":       return "Handles cleaning and sanitation of dorm facilities.";
            default:               return "General maintenance duties.";
        }
    }


}
