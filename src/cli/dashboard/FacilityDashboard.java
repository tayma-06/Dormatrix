package cli.dashboard;

import controllers.facilities.*;
import java.util.Scanner;
import libraries.slots.SlotAllocator;
import utils.ConsoleUtil;

public class FacilityDashboard {

    private final Scanner scanner = new Scanner(System.in);

    public void showMenu(String username, StudyRoomController study, FridgeController fridge, LaundryController laundry) {
        while (true) {
            ConsoleUtil.clearScreen();
            System.out.println();
            System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                       FACILITY BOOKING SYSTEM                       ║");
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ [1] Book Study Room Seat                                            ║");
            System.out.println("║ [2] Check-in to Study Room                                          ║");
            System.out.println("║ [3] Book Fridge Slot                                                ║");
            System.out.println("║ [4] Book Laundry Machine                                            ║");
            System.out.println("║ [0] Logout                                                          ║");
            System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

            System.out.println();
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            if (choice == 0) {
                ConsoleUtil.clearScreen();
                break;
            }

            handleFacilityChoice(choice, username, study, fridge, laundry);
        }
    }

    private void handleFacilityChoice(int choice, String user, StudyRoomController s, FridgeController f, LaundryController l) {
        ConsoleUtil.clearScreen();
        switch (choice) {
            case 1: // 1. Book Study Room Seat
                int currentSlot = SlotAllocator.getCurrentSlotIndex();

                // Display the Room Layout
                System.out.println("\n--- Study Room Layout ---");
                for (int i = 0; i < 10; i++) {
                    if (s.getSeatMap()[currentSlot][i] == null) {
                        System.out.print("[ Seat " + (i + 1) + " : EMPTY ]  ");
                    } else {
                        System.out.print("[ Seat " + (i + 1) + " : TAKEN ]  ");
                    }
                    if (i == 4) {
                        System.out.println(); // Break to next line for 2 rows of 5
                    }
                }

                System.out.print("\n\nEnter seat number you want to book (1-10): ");
                int seatToBook = scanner.nextInt() - 1; // Subtract 1 for 0-based array index

                if (s.bookSeat(user, seatToBook)) {
                    System.out.println("Booking successful! You have 30 seconds to choose Option 2 and Check-in.");
                }
                ConsoleUtil.pause();
                break;

            case 2: // 2. Check-in to Study Room
                System.out.print("Enter your reserved seat number (1-10) to confirm arrival: ");
                int seatToCheckIn = scanner.nextInt() - 1;

                s.checkIn(user, seatToCheckIn);
                ConsoleUtil.pause();
                break;
            case 3:
                f.handleFridgeBooking(user);
                ConsoleUtil.pause();
                break;
            case 4: // Schedule Laundry Machine
                // First, show the current status of all 6 slots
                l.displayLaundryStatus();

                System.out.print("\nEnter Laundry slot index (1-6) to book: ");
                int slot = scanner.nextInt() - 1; // Convert to 0-based index

                // Attempt booking
                String result = l.bookLaundry(slot, user);
                System.out.println(result);
                ConsoleUtil.pause();
                break;
            default:
                System.out.println("Invalid option.");
                ConsoleUtil.pause();
        }
    }
}
