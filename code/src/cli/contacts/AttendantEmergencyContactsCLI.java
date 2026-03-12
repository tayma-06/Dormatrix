package cli.contacts;

import cli.Input;
import controllers.contacts.EmergencyContactController;

import java.util.Scanner;

public class AttendantEmergencyContactsCLI {

    private final EmergencyContactController controller = new EmergencyContactController();
    private final Scanner sc = Input.SC;

    public void show(String username) {
        while (true) {
            System.out.println();
            System.out.println(controller.renderBoard());
            System.out.println("1. Update a contact");
            System.out.println("2. Clear a contact");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");

            int choice = readInt();
            if (choice == 0) return;

            if (choice == 1) {
                System.out.println(controller.renderEditMenu());
                System.out.print("Contact option: ");
                int option = readInt();
                if (option == 0) continue;

                String contactName = readLine("Contact name / organization: ");
                String phone = readLine("Phone number: ");
                String note = readLine("Note (optional): ");

                boolean ok = controller.updateKnownContact(option, contactName, phone, note, username);
                System.out.println(ok ? "Emergency contact updated." : "Could not update contact.");
            } else if (choice == 2) {
                System.out.println(controller.renderEditMenu());
                System.out.print("Contact option: ");
                int option = readInt();
                if (option == 0) continue;

                boolean ok = controller.clearKnownContact(option, username);
                System.out.println(ok ? "Emergency contact cleared." : "Could not clear contact.");
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private int readInt() {
        while (true) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;
            try {
                return Integer.parseInt(line);
            } catch (Exception e) {
                System.out.print("Invalid number. Enter again: ");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
}