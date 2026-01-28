package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.forms.*;
import cli.views.*;
import controllers.authentication.AccountManager;
import controllers.room.RoomController; // Import RoomController

import java.util.Scanner;

public class AdminDashboardController {
    private final MainDashboard mainDashboard;
    private final AccountManager accountManager;
    private final RoomController roomController; // Add RoomController field
    private final Scanner scanner;

    private final CreateAccount createAccountForm;
    private final DeleteAccount deleteAccountForm;
    private final ViewAccount viewAccountForm;
    private final SearchUser searchUserForm;

    public AdminDashboardController() {
        this.mainDashboard = new MainDashboard();
        this.accountManager = new AccountManager();
        this.roomController = new RoomController(); // Initialize RoomController
        this.scanner = new Scanner(System.in);

        this.createAccountForm = new CreateAccount(accountManager, scanner);
        this.deleteAccountForm = new DeleteAccount(accountManager, scanner);
        this.viewAccountForm = new ViewAccount(accountManager, scanner);
        this.searchUserForm = new SearchUser(accountManager, scanner);
    }

    public void handleInput(int choice, String username) {
        switch (choice) {
            case 1:
                createAccountForm.show();
                break;
            case 2:
                deleteAccountForm.show();
                break;
            case 3:
                handleViewMenu();
                break;
            case 4:
                handleRoomMenu(); // New Case for Rooms
                break;
            case 0:
                mainDashboard.show();
                break;
            default:
                System.out.println("Invalid choice. Please try again...");
        }
    }
    private void handleViewMenu() {
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("|                      View and Search Accounts                       |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("| 1. View Accounts (By Role or All)                                   |");
        System.out.println("| 2. Search User by ID                                                |");
        System.out.println("| 0. Back                                                             |");
        System.out.println("-----------------------------------------------------------------------");
        System.out.print("Enter choice: ");

        if (scanner.hasNextInt()) {
            int viewChoice = scanner.nextInt();
            scanner.nextLine();
            switch (viewChoice) {
                case 1:
                    viewAccountForm.show();
                    break;
                case 2:
                    searchUserForm.show();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        } else {
            scanner.nextLine();
        }
    }
    private void handleRoomMenu() {
        while (true) {
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("|                           MANAGE ROOMS                              |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("| 1. Add New Room                                                     |");
            System.out.println("| 2. View Available Rooms                                             |");
            System.out.println("| 0. Back                                                             |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.print("Enter choice: ");
            if (scanner.hasNextInt()) {
                int roomChoice = scanner.nextInt();
                scanner.nextLine();

                switch (roomChoice) {
                    case 1:
                        addNewRoomFlow();
                        break;
                    case 2:
                        roomController.showAvailableRooms();
                        System.out.println("\nPress Enter to continue...");
                        scanner.nextLine();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Invalid choice!");
                }
            } else {
                System.out.println("Invalid input.");
                scanner.nextLine();
            }
        }
    }

    private void addNewRoomFlow() {
        System.out.println("\n--- Add New Room ---");
        System.out.print("Enter Room Number/ID : ");
        String roomId = scanner.nextLine().trim();
        if (roomId.isEmpty()) {
            System.out.println("Error: Room ID cannot be empty.");
            return;
        }
        System.out.print("Enter Room Capacity (e.g., 4): ");
        if (scanner.hasNextInt()) {
            int capacity = scanner.nextInt();
            scanner.nextLine();
            roomController.addRoom(roomId, capacity);
        } else {
            System.out.println("Error: Capacity must be a number.");
            scanner.nextLine();
        }
    }
}