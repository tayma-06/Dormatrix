package cli.announcement;

import controllers.announcement.AnnouncementController;
import libraries.collections.MyArrayList;
import models.announcements.Announcement;
import utils.*;
import utils.TerminalUI.MenuItem;

import static utils.TerminalUI.*;
import static utils.TerminalUIExtras.*;

public class AttendantAnnouncementCLI {

    private final AnnouncementController controller = new AnnouncementController();

    private static final MenuItem[] MENU = {
            new MenuItem(1, "View All Announcements"),
            new MenuItem(2, "Post Announcement"),
            new MenuItem(3, "Update Announcement"),
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
                    controller.renderAllBoard();
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

                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyAttendantTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);

                    tBoxTop();
                    tBoxTitle("POST ANNOUNCEMENT");
                    tBoxSep();
                    tBoxLine("Title: " + title);
                    tBoxSep();
                    tBoxLine("Expiry date format: YYYY-MM-DD  e.g. 2026-04-01");
                    tBoxLine("Leave blank = never expires.");
                    tBoxLine("  [ESC] Cancel and go back", ConsoleColors.fgRGB(160, 150, 60));
                    tBoxSep();
                    tCustomInputRow("Expires  : ");
                    String expiresAt = readLineOrEsc();
                    if (expiresAt == null) continue;

                    // Validate date format if not blank
                    if (!expiresAt.isEmpty()) {
                        try {
                            java.time.LocalDate.parse(expiresAt.trim());
                        } catch (Exception e) {
                            tError("Invalid date format. Use YYYY-MM-DD.");
                            tPause();
                            continue;
                        }
                    }

                    controller.postAnnouncement(username, title, body, expiresAt);
                    tBoxTop();
                    tBoxLine("Announcement posted successfully.");
                    tBoxBottom();
                    tPause();
                } else if (ch == 3) {
                    MyArrayList<Announcement> all = controller.getAllNewestFirst();
                    if (all.size() == 0) {
                        tBoxTop();
                        tBoxLine("No announcements to update.");
                        tBoxBottom();
                        tPause();
                        continue;
                    }

                    // Build picker labels
                    String[] annLabels = new String[all.size()];
                    for (int i = 0; i < all.size(); i++) {
                        Announcement a = all.get(i);
                        String expired = a.isExpired() ? " [EXPIRED]" : "";
                        annLabels[i] = String.format("%-5s%-30s %s%s",
                                "[" + (i + 1) + "]",
                                a.getTitle().length() > 28
                                        ? a.getTitle().substring(0, 28) + ".." : a.getTitle(),
                                a.getCreatedAt(),
                                expired);
                    }

                    int idx;
                    try { idx = tArrowSelect("SELECT ANNOUNCEMENT TO UPDATE", annLabels); }
                    catch (InterruptedException e) { continue; }
                    if (idx < 0) continue;

                    Announcement selected = all.get(idx);

                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyAttendantTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);

                    tBoxTop();
                    tBoxTitle("UPDATE ANNOUNCEMENT");
                    tBoxSep();
                    tBoxLine("Current title  : " + selected.getTitle());
                    tBoxLine("Current expiry : " + (selected.getExpiresAt().isEmpty()
                            ? "No expiry" : selected.getExpiresAt()));
                    tBoxSep();
                    tBoxLine("Leave any field blank to keep current value.");
                    tBoxLine("  [ESC] Cancel and go back", ConsoleColors.fgRGB(160, 150, 60));
                    tBoxSep();
                    tCustomInputRow("New Title    : ");
                    String newTitle = readLineOrEsc();
                    if (newTitle == null) continue;

                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyAttendantTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);

                    tBoxTop();
                    tBoxTitle("UPDATE ANNOUNCEMENT");
                    tBoxSep();
                    tBoxLine("Current message: " + selected.getBody());
                    tBoxSep();
                    tBoxLine("  [ESC] Cancel and go back", ConsoleColors.fgRGB(160, 150, 60));
                    tBoxSep();
                    tCustomInputRow("New Message  : ");
                    String newBody = readLineOrEsc();
                    if (newBody == null) continue;

                    ConsoleUtil.clearScreen();
                    BackgroundFiller.applyAttendantTheme();
                    TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                    TerminalUI.at(2, 1);

                    tBoxTop();
                    tBoxTitle("UPDATE ANNOUNCEMENT");
                    tBoxSep();
                    tBoxLine("Format: YYYY-MM-DD  e.g. 2026-04-01");
                    tBoxLine("Leave blank to keep current expiry.");
                    tBoxLine("  [ESC] Cancel and go back", ConsoleColors.fgRGB(160, 150, 60));
                    tBoxSep();
                    tCustomInputRow("New Expiry   : ");
                    String newExpiry = readLineOrEsc();
                    if (newExpiry == null) continue;

                    if (!newExpiry.isEmpty()) {
                        try { java.time.LocalDate.parse(newExpiry.trim()); }
                        catch (Exception e) {
                            tError("Invalid date format. Use YYYY-MM-DD.");
                            tPause();
                            continue;
                        }
                    }

                    // Pass null for blank fields to keep existing values
                    boolean ok = controller.updateAnnouncement(
                            selected.getAnnouncementId(),
                            newTitle.isEmpty()  ? null : newTitle,
                            newBody.isEmpty()   ? null : newBody,
                            newExpiry.isEmpty() ? null : newExpiry
                    );

                    if (ok) { tBoxTop(); tBoxLine("Announcement updated successfully."); tBoxBottom(); }
                    else tError("Failed to update announcement.");
                    tPause();
                }

            } catch (Exception e) {
                TerminalUI.cleanup();
                System.err.println("[AttendantAnnouncementCLI] " + e.getMessage());
            }
        }
    }
}