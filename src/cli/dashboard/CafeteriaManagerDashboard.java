package cli.dashboard;

import controllers.food.*;
import models.food.DailyMenu;
import models.food.MealType;
import utils.CafeteriaAsciiUI;
import utils.FastInput;
import utils.TimeManager;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CafeteriaManagerDashboard implements Dashboard {
    private final CafeteriaController controller = new CafeteriaController();
    private final MealTokenController tokenController = new MealTokenController();

    @Override
    public void show(String username) {
        while (true) {
            String nowLine = "Now: " + TimeManager.nowDate() + " " + TimeManager.nowTime()
                    + " | Slot: " + TimeManager.getCurrentMealSlot()
                    + " | Ramadan: " + TimeManager.isRamadanMode()
                    + " | Demo: " + (TimeManager.isDemoMode() ? "ON" : "OFF");

            System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                     CAFETERIA MANAGER DASHBOARD                     ║");
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");

            String welcomeMessage = "Welcome, " + username;
            int totalWidth = 69;
            int paddingLeft = (totalWidth - welcomeMessage.length()) / 2;
            int paddingRight = totalWidth - welcomeMessage.length() - paddingLeft;
            String formattedWelcome =
                    String.format("║%" + paddingLeft + "s%s%" + paddingRight + "s║", "", welcomeMessage, "");
            System.out.println(formattedWelcome);

            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            System.out.println(String.format("║ %-67s ║", nowLine));
            System.out.println(String.format("║ %-67s ║", CafeteriaAsciiUI.renderSlotProgress(TimeManager.getCurrentMealSlot())));
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");

            System.out.println("║ [1] Update Weekly Menu                                              ║");
            System.out.println("║ [2] Schedule Special Event                                          ║");
            System.out.println("║ [3] Verify Student Token                                            ║");
            System.out.println("║ [4] Toggle Ramadan Mode                                             ║");
            System.out.println("║ [5] Toggle Demo Mode (Fast Day)                                     ║");
            System.out.println("║ [0] Logout                                                          ║");
            System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

            System.out.print("\nEnter your choice: ");
            int choice = FastInput.readInt();

            if (choice == 0) {
                System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
                System.out.println("║                         Logging Out....                             ║");
                System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
                return;
            }

            switch (choice) {
                case 1 -> updateWeeklyMenu();
                case 2 -> scheduleCalendar();
                case 3 -> verifyTokenLoop();
                case 4 -> toggleRamadan();
                case 5 -> toggleDemoMode();
                default -> System.out.println("Invalid Selection!");
            }

            System.out.print("\nPress Enter to continue...");
            FastInput.readLine();
        }
    }

    private void updateWeeklyMenu() {
        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
        MealType[] types = {MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER};
        List<DailyMenu> newMenu = new ArrayList<>();

        System.out.println("\n--- Set Weekly Menu ---");
        for (String day : days) {
            System.out.println(">> " + day);
            for (MealType type : types) {
                System.out.print("   " + type + " items: ");
                String items = FastInput.readNonEmptyLine();
                newMenu.add(new DailyMenu(day, type, items));
            }
        }
        controller.saveMenu(newMenu);
        System.out.println("Success: Weekly menu updated!");
    }

    private void scheduleCalendar() {
        try {
            System.out.print("Enter Date (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(FastInput.readNonEmptyLine());

            System.out.println("Select Meal Type: 1.LUNCH 2.DINNER");
            System.out.print("Choice: ");
            int typeChoice = FastInput.readInt();

            MealType type = (typeChoice == 2) ? MealType.DINNER : MealType.LUNCH;

            System.out.print("Enter Special Menu Items: ");
            String items = FastInput.readNonEmptyLine();

            controller.scheduleSpecialMeal(date, type, items);
            System.out.println("Success: Event added to calendar.");
        } catch (DateTimeParseException e) {
            System.out.println("Error: Invalid date format.");
        }
    }

    private void verifyTokenLoop() {
        while (true) {
            System.out.print("Enter Token ID (or 0 to back): ");
            String id = FastInput.readNonEmptyLine();
            if (id.equals("0")) return;

            System.out.println(">> " + tokenController.verifyAndUseToken(id));
        }
    }

    private void toggleRamadan() {
        System.out.print("Enable Ramadan Mode? (true/false): ");
        boolean isRamadan = FastInput.readBoolean();
        TimeManager.setRamadanMode(isRamadan);
        controller.setSystemMode(isRamadan);
        System.out.println("System Mode updated.");
    }

    private void toggleDemoMode() {
        boolean newState = !TimeManager.isDemoMode();
        TimeManager.setDemoMode(newState);
        System.out.println("Demo Mode is now: " + (newState ? "ON" : "OFF"));
    }
}
