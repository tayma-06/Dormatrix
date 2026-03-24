package cli.views;

import controllers.miscellaneous.LostFoundController;
import java.util.List;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

public class LostFoundView {

    private LostFoundController controller = new LostFoundController();

    public void showMainBoard(String userId, boolean canAddFoundItem) {
        boolean back = false;
        while (!back) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.at(2, 1);

            String[] items;
            if (canAddFoundItem) {
                items = new String[]{
                        "[1] View Found Items",
                        "[2] View Lost Items",
                        "[3] Report Lost Item",
                        "[4] Claim an Item",
                        "[5] Add Found Item (Hall Attendant Only)",
                        "[0] Back to Dashboard"
                };
            } else {
                items = new String[]{
                        "[1] View Found Items",
                        "[2] View Lost Items",
                        "[3] Report Lost Item",
                        "[4] Claim an Item",
                        "[0] Back to Dashboard"
                };
            }
            TerminalUI.tSubDashboard("LOST & FOUND BOARD", items);

            int choice = FastInput.readInt();

            switch (choice) {
                case 1 -> {
                    viewFoundItems();
                    TerminalUI.tPause();
                }
                case 2 -> {
                    viewLostItems();
                    TerminalUI.tPause();
                }
                case 3 -> {
                    reportLost(userId);
                    TerminalUI.tPause();
                }
                case 4 -> {
                    claimItem(userId);
                    TerminalUI.tPause();
                }
                case 5 -> {
                    if (canAddFoundItem) {
                        addFound();
                        TerminalUI.tPause();
                    } else {
                        TerminalUI.tError("You do not have permission.");
                        TerminalUI.tPause();
                    }
                }
                case 0 -> {
                    ConsoleUtil.clearScreen();
                    back = true;
                }
                default -> {
                    TerminalUI.tError("Invalid choice. Please try again.");
                    TerminalUI.tPause();
                }
            }
        }
    }

    // --- UPDATED: Now displays the relational Lost ID ---
    private void viewFoundItems() {
        List<String> items = controller.getFoundItems();
        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("FOUND ITEMS");
        TerminalUI.tBoxSep();
        if (items.isEmpty()) {
            TerminalUI.tBoxLine("No found items reported at this time.");
        } else {
            TerminalUI.tBoxLine("Found ID      | Linked Lost ID | Status");
            TerminalUI.tBoxSep();
            for (String item : items) {
                String[] p = item.split(",");
                // New Format: id, lostItemId, isClaimed, claimantId (4 parts)
                if (p.length == 4) {
                    String status = p[2].equals("true") ? "Claimed by " + p[3] : "Available";
                    TerminalUI.tBoxLine(String.format("%-13s | %-14s | %s", p[0], p[1], status));
                }
            }
        }
        TerminalUI.tBoxBottom();
    }

    private void viewLostItems() {
        List<String> items = controller.getLostItems();
        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("LOST ITEMS");
        TerminalUI.tBoxSep();
        if (items.isEmpty()) {
            TerminalUI.tBoxLine("No lost items reported at this time.");
        } else {
            TerminalUI.tBoxLine("ID            | Name       | Description          | Reporter   | Date");
            TerminalUI.tBoxSep();
            for (String item : items) {
                String[] p = item.split(",");
                if (p.length == 5) {
                    TerminalUI.tBoxLine(String.format("%-13s | %-10s | %-20s | %-10s | %s", p[0], p[1], p[2], p[3], p[4]));
                }
            }
        }
        TerminalUI.tBoxBottom();
    }

    private void reportLost(String userId) {
        TerminalUI.tEmpty();
        TerminalUI.tPrompt("Enter Item Name: ");
        String name = FastInput.readLine();
        TerminalUI.tPrompt("Enter Description (color, brand, etc.): ");
        String desc = FastInput.readLine();

        controller.reportLostItem(name, desc, userId);
        TerminalUI.tSuccess("Item reported. The Hall Office will notify you if it is found.");
    }

    // --- UPDATED: Now only asks for the Lost ID ---
    private void addFound() {
        TerminalUI.tEmpty();
        TerminalUI.tPrompt("Enter the ID of the reported Lost Item (e.g., LID-A1B2C3D4): ");
        String lostId = FastInput.readLine().trim();

        controller.addFoundItem(lostId);
        TerminalUI.tSuccess("Item linked and added to the Found database.");
    }

    private void claimItem(String userId) {
        TerminalUI.tEmpty();
        TerminalUI.tPrompt("Enter the ID of the Found Item (e.g., FID-123456): ");
        String id = FastInput.readLine().trim();

        if (controller.verifyAndClaim(id, userId)) {
            TerminalUI.tSuccess("Claim processed! Visit Hall Office with your Student ID.");
        } else {
            TerminalUI.tError("Invalid ID, or the item has already been claimed.");
        }
    }
}