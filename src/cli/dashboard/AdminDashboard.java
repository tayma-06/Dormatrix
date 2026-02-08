package cli.dashboard;

import controllers.dashboard.AdminDashboardController;
import utils.BackgroundFiller;
import utils.ConsoleUtil;
import utils.FastInput;

public class AdminDashboard implements Dashboard {

    private final AdminDashboardController controller = new AdminDashboardController();

    @Override
    public void show(String username) {
        while (true) {
            ConsoleUtil.clearScreen();
            BackgroundFiller.applyAdminTheme();
            System.out.println();
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("┃                           ADMIN DASHBOARD                           ┃");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // Center the "Welcome" message within a width of 67 characters
            String welcomeMessage = "Welcome, " + username;
            int padding = (69 - welcomeMessage.length()) / 2; // Calculate the padding to center the text
            String formattedWelcome = String.format("┃%" + padding + "s%s%" + padding + "s┃", "", welcomeMessage, "");

            System.out.println(formattedWelcome);
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("┃ [1] Create Account                                                  ┃");
            System.out.println("┃ [2] Delete Account                                                  ┃");
            System.out.println("┃ [3] View & Search Accounts                                          ┃");
            System.out.println("┃ [4] Manage Rooms                                                    ┃");
            System.out.println("┃ [0] Logout                                                          ┃");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println();

            // Prompt for user input
            System.out.print("Enter your choice: ");
            int choice = FastInput.readInt();

            if (choice == 0) {
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("┃ Logging Out....                                                     ┃");
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                BackgroundFiller.resetTheme();
                return;
            }

            // Handle user input
            controller.handleInput(choice, username);
            ConsoleUtil.pause();
        }
    }
}
