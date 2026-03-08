package cli.announcement;

import cli.Input;
import controllers.announcement.AnnouncementController;

import java.util.Scanner;

public class AttendantAnnouncementCLI {

    private final AnnouncementController controller = new AnnouncementController();
    private final Scanner sc = Input.SC;

    public void show(String username) {
        while (true) {
            System.out.println();
            System.out.println("1. View announcements");
            System.out.println("2. Post announcement");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");

            int ch = readInt();
            if (ch == 0) return;

            if (ch == 1) {
                System.out.println(controller.renderBoard());
            } else if (ch == 2) {
                String title = readNonEmpty("Title: ");
                String body = readNonEmpty("Message: ");
                controller.postAnnouncement(username, title, body);
                System.out.println("Announcement posted.");
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

    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (!line.isEmpty()) return line;
            System.out.println("Input can not be empty.");
        }
    }
}