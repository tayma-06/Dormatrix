package controllers.dashboard;

import cli.dashboard.MainDashboard;
import cli.forms.CreateAccount;
import cli.forms.DeleteAccount;
import cli.views.ViewAccount;
import cli.views.SearchUser;
import controllers.authentication.AccountManager;
import controllers.room.RoomController;
import utils.FastInput;

public class AdminDashboardController {
    private final MainDashboard mainDashboard;
    private final AccountManager accountManager;
    private final RoomController roomController;

    private final CreateAccount createAccountForm;
    private final DeleteAccount deleteAccountForm;
    private final ViewAccount viewAccountForm;
    private final SearchUser searchUserForm;

    public AdminDashboardController() {
        this.mainDashboard = new MainDashboard();
        this.accountManager = new AccountManager();
        this.roomController = new RoomController();

        this.createAccountForm = new CreateAccount(accountManager);
        this.deleteAccountForm = new DeleteAccount(accountManager);
        this.viewAccountForm = new ViewAccount(accountManager);
        this.searchUserForm = new SearchUser(accountManager);
    }

    public void handleInput(int choice, String username) {
        switch (choice) {
            case 1 -> createAccountForm.show();
            case 2 -> deleteAccountForm.show();
            case 3 -> handleViewMenu();
            case 4 -> handleRoomMenu();
            case 0 -> mainDashboard.show();
            default -> System.out.println("Invalid choice. Please try again...");
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

        int viewChoice = FastInput.readInt();
        switch (viewChoice) {
            case 1 -> viewAccountForm.show();
            case 2 -> searchUserForm.show();
            case 0 -> { return; }
            default -> System.out.println("Invalid choice!");
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

            int roomChoice = FastInput.readInt();
            switch (roomChoice) {
                case 1 -> addNewRoomFlow();
                case 2 -> {
                    roomController.showAvailableRooms();
                    System.out.println("\nPress Enter to continue...");
                    FastInput.readLine();
                }
                case 0 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void addNewRoomFlow() {
        System.out.println("\n--- Add New Room ---");
        System.out.print("Enter Room Number/ID : ");
        String roomId = FastInput.readNonEmptyLine();
        if (roomId.isEmpty()) {
            System.out.println("Error: Room ID cannot be empty.");
            return;
        }

        System.out.print("Enter Room Capacity (e.g., 4): ");
        int capacity = FastInput.readInt();
        roomController.addRoom(roomId, capacity);
    }
}
