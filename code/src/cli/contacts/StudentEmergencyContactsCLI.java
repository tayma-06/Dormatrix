package cli.contacts;

import cli.Input;
import controllers.contacts.EmergencyContactController;

import java.util.Scanner;

public class StudentEmergencyContactsCLI {

    private final EmergencyContactController controller = new EmergencyContactController();
    private final Scanner sc = Input.SC;

    public void show() {
        utils.ConsoleUtil.clearScreen();
        controller.renderBoard();
        utils.TerminalUI.tPause();
    }
}