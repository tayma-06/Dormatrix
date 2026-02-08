package cli.forms;

import controllers.food.CafeteriaController;
import models.food.MealType;
import models.store.StudentBalance;
import utils.FastInput;
import utils.TimeManager;
import utils.ConsoleColors;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MealTokenPurchase {
    private final CafeteriaController controller = new CafeteriaController();

    public void show(String username) {
        while (true) {
            // 1. Get Live Data
            MealType currentSlot = TimeManager.getCurrentMealSlot();
            String today = LocalDate.now().getDayOfWeek().toString();
            String menuItems = controller.getTodaysMenu(today, currentSlot);
            StudentBalance balance = controller.loadStudentBalance(username);

            // 2. Display Header
            System.out.println("\n=======================================================================");
            System.out.println("|                      DORMATRIX MEAL TOKEN SYSTEM                    |");
            System.out.println("=======================================================================" );
            System.out.println(" Date: " + LocalDate.now() + " | Time: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            System.out.println(" Current Status: " + (currentSlot == MealType.NONE ? "CLOSED" : "ACTIVE - " + currentSlot));
            System.out.println("-----------------------------------------------------------------------");

            if (currentSlot != MealType.NONE) {
                System.out.println(" TODAY'S MENU: " + menuItems );
            } else {
                System.out.println(" Cafeteria is currently closed. Check back during meal hours.");
            }

            System.out.println(" Your Balance: " + (balance != null ? balance.getBalance() : "N/A") + " BDT");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println(" 1. Buy Current Meal Token (" + currentSlot + ")");
            System.out.println(" 2. Pre-order Next Meal");
            System.out.println(" 3. View My Tokens");
            System.out.println(" 0. Back to Dashboard");
            System.out.print("\n Enter choice: ");

            int choice = FastInput.readInt();
            if (choice == 0) break;

            if (choice == 1) {
                if (currentSlot == MealType.NONE) {
                    System.out.println(">> No active meal to buy right now!");
                } else {
                    String result = controller.purchaseToken(username);
                    System.out.println(">> " + result);
                }
                System.out.print("Press Enter to continue...");
                FastInput.readLine();
            }
        }
    }
}