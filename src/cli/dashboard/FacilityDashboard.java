package cli.dashboard;

import controllers.facilities.*;
import libraries.slots.SlotAllocator;
import utils.*;
import static utils.TerminalUI.*;

public class FacilityDashboard {

    private static final String BOX = ConsoleColors.fgRGB(60, 140, 255);   // electric blue box (matches student)
    private static final String TEXT = ConsoleColors.ThemeText.STUDENT_TEXT;
    private static final String BG = ConsoleColors.bgRGB(0, 6, 45);
    private static final String MUTED = ConsoleColors.Accent.MUTED;

    private static final MenuItem[] MENU = {
        new MenuItem(1, "Book Study Room Seat"),
        new MenuItem(2, "Check-in to Study Room"),
        new MenuItem(3, "Book Fridge Slot"),
        new MenuItem(4, "Book Laundry Machine"),
        new MenuItem(0, "Back"),};

    public void showMenu(String username, StudyRoomController study, FridgeController fridge, LaundryController laundry) {
        while (true) {
            try {
                BackgroundFiller.applyStudentTheme();
                setActiveTheme(BOX, TEXT, BG);
                System.out.print(HIDE_CUR);

                int menuStartRow = 3;
                int promptRow = drawDashboard(
                        "FACILITY BOOKING SYSTEM",
                        "Welcome, " + username,
                        MENU, TEXT, BOX,
                        null,
                        menuStartRow
                );

                System.out.print(SHOW_CUR);
                int choice = FastInput.readInt();
                System.out.print(RESET);

                if (choice == 0) {
                    ConsoleUtil.clearScreen();
                    return;
                }

                handleFacilityChoice(choice, username, study, fridge, laundry);

            } catch (Exception e) {
                cleanup();
                System.err.println("[FacilityDashboard] " + e.getMessage());
            }
        }
    }

    private void handleFacilityChoice(int choice, String user, StudyRoomController s, FridgeController f, LaundryController l) {
        ConsoleUtil.clearScreen();
        switch (choice) {
            case 1:
                int currentSlot = SlotAllocator.getCurrentSlotIndex();

                System.out.println("\n--- Study Room Layout ---");
                for (int i = 0; i < 10; i++) {
                    if (s.getSeatMap()[currentSlot][i] == null) {
                        System.out.print("[ Seat " + (i + 1) + " : EMPTY ]  ");
                    } else {
                        System.out.print("[ Seat " + (i + 1) + " : TAKEN ]  ");
                    }
                    if (i == 4) {
                        System.out.println();
                    }
                }

                System.out.print("\n\nEnter seat number you want to book (1-10): ");
                int seatToBook = FastInput.readInt() - 1;

                if (s.bookSeat(user, seatToBook)) {
                    System.out.println("Booking successful! You have 30 seconds to choose Option 2 and Check-in.");
                }
                ConsoleUtil.pause();
                break;

            case 2:
                System.out.print("Enter your reserved seat number (1-10) to confirm arrival: ");
                int seatToCheckIn = FastInput.readInt() - 1;

                s.checkIn(user, seatToCheckIn);
                ConsoleUtil.pause();
                break;
            case 3:
                f.handleFridgeBooking(user);
                ConsoleUtil.pause();
                break;
            case 4:
                l.displayLaundryStatus();

                System.out.print("\nEnter Laundry slot index (1-6) to book: ");
                int slot = FastInput.readInt() - 1;

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
