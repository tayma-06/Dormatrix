package cli.views.room;

import models.room.Room;
import utils.FastInput;
import utils.TerminalUI;

public class StudentRoomView {

    public int show(String roomNumber, Room room) {
        TerminalUI.tEmpty();
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("MY ROOM STATUS");
        TerminalUI.tBoxSep();
        if (roomNumber.equals("UNASSIGNED") || roomNumber.equals("N/A")) {
            TerminalUI.tBoxLine("Status: NOT ASSIGNED");
            TerminalUI.tBoxLine("Action: Please contact Hall Office.");
        } else {
            TerminalUI.tBoxLine("Room No: " + roomNumber);

            if (room != null) {
                String status = room.isAvailable() ? "AVAILABLE" : "FULL";
                TerminalUI.tBoxLine("Occupancy: " + room.getCurrentOccupancy() + "/" + room.getCapacity());
                TerminalUI.tBoxLine("Status: " + status);
            } else {
                TerminalUI.tBoxLine("Info: Room details not found.");
            }
        }
        TerminalUI.tSubDashboard("MY ROOM STATUS", new String[]{
            "[1] My Room Complaints",
            "[0] Exit"
        });

        while (true) {
            String input = FastInput.readLine().trim();
            if (input.equals("0")) {
                return 0;
            } else if (input.equals("1")) {
                return 1;
            } else {
                TerminalUI.tPrompt("Invalid choice. Enter 0 or 1: ");
            }
        }
    }
}
