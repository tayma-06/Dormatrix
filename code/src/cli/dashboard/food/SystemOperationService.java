package cli.dashboard.food;

import controllers.food.OperationController;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;
import utils.TimeManager;

public class SystemOperationService {

    private final OperationController opController = new OperationController();

    public void showTokenVerificationUI() {
        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);

            TerminalUI.tBoxTop();
            TerminalUI.tBoxTitle("MEAL TOKEN VERIFICATION");
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("Scan or enter a token ID to verify and consume it.");
            TerminalUI.tBoxLine("Enter 0 to go back.");
            TerminalUI.tBoxBottom();

            TerminalUI.tEmpty();
            TerminalUI.tPrompt("Enter Token ID: ");
            String id = FastInput.readNonEmptyLine();
            if (id.equals("0")) {
                ConsoleUtil.clearScreen();
                return;
            }

            String result = opController.processTokenVerification(id);

            TerminalUI.tEmpty();
            if (result.toLowerCase().contains("success") || result.toLowerCase().contains("verified") || result.toLowerCase().contains("used")) {
                TerminalUI.tSuccess(result);
            } else {
                TerminalUI.tError(result);
            }
            TerminalUI.tPause();
        }
    }

    public void showRamadanToggleUI() {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("RAMADAN MODE SETTINGS");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Current Mode: " + (TimeManager.isRamadanMode() ? "RAMADAN" : "NORMAL"));
        TerminalUI.tBoxBottom();

        TerminalUI.tEmpty();
        TerminalUI.tPrompt("Enable Ramadan Mode? (true/false): ");
        boolean isRamadan = FastInput.readBoolean();
        String result = opController.processRamadanModeToggle(isRamadan);
        TerminalUI.tEmpty();
        TerminalUI.tSuccess(result);
    }
}
