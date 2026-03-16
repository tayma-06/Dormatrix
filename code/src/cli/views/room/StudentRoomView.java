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
            fillBackground(TerminalUI.getActiveBgColor());
            System.out.print(HIDE_CUR);

            boolean noRoom = roomNumber == null
                    || roomNumber.equals("UNASSIGNED")
                    || roomNumber.equals("N/A");

            String subtitle = noRoom
                    ? "No room is currently assigned to your profile"
                    : "Review your room details and choose a room-service action";

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
        boolean noRoom = roomNumber == null
                || roomNumber.equals("UNASSIGNED")
                || roomNumber.equals("N/A");

        if (noRoom) {
            return new String[]{
                    "Room Number : (not assigned)",
                    "Status      : " + ConsoleColors.Accent.WARNING + "PENDING ASSIGNMENT" + RESET,
                    "Room Change : Unavailable until a room is assigned",
                    "Complaints  : Available after room allocation",
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

        int capacity = Math.max(1, room.getCapacity());
        int occupancy = Math.max(0, room.getCurrentOccupancy());
        int freeSeats = Math.max(0, capacity - occupancy);
        double ratio = occupancy / (double) capacity;

        String statusColor;
        String statusText;
        if (ratio >= 1.0) {
            statusColor = ConsoleColors.Accent.ERROR;
            statusText = "FULL";
        } else if (ratio >= 0.75) {
            statusColor = ConsoleColors.Accent.WARNING;
            statusText = "NEAR CAPACITY";
        } else {
            statusColor = ConsoleColors.Accent.SUCCESS;
            statusText = "AVAILABLE";
        }

        String comfort;
        if (occupancy == 0) {
            comfort = "Very quiet room";
        } else if (ratio >= 1.0) {
            comfort = "No free space remaining";
        } else if (ratio >= 0.75) {
            comfort = "Crowded right now";
        } else {
            comfort = "Still has breathing room";
        }

        return new String[]{
                "Room Number : " + roomNumber,
                "Occupancy   : " + occupancy + "/" + capacity,
                "Free Seats  : " + freeSeats,
                "Status      : " + statusColor + statusText + RESET,
                "Space Meter : " + buildOccupancyBar(occupancy, capacity, 18),
                "Room Feel   : " + comfort,
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