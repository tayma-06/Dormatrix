package cli.dashboard;

import controllers.facilities.*;
import libraries.slots.SlotAllocator;
import java.util.Scanner;

public class FacilityDashboard {
    private final Scanner scanner = new Scanner(System.in);

    public void showMenu(String username, StudyRoomController study, FridgeController fridge, LaundryController laundry) {
        while (true) {
            System.out.println();
            System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                       FACILITY BOOKING SYSTEM                       ║");
            System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ [1] Book Study Room Seat                                            ║");
            System.out.println("║ [2] Check-in to Study Room                                          ║");
            System.out.println("║ [3] Book Fridge Slot                                                ║");
            System.out.println("║ [4] Schedule Laundry Machine                                        ║");
            System.out.println("║ [0] Logout                                                          ║");
            System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

            System.out.println();
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            if (choice == 0) break;

            handleFacilityChoice(choice, username, study, fridge, laundry);
        }
    }

    private void handleFacilityChoice(int choice, String user, StudyRoomController s, FridgeController f, LaundryController l) {
        switch (choice) {
            case 1: // Book Study Seat
                System.out.print("Enter seat number (1-10): ");
                int seatToBook = scanner.nextInt() - 1; // Convert to 0-index for the array

                if (s.bookSeat(user, seatToBook)) {
                    System.out.println("Seat " + (seatToBook + 1) + " reserved! You have 30 seconds to Check-in.");
                } else {
                    System.out.println("Booking failed. Seat occupied or you already have a booking.");
                }
                break;

            case 2: // Check-in
                System.out.print("Enter your reserved seat number (1-10) to confirm arrival: ");
                int seatToCheckIn = scanner.nextInt() - 1;

                s.checkIn(seatToCheckIn); // This marks checkInStatus[slot][seat] as true
                break;
            case 3:
                f.handleFridgeBooking(user);
                break;
            case 4:
                System.out.print("Enter slot index (1-6): ");
                int slot = scanner.nextInt() - 1;
                System.out.println(l.bookLaundry(slot, user));
                break;
            default:
                System.out.println("Invalid option.");
        }
    }
}