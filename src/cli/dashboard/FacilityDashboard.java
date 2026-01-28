package cli.dashboard;

import controllers.facilities.*;
import libraries.slots.SlotAllocator;
import java.util.Scanner;

public class FacilityDashboard {
    private final Scanner scanner = new Scanner(System.in);

    public void showMenu(String username, StudyRoomController study, FridgeController fridge, LaundryController laundry) {
        while (true) {
            System.out.println("\n-----------------------------------------------------------------------");
            System.out.println("|                       Facility Booking System                          |");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("Current Virtual Time Slot: " + (SlotAllocator.getCurrentSlotIndex() + 1) + "/6");
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("| 1.Book Study Room Seat                                                   |");
            System.out.println("| 2.Check-in to Study Room                                                 |");
            System.out.println("| 3.Book Fridge Slot                                                       |");
            System.out.println("| 4.Schedule Laundry Machine                                               |");
            System.out.println("| 0. Logout                                                                |");
            System.out.println("---------------------------------------------------------------------------");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            if (choice == 0) break;

            handleFacilityChoice(choice, username, study, fridge, laundry);
        }
    }

    private void handleFacilityChoice(int choice, String user, StudyRoomController s, FridgeController f, LaundryController l) {
        switch (choice) {
            case 1:
                if (s.bookSeat(user)) System.out.println("Seat reserved! You have 30 seconds to Check-in (Option 2).");
                else System.out.println("Booking failed. Already booked or slot full.");
                break;
            case 2:
                s.checkIn(user);
                System.out.println("Check-in confirmed for " + user);
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