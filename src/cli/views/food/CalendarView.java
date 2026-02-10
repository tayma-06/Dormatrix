package cli.views.food;

import controllers.food.CafeteriaController;
import models.food.MealType;

import java.time.LocalDate;
import java.time.DayOfWeek;

public class CalendarView {

    private final CafeteriaController controller = new CafeteriaController();

    public void renderWeeklyCalendar(LocalDate today) {
        System.out.println();
        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                          WEEKLY MEAL PLAN                           ║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
        System.out.println("║               Mon | Tue | Wed | Thu | Fri | Sat | Sun               ║");

        System.out.print("║               ");
        for (int i = 0; i < 7; i++) {
            LocalDate day = today.plusDays(i);
            String status = (day.isBefore(LocalDate.now())) ? " X " : String.format(" %s ", day.getDayOfMonth());
            System.out.print(status + "|");
        }
        System.out.println("\n╚═════════════════════════════════════════════════════════════════════╝");
    }

    public void showWeeklyMenuAndPurchaseTokens(String username, LocalDate today) {
        renderWeeklyCalendar(today);

        for (int i = 0; i < 7; i++) {
            LocalDate day = today.plusDays(i);
            if (day.isBefore(LocalDate.now())) {
                System.out.println("\n>> " + day + " - Past Day (No Purchase Available)");
                continue;
            }
            DayOfWeek dayOfWeek = day.getDayOfWeek();
            System.out.println("\n>> " + dayOfWeek + " (" + day + ")");

            // Show the weekly menu for the selected day
            String dayMenu = controller.getMenuForTime(day, dayOfWeek.toString(), MealType.BREAKFAST);
            System.out.println("   Breakfast: " + dayMenu);
            dayMenu = controller.getMenuForTime(day, dayOfWeek.toString(), MealType.LUNCH);
            System.out.println("   Lunch: " + dayMenu);
            dayMenu = controller.getMenuForTime(day, dayOfWeek.toString(), MealType.DINNER);
            System.out.println("   Dinner: " + dayMenu);

            // Allow user to buy tokens for the day
            System.out.print("   Would you like to buy tokens for this day? (yes/no): ");
            String answer = utils.FastInput.readLine().trim().toLowerCase();
            if (answer.equals("yes") || answer.equals("y")) {
                buyDayToken(username, day);
            }
        }
    }

    private void buyDayToken(String username, LocalDate day) {
        System.out.println("Buying tokens for: " + day);
        for (MealType mealType : MealType.values()) {
            if (mealType == MealType.NONE) continue;

            System.out.print("   Would you like to buy " + mealType + " token? (yes/no): ");
            String answer = utils.FastInput.readLine().trim().toLowerCase();
            if (answer.equals("yes") || answer.equals("y")) {
                String result = controller.purchaseTokenForDay(username, day, mealType);
                System.out.println(">> " + result);
            }
        }
    }
}
