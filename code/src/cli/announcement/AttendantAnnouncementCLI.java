package cli.announcement;

import controllers.announcement.AnnouncementController;
import utils.*;
import utils.TerminalUI.MenuItem;

import static utils.TerminalUI.*;
import static utils.TerminalUIExtras.*;

public class AttendantAnnouncementCLI {

    private final AnnouncementController controller = new AnnouncementController();

    private static final MenuItem[] MENU = {
            new MenuItem(1, "View Announcements"),
            new MenuItem(2, "Post Announcement"),
            new MenuItem(0, "Back"),
    };

    public void show(String username) {
        while (true) {
            try {
                ConsoleUtil.clearScreen();
                BackgroundFiller.applyAttendantTheme();
                TerminalUI.setActiveTheme(
                        ConsoleColors.fgRGB(40, 220, 210),
                        ConsoleColors.ThemeText.ATTENDANT_TEXT,
                        ConsoleColors.bgRGB(0, 28, 26)
                );
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());

                drawDashboard(
                        "ANNOUNCEMENTS", "",
                        MENU,
                        ConsoleColors.ThemeText.ATTENDANT_TEXT,
                        ConsoleColors.fgRGB(40, 220, 210),
                        null, 3
                );

                int ch = readChoiceArrow();

                if (ch == 0) return;

                ConsoleUtil.clearScreen();
                BackgroundFiller.applyAttendantTheme();
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                TerminalUI.at(2, 1);

                if (ch == 1) {
                    controller.renderBoard();
                    tPause();

                } else if (ch == 2) {
                    tBoxTop();
                    tBoxTitle("POST ANNOUNCEMENT");
                    tBoxSep();
                    tBoxLine("Enter announcement details below.");
                    tBoxSep();
                    tBoxLine("  [ESC] Cancel and go back", ConsoleColors.fgRGB(160, 150, 60));  // ← add this
                    tBoxSep();
                    tCustomInputRow("Title  : ");
                    String title = readLineOrEsc();
                    if (title == null) continue;   // ESC pressed → go back to menu

                    if (title.isEmpty()) {
                        tError("Title cannot be empty.");
                        tPause();
                        continue;
                    }

                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyAttendantTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);

                    tBoxTop();
                    tBoxTitle("POST ANNOUNCEMENT");
                    tBoxSep();
                    tBoxLine("Title: " + title);
                    tBoxSep();
                    tBoxLine("  [ESC] Cancel and go back", ConsoleColors.fgRGB(160, 150, 60));  // ← add this
                    tBoxSep();
                    tCustomInputRow("Message: ");
                    String body = readLineOrEsc();
                    if (body == null) continue;    // ESC pressed → go back to menu

                    if (body.isEmpty()) {
                        tError("Message cannot be empty.");
                        tPause();
                        continue;
                    }

                    controller.postAnnouncement(username, title, body);
                    tBoxTop();
                    tBoxLine("Announcement posted successfully.");
                    tBoxBottom();
                    tPause();
                }

            } catch (Exception e) {
                TerminalUI.cleanup();
                System.err.println("[AttendantAnnouncementCLI] " + e.getMessage());
            }
        }
    }
}