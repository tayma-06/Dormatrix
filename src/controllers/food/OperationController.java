package controllers.food;

import controllers.food.CafeteriaController;
import controllers.food.MealTokenController;
import utils.FastInput;
import utils.TimeManager;

public class OperationController {
    private final MealTokenController tokenData = new MealTokenController();
    private final CafeteriaController cafeteriaData = new CafeteriaController();

    public void processTokenVerification() {
        while (true) {
            System.out.print("Enter Token ID (or 0 to back): ");
            String id = FastInput.readNonEmptyLine();
            if (id.equals("0")) return;

            System.out.println(">> " + tokenData.verifyAndUseToken(id));
        }
    }

    public void manageRamadanMode() {
        System.out.print("Enable Ramadan Mode? (true/false): ");
        boolean isRamadan = FastInput.readBoolean();
        TimeManager.setRamadanMode(isRamadan);
        cafeteriaData.setSystemMode(isRamadan);
        System.out.println("System Mode updated.");
    }
}