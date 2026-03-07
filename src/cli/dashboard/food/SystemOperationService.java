package cli.dashboard.food;

import controllers.food.OperationController;
import utils.ConsoleUtil;
import utils.FastInput;

public class SystemOperationService {

    private final OperationController opController = new OperationController();

    public void showTokenVerificationUI() {
        while (true) {
            ConsoleUtil.clearScreen();
            System.out.print("Enter Token ID (or 0 to back): ");
            String id = FastInput.readNonEmptyLine();
            if (id.equals("0")) {
                ConsoleUtil.clearScreen();
                return;
            }

            String result = opController.processTokenVerification(id);
            System.out.println(">> " + result);
        }
    }

    public void showRamadanToggleUI() {
        ConsoleUtil.clearScreen();
        System.out.print("Enable Ramadan Mode? (true/false): ");
        boolean isRamadan = FastInput.readBoolean();
        String result = opController.processRamadanModeToggle(isRamadan);
        System.out.println(result);
        ConsoleUtil.clearScreen();
    }
}
