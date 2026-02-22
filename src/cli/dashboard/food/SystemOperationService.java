package cli.dashboard.food;

import controllers.food.CafeteriaController;
import controllers.food.MealTokenController;
import utils.FastInput;
import utils.TimeManager;

public class SystemOperationService {
    private final MealTokenController tokenController;
    private final CafeteriaController cafeteriaController;

    public SystemOperationService(MealTokenController tokenController, CafeteriaController cafeteriaController) {
        this.tokenController = tokenController;
        this.cafeteriaController = cafeteriaController;
    }

    public void verifyTokenLoop() {
        while (true) {
            System.out.print("Enter Token ID (or 0 to back): ");
            String id = FastInput.readNonEmptyLine();
            if (id.equals("0")) return;

            System.out.println(">> " + tokenController.verifyAndUseToken(id));
        }
    }

    public void toggleRamadanMode() {
        System.out.print("Enable Ramadan Mode? (true/false): ");
        boolean isRamadan = FastInput.readBoolean();
        TimeManager.setRamadanMode(isRamadan);
        cafeteriaController.setSystemMode(isRamadan);
        System.out.println("[System] Mode updated successfully.");
    }
}