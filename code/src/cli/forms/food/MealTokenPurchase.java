package cli.forms.food;

import cli.views.food.CalendarView;
import cli.views.food.TokenListView;
import controllers.food.TokenPurchaseController;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import models.food.MealType;
import models.store.StudentBalance;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;
import utils.TimeManager;

public class MealTokenPurchase {

    private final TokenPurchaseController purchaseController = new TokenPurchaseController();
    private final CalendarView calendarView = new CalendarView();

    public void show(String username) {
        while (true) {
            utils.CafeteriaAsciiUI.stopTokenScreenAnimation();
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);
            LocalDate todayDate = TimeManager.nowDate();
            MealType currentSlot = TimeManager.getCurrentMealSlot();
            String dayOfWeek = TimeManager.nowDay().toString();
            String menuItems = purchaseController.getMenuForTime(todayDate, dayOfWeek, currentSlot);
            StudentBalance balance = purchaseController.getStudentBalance(username);
            TerminalUI.tBoxTop();
            TerminalUI.tBoxTitle("MEAL TOKEN PURCHASE");
            TerminalUI.tBoxSep();

            String dateTimeLine = "Date: " + todayDate
                    + " | Time: " + TimeManager.nowTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            TerminalUI.tBoxLine(dateTimeLine);

            String statusLine = "Current Status: "
                    + (currentSlot == MealType.NONE ? "CLOSED" : "ACTIVE - " + currentSlot);
            TerminalUI.tBoxLine(statusLine);

            String progressLine = utils.CafeteriaAsciiUI.renderSlotProgress(currentSlot);
            TerminalUI.tBoxLine(progressLine);

            TerminalUI.tBoxSep();

            if (currentSlot != MealType.NONE) {
                String menuLine = "Today's Menu: " + menuItems;
                for (String line : ConsoleUtil.wrapText(menuLine, 67)) {
                    TerminalUI.tBoxLine(line);
                }
            } else {
                TerminalUI.tBoxLine("Cafeteria is currently closed.");
            }

            String balanceLine = "Your Balance: "
                    + (balance != null ? balance.getBalance() : "N/A") + " BDT";
            TerminalUI.tBoxLine(balanceLine);

            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("[1] Buy Current Meal Token");
            TerminalUI.tBoxLine("[2] Buy Tokens for the Week (Monday-Sunday)");
            TerminalUI.tBoxLine("[3] View My Tokens");
            TerminalUI.tBoxLine("[0] Back to Dashboard", utils.ConsoleColors.Accent.EXIT);
            TerminalUI.tBoxBottom();
            TerminalUI.tEmpty();
            TerminalUI.tPrompt("Enter choice: ");

            utils.CafeteriaAsciiUI.startTokenScreenAnimation(5);
            int choice = FastInput.readInt();
            utils.CafeteriaAsciiUI.stopTokenScreenAnimation();
            if (choice == 0) {
                ConsoleUtil.clearScreen();
                break;
            }

            boolean skipOuterPause = false;

            switch (choice) {
                case 1 -> {
                    if (currentSlot == MealType.NONE) {
                        TerminalUI.tError("No active meal to buy right now!");
                    } else {
                        String result = purchaseController.processTokenPurchase(username);
                        TerminalUI.tSuccess(result);
                    }
                }
                case 2 -> {
                    calendarView.showWeeklyMenuAndPurchaseTokens(username, todayDate);
                    skipOuterPause = true;
                }
                case 3 -> {
                    TokenListView tokenListView = new TokenListView();
                    tokenListView.show(username);
                    skipOuterPause = true;
                }
                default ->
                    TerminalUI.tError("Invalid choice.");
            }

            if (!skipOuterPause) {
                TerminalUI.tPause();
            }
        }
    }
}
