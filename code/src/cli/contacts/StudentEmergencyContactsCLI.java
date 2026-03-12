package cli.contacts;

import cli.Input;
import controllers.contacts.EmergencyContactController;

import java.util.Scanner;

public class StudentEmergencyContactsCLI {

    private final EmergencyContactController controller = new EmergencyContactController();
    private final Scanner sc = Input.SC;

    public void show() {
        System.out.println();
        System.out.println(controller.renderBoard());
        System.out.print("Press Enter to go back...");
        sc.nextLine();
    }
}