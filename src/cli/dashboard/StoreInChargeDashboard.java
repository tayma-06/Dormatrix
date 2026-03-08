package cli.dashboard;

import controllers.dashboard.StoreInChargeDashboardController;
import utils.*;
import static utils.TerminalUI.*;

public class StoreInChargeDashboard implements Dashboard {

    private final StoreInChargeDashboardController controller
            = new StoreInChargeDashboardController();
    private boolean firstShow = true;

    private static final String BOX = ConsoleColors.fgRGB(255, 150, 40);   // bright orange box
    private static final String TEXT = ConsoleColors.ThemeText.STORE_TEXT;
    private static final String BG = ConsoleColors.bgRGB(40, 16, 0);
    private static final String MUTED = ConsoleColors.Accent.MUTED;

    private static final MenuItem[] MENU = {
        new MenuItem(1, "Inventory Management"),
        new MenuItem(2, "Process Purchase"),
        new MenuItem(3, "Sales Summary & Reports"),
        new MenuItem(0, "Logout"),};

    @Override
    public void show(String username) {
        if (firstShow) {
            try {
                quickMatrixRain();
            } catch (InterruptedException ignored) {
            }
            firstShow = false;
        }

        while (true) {
            try {
                BackgroundFiller.applyStoreInChargeTheme();
                setActiveTheme(BOX, TEXT, BG);
                System.out.print(HIDE_CUR);

                int menuStartRow = 3;
                int promptRow = drawDashboard(
                        "STORE-IN-CHARGE DASHBOARD",
                        "Welcome, " + username,
                        MENU, TEXT, BOX,
                        null,
                        menuStartRow
                );

                System.out.print(SHOW_CUR);
                int choice = FastInput.readInt();
                System.out.print(RESET);

                if (choice == 0) {
                    BackgroundFiller.applyStoreInChargeTheme();
                    showLogout();
                    BackgroundFiller.resetTheme();
                    return;
                }

                controller.handleInput(choice, username);

            } catch (Exception e) {
                cleanup();
                System.err.println("[StoreInChargeDashboard] " + e.getMessage());
            }
        }
    }
}
