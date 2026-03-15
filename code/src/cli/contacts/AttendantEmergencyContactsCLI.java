package cli.contacts;

import controllers.contacts.EmergencyContactController;
import utils.*;
import utils.TerminalUI.MenuItem;

import static utils.TerminalUI.*;
import static utils.TerminalUIExtras.*;

public class AttendantEmergencyContactsCLI {

    private final EmergencyContactController controller = new EmergencyContactController();

    private static final MenuItem[] MENU = {
            new MenuItem(1, "Update a Contact"),
            new MenuItem(2, "Clear a Contact"),
            new MenuItem(0, "Back"),
    };

    private static final String[] CONTACT_LABELS = {
            "Hall Attendant 1", "Hall Attendant 2", "Ambulance",
            "Medical Centre",   "Pharmacy",         "Fire Service",
            "Security Desk"
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
                TerminalUI.at(2, 1);

                // Show contacts board
                controller.renderBoard();

                // Draw action menu below
                drawDashboard(
                        "EMERGENCY CONTACTS", "",
                        MENU,
                        ConsoleColors.ThemeText.ATTENDANT_TEXT,
                        ConsoleColors.fgRGB(40, 220, 210),
                        null, getCursorRowAfterBoard()
                );

                int choice = readChoiceArrow();
                if (choice == 0) return;

                // Contact picker
                String[] contactOptions = new String[CONTACT_LABELS.length];
                for (int i = 0; i < CONTACT_LABELS.length; i++) {
                    contactOptions[i] = String.format("%-5s%s",
                            "[" + (i + 1) + "]", CONTACT_LABELS[i]);
                }

                ConsoleUtil.clearScreen();
                BackgroundFiller.applyAttendantTheme();
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                TerminalUI.at(2, 1);

                int idx;
                try { idx = tArrowSelect("SELECT CONTACT", contactOptions); }
                catch (InterruptedException e) { continue; }
                if (idx < 0) continue;
                int option = idx + 1;

                ConsoleUtil.clearScreen();
                BackgroundFiller.applyAttendantTheme();
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                TerminalUI.at(2, 1);

                if (choice == 1) {
                    tBoxTop();
                    tBoxTitle("UPDATE: " + CONTACT_LABELS[idx]);
                    tBoxSep();
                    tCustomInputRow("Contact Name : ");
                    String contactName = FastInput.readLine().trim();

                    tBoxTop();
                    tBoxSep();
                    tCustomInputRow("Phone Number : ");
                    String phone = FastInput.readLine().trim();

                    tBoxTop();
                    tBoxSep();
                    tCustomInputRow("Note (opt.)  : ");
                    String note = FastInput.readLine().trim();

                    boolean ok = controller.updateKnownContact(option, contactName, phone, note, username);
                    if (ok) { tBoxTop(); tBoxLine("Contact updated successfully."); tBoxBottom(); }
                    else      tError("Could not update contact.");
                    tPause();

                } else if (choice == 2) {
                    tBoxTop();
                    tBoxTitle("CLEAR: " + CONTACT_LABELS[idx]);
                    tBoxSep();
                    tBoxLine("This will clear all info for this contact.");
                    tBoxBottom();

                    String[] confirm = {"Yes, clear it", "Cancel"};
                    int cidx;
                    try { cidx = tArrowSelect("CONFIRM CLEAR", confirm); }
                    catch (InterruptedException e) { continue; }

                    if (cidx == 0) {
                        boolean ok = controller.clearKnownContact(option, username);
                        if (ok) { tBoxTop(); tBoxLine("Contact cleared."); tBoxBottom(); }
                        else      tError("Could not clear contact.");
                        tPause();
                    }
                }

            } catch (Exception e) {
                TerminalUI.cleanup();
                System.err.println("[AttendantEmergencyContactsCLI] " + e.getMessage());
            }
        }
    }

    private int getCursorRowAfterBoard() {
        // board draws 7 contacts × ~4 lines + 2 header lines ≈ 32 rows from row 2
        return 32;
    }
}