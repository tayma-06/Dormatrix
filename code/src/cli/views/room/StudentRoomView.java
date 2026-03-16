package cli.views.room;

import models.room.Room;
import utils.ConsoleColors;
import utils.TerminalUI;

import static utils.TerminalUI.*;

public class StudentRoomView {

    private static final MenuItem[] MENU = {
            new MenuItem(1, "View My Room Complaints"),
            new MenuItem(2, "Apply For Room Change"),
            new MenuItem(3, "View My Room Change Applications"),
            new MenuItem(0, "Back")
    };

    public int show(String roomNumber, Room room) {
        try {
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            System.out.print(HIDE_CUR);

            String subtitle = roomNumber.equals("UNASSIGNED") || roomNumber.equals("N/A")
                    ? "No room is currently assigned"
                    : "Your current dorm room overview";

            String[] extra = buildRoomSummary(roomNumber, room);

            drawDashboard(
                    "MY ROOM STATUS",
                    subtitle,
                    MENU,
                    TerminalUI.getActiveTextColor(),
                    TerminalUI.getActiveBoxColor(),
                    extra,
                    3
            );

            return readChoiceArrow();
        } catch (Exception e) {
            cleanup();
            return 0;
        }
    }

    private String[] buildRoomSummary(String roomNumber, Room room) {
        if (roomNumber.equals("UNASSIGNED") || roomNumber.equals("N/A")) {
            return new String[]{
                    "Room Number : (not assigned)",
                    "Status      : " + ConsoleColors.Accent.WARNING + "PENDING ASSIGNMENT" + RESET,
                    "Room Change : " + ConsoleColors.Accent.MUTED + "Unavailable until a room is assigned" + RESET,
                    "Next Step   : Contact the Hall Office for room allocation."
            };
        }

        if (room == null) {
            return new String[]{
                    "Room Number : " + roomNumber,
                    "Status      : " + ConsoleColors.Accent.WARNING + "DETAILS UNAVAILABLE" + RESET,
                    "Room Change : You may still submit a request if needed.",
                    "Info        : Room record could not be loaded right now."
            };
        }

        String statusColor = room.isAvailable()
                ? ConsoleColors.Accent.SUCCESS
                : ConsoleColors.Accent.ERROR;

        return new String[]{
                "Room Number : " + roomNumber,
                "Occupancy   : " + room.getCurrentOccupancy() + "/" + room.getCapacity(),
                "Status      : " + statusColor + (room.isAvailable() ? "AVAILABLE" : "FULL") + RESET,
                "Space Meter : " + buildOccupancyBar(room.getCurrentOccupancy(), room.getCapacity(), 18),
                "Room Change : Submit an application if you want to move rooms.",
                "Note        : Complaints and room-change requests are available below."
        };
    }

    private String buildOccupancyBar(int occupancy, int capacity, int width) {
        int safeCap = Math.max(1, capacity);
        int safeOcc = Math.max(0, Math.min(occupancy, safeCap));
        int fill = (int) Math.round((safeOcc * width) / (double) safeCap);

        String filled = "█".repeat(Math.max(0, fill));
        String empty = "░".repeat(Math.max(0, width - fill));

        String color;
        double ratio = safeOcc / (double) safeCap;
        if (ratio >= 1.0) {
            color = ConsoleColors.Accent.ERROR;
        } else if (ratio >= 0.75) {
            color = ConsoleColors.Accent.WARNING;
        } else {
            color = ConsoleColors.Accent.SUCCESS;
        }

        return color + filled + ConsoleColors.Accent.MUTED + empty + RESET;
    }
}