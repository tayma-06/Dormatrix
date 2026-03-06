package cli.views;

import controllers.miscellaneous.LostFoundController;
import java.util.List;
import java.util.Scanner;

public class LostFoundView {
    private LostFoundController controller = new LostFoundController();
    private Scanner scanner = new Scanner(System.in);

    public void showMainBoard(String userId, boolean canAddFoundItem) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--------------------------------------------------");
            System.out.println("|              LOST & FOUND BOARD                |");
            System.out.println("--------------------------------------------------");
            System.out.println("1. View Found Items");
            System.out.println("2. View Lost Items");
            System.out.println("3. Report Lost Item");
            System.out.println("4. Claim an Item");
            if (canAddFoundItem) {
                System.out.println("5. Add Found Item (Hall Attendant Only)");
            }
            System.out.println("0. Back to Dashboard");
            System.out.println("--------------------------------------------------");

            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the leftover newline from nextInt()

            switch(choice) {
                case 1 -> viewFoundItems();
                case 2 -> viewLostItems();
                case 3 -> reportLost(userId);
                case 4 -> claimItem(userId);
                case 5 -> {
                    if (canAddFoundItem) addFound();
                    else System.out.println("Invalid option. You do not have permission.");
                }
                case 0 -> back = true;
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void viewFoundItems() {
        List<String> items = controller.getFoundItems();
        if (items.isEmpty()) {
            System.out.println("\nNo found items reported at this time.");
            return;
        }

        System.out.println("\nID            | Name       | Description                  | Location        | Status");
        System.out.println("---------------------------------------------------------------------------------------------");
        for (String item : items) {
            String[] p = item.split(",");
            if (p.length == 6) {
                String status = p[4].equals("true") ? "Claimed by " + p[5] : "Available";
                // Using printf for clean column formatting
                System.out.printf("%-13s | %-10s | %-28s | %-15s | %s\n", p[0], p[1], p[2], p[3], status);
            }
        }
    }

    private void viewLostItems() {
        List<String> items = controller.getLostItems();
        if (items.isEmpty()) {
            System.out.println("\nNo lost items reported at this time.");
            return;
        }

        System.out.println("\nID            | Name       | Description                  | Reporter ID     | Date Reported");
        System.out.println("---------------------------------------------------------------------------------------------");
        for (String item : items) {
            String[] p = item.split(",");
            // LostItem format: id, name, description, reporterId, date
            if (p.length == 5) {
                System.out.printf("%-13s | %-10s | %-28s | %-15s | %s\n", p[0], p[1], p[2], p[3], p[4]);
            }
        }
    }

    private void reportLost(String userId) {
        System.out.print("Enter Item Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Description (color, brand, etc.): ");
        String desc = scanner.nextLine();

        controller.reportLostItem(name, desc, userId);
        System.out.println("\n[SUCCESS] Item reported. The Hall Office will notify you if it is found.");
    }

    private void addFound() {
        System.out.print("Enter Item Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Description: ");
        String desc = scanner.nextLine();
        System.out.print("Location Found (e.g., Study Room, Cafeteria): ");
        String loc = scanner.nextLine();

        controller.addFoundItem(name, desc, loc);
        System.out.println("\n[SUCCESS] Item added to the Found database.");
    }

    private void claimItem(String userId) {
        System.out.print("Enter the ID of the Found Item you wish to claim (e.g., FID-123456): ");
        String id = scanner.nextLine().trim();

        if (controller.verifyAndClaim(id, userId)) {
            System.out.println("\n[SUCCESS] Claim processed! Please visit the Hall Office with your Student ID to pick it up.");
        } else {
            System.out.println("\n[ERROR] Invalid ID, or the item has already been claimed.");
        }
    }
}