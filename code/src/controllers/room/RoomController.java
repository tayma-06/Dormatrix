package controllers.room;

import cli.dashboard.room.StudentRoomDashboard;
import cli.views.room.StudentRoomView;
import controllers.dashboard.room.StudentRoomDashboardController;
import models.room.Room;
import utils.ConsoleColors;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RoomController {

    private final String ROOM_FILE = "data/rooms/rooms.txt";
    private final String STUDENT_FILE = "data/users/students.txt";
    private final StudentRoomView studentRoomView;
    private List<Room> rooms;

    public RoomController() {
        this.rooms = loadRooms();
        this.studentRoomView = new StudentRoomView();
    }

    public List<Room> getAllRooms() {
        this.rooms = loadRooms();
        return rooms;
    }

    public Room getRoomWithRealOccupancy(String roomId) {
        this.rooms = loadRooms();

        if (roomId == null || roomId.trim().isEmpty()) {
            return null;
        }

        for (Room r : rooms) {
            if (r.getRoomId().equalsIgnoreCase(roomId.trim())) {
                r.setCurrentOccupancy(countStudentsInRoom(r.getRoomId()));
                return r;
            }
        }
        return null;
    }

    public void showStudentRoomDetails(String studentIdentifier) {
        this.rooms = loadRooms();

        String roomNumber = getStudentRoomNumber(studentIdentifier);

        Room roomDetails = null;
        if (!roomNumber.equals("UNASSIGNED") && !roomNumber.equals("N/A")) {
            roomDetails = getRoomWithRealOccupancy(roomNumber);
        }

        boolean stayInMenu = true;
        while (stayInMenu) {
            ConsoleUtil.clearScreen();
            int choice = studentRoomView.show(roomNumber, roomDetails);

            if (choice == 1) {
                if (roomNumber.equals("UNASSIGNED") || roomNumber.equals("N/A")) {
                    TerminalUI.tError("You do not have a room assigned yet.");
                    ConsoleUtil.pause();
                } else {
                    new StudentRoomDashboard(
                            new StudentRoomDashboardController(new RoomService())
                    ).showComplaints(roomNumber);
                }
            } else {
                ConsoleUtil.clearScreen();
                stayInMenu = false;
            }
        }
    }

    public boolean addRoom(String roomId, int capacity) {
        this.rooms = loadRooms();

        for (Room r : rooms) {
            if (r.getRoomId().equalsIgnoreCase(roomId)) {
                TerminalUI.tError("Room " + roomId + " already exists.");
                return false;
            }
        }

        Room newRoom = new Room(roomId, capacity, 0);
        rooms.add(newRoom);
        saveRooms();
        TerminalUI.tSuccess("Room " + roomId + " added.");
        return true;
    }

    public void showAvailableRooms() {
        browseAvailableRooms(false);
    }

    public String pickAvailableRoomInteractive() {
        return browseAvailableRooms(true);
    }

    private String browseAvailableRooms(boolean allowSelect) {
        this.rooms = loadRooms();

        List<Room> available = new ArrayList<>();
        for (Room r : rooms) {
            r.setCurrentOccupancy(countStudentsInRoom(r.getRoomId()));
            if (r.isAvailable()) {
                available.add(r);
            }
        }

        if (available.isEmpty()) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.tBoxTop();
            TerminalUI.tBoxTitle("AVAILABLE ROOMS");
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("No rooms available.");
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("Press Enter to return...");
            TerminalUI.tBoxBottom();
            FastInput.readLine();
            return null;
        }

        int selected = 0;

        while (true) {
            drawAvailableRoomBrowser(available, selected, allowSelect);

            int key;
            try {
                key = readNavKey();
            } catch (Exception e) {
                return null;
            }

            if (key == Key.UP) {
                selected = (selected - 1 + available.size()) % available.size();
            } else if (key == Key.DOWN) {
                selected = (selected + 1) % available.size();
            } else if (key == Key.ENTER) {
                if (allowSelect) {
                    return available.get(selected).getRoomId();
                }
                return null;
            } else if (key == Key.ESC || key == 0) {
                return null;
            }
        }
    }

    private void drawAvailableRoomBrowser(List<Room> rooms, int selected, boolean allowSelect) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        System.out.print(TerminalUI.HIDE_CUR);

        int totalW = Math.min(TerminalUI.termW() - 4, 112);
        int totalCol = TerminalUI.centerCol(totalW);
        int leftW = 42;
        int rightW = totalW - leftW - 3;
        int topRow = 3;

        int visible = Math.max(5, Math.min(10, TerminalUI.termH() - 14));
        int start = Math.max(0, selected - visible / 2);
        if (start + visible > rooms.size()) {
            start = Math.max(0, rooms.size() - visible);
        }
        int end = Math.min(rooms.size(), start + visible);

        List<String> left = new ArrayList<>();
        left.add("Open rooms: " + rooms.size());
        left.add("");

        for (int i = start; i < end; i++) {
            Room room = rooms.get(i);
            String marker = i == selected ? "> " : "  ";
            String line = marker
                    + TerminalUI.padL(room.getRoomId(), 10)
                    + "  "
                    + room.getCurrentOccupancy() + "/" + room.getCapacity();
            left.add(line);
        }

        if (start > 0 || end < rooms.size()) {
            left.add("");
            left.add("Showing " + (start + 1) + "-" + end + " of " + rooms.size());
        }

        Room room = rooms.get(selected);
        int free = Math.max(0, room.getCapacity() - room.getCurrentOccupancy());

        List<String> right = new ArrayList<>();
        right.add("Room ID        : " + room.getRoomId());
        right.add("Status         : AVAILABLE");
        right.add("Occupancy      : " + room.getCurrentOccupancy() + "/" + room.getCapacity());
        right.add("Free Seats     : " + free);
        right.add("");
        right.add("Space Meter");
        right.add(buildOccupancyBar(room.getCurrentOccupancy(), room.getCapacity(), Math.max(12, rightW - 10)));
        right.add("");
        right.add("Summary");
        right.add(room.toString());
        right.add("");
        right.add("Room Insight");

        if (room.getCurrentOccupancy() == 0) {
            right.add("This room is completely empty.");
        } else if (free == 1) {
            right.add("Only one seat remains.");
        } else {
            right.add("This room still has multiple open seats.");
        }

        drawPanel(topRow, totalCol, leftW, "AVAILABLE ROOM LIST", left, selected - start + 2, 2);
        drawPanel(topRow, totalCol + leftW + 3, rightW, "LIVE PREVIEW", right, -1, -1);

        int footerRow = topRow + Math.max(left.size(), right.size()) + 4;
        TerminalUI.at(footerRow, totalCol);

        String hint = allowSelect
                ? "Use Up/Down keys to inspect rooms. Press Enter to select. Press 0 to cancel."
                : "Use Up/Down keys to inspect rooms. Press Enter or 0 to return.";

        System.out.print(ConsoleColors.Accent.MUTED + hint + TerminalUI.RESET);
        System.out.flush();
    }

    private void drawPanel(int row, int col, int width, String title, List<String> lines, int selectedLineIndex, int contentStartLine) {
        String box = TerminalUI.getActiveBoxColor();
        String panel = TerminalUI.getActivePanelBgColor();
        String text = TerminalUI.getActiveTextColor();

        int inner = Math.max(10, width - 2);

        TerminalUI.at(row++, col);
        System.out.print(box + panel + "╔" + "═".repeat(inner) + "╗" + TerminalUI.RESET);

        TerminalUI.at(row++, col);
        System.out.print(
                box + panel + "║"
                        + TerminalUI.BOLD + text + panel + TerminalUI.padC(title, inner)
                        + box + panel + "║" + TerminalUI.RESET
        );

        TerminalUI.at(row++, col);
        System.out.print(box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);

        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i) == null ? "" : lines.get(i);
            boolean isSelected = selectedLineIndex >= 0 && i >= contentStartLine && i == selectedLineIndex;

            String rowBg = isSelected ? ConsoleColors.bgRGB(185, 165, 220) : panel;
            String rowFg = isSelected ? ConsoleColors.fgRGB(25, 15, 55) : text;

            TerminalUI.at(row++, col);
            System.out.print(
                    box + panel + "║ "
                            + rowBg + rowFg + fitLine(raw, inner - 2)
                            + box + panel + " ║" + TerminalUI.RESET
            );
        }

        TerminalUI.at(row, col);
        System.out.print(box + panel + "╚" + "═".repeat(inner) + "╝" + TerminalUI.RESET);
        System.out.flush();
    }

    private String fitLine(String text, int width) {
        if (text == null) {
            text = "";
        }

        if (!text.contains("\u001B")) {
            text = TerminalUI.truncate(text, width);
        }

        int visible = TerminalUI.stripAnsi(text).length();
        if (visible > width) {
            String plain = TerminalUI.stripAnsi(text);
            plain = TerminalUI.truncate(plain, width);
            visible = plain.length();
            return plain + " ".repeat(Math.max(0, width - visible));
        }

        return text + " ".repeat(Math.max(0, width - visible));
    }

    private String buildOccupancyBar(int occupancy, int capacity, int width) {
        int safeCap = Math.max(1, capacity);
        int safeOcc = Math.max(0, Math.min(occupancy, safeCap));
        int fill = (int) Math.round((safeOcc * width) / (double) safeCap);
        int empty = Math.max(0, width - fill);

        String fillBg;
        double ratio = safeOcc / (double) safeCap;

        if (ratio >= 1.0) {
            fillBg = ConsoleColors.bgRGB(200, 70, 70);
        } else if (ratio >= 0.75) {
            fillBg = ConsoleColors.bgRGB(230, 180, 40);
        } else {
            fillBg = ConsoleColors.bgRGB(70, 190, 120);
        }

        String emptyBg = ConsoleColors.bgRGB(55, 30, 75);

        return fillBg + " ".repeat(fill)
                + emptyBg + " ".repeat(empty)
                + TerminalUI.RESET;
    }

    public boolean allocateRoom(String roomId) {
        this.rooms = loadRooms();

        for (Room r : rooms) {
            r.setCurrentOccupancy(countStudentsInRoom(r.getRoomId()));
            if (r.getRoomId().equalsIgnoreCase(roomId)) {
                if (r.isAvailable()) {
                    r.incrementOccupancy();
                    saveRooms();
                    return true;
                } else {
                    TerminalUI.tError("Room " + roomId + " is full.");
                    return false;
                }
            }
        }

        TerminalUI.tError("Room ID not found.");
        return false;
    }

    public void freeRoom(String roomId) {
        this.rooms = loadRooms();

        if (roomId == null || roomId.equals("N/A") || roomId.isEmpty() || roomId.equals("UNASSIGNED")) {
            return;
        }

        for (Room r : rooms) {
            if (r.getRoomId().equalsIgnoreCase(roomId)) {
                r.decrementOccupancy();
                saveRooms();
                return;
            }
        }
    }

    private String getStudentRoomNumber(String target) {
        File file = new File(STUDENT_FILE);
        if (!file.exists()) {
            return "UNASSIGNED";
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length > 1) {
                    String fileId = parts[0].trim().replace("\uFEFF", "");
                    String fileName = parts[1].trim();
                    if (fileId.equals(target.trim()) || fileName.equalsIgnoreCase(target.trim())) {
                        if (parts.length > 7) {
                            String r = parts[7].trim();
                            return r.isEmpty() ? "UNASSIGNED" : r;
                        }
                    }
                }
            }
        } catch (IOException e) {
            TerminalUI.tError("Failed to read student room assignments.");
        }

        return "UNASSIGNED";
    }

    private int countStudentsInRoom(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            return 0;
        }

        File file = new File(STUDENT_FILE);
        if (!file.exists()) {
            return 0;
        }

        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length > 7) {
                    String r = parts[7].trim();
                    if (!r.isEmpty() && r.equalsIgnoreCase(roomId.trim())) {
                        count++;
                    }
                }
            }
        } catch (IOException e) {
            TerminalUI.tError("Failed to count room occupancy.");
        }

        return count;
    }

    private void saveRooms() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ROOM_FILE))) {
            for (Room r : rooms) {
                pw.println(r.toFileString());
            }
        } catch (IOException e) {
            TerminalUI.tError("Failed to save room data.");
        }
    }

    private List<Room> loadRooms() {
        List<Room> list = new ArrayList<>();
        File file = new File(ROOM_FILE);
        if (!file.exists()) {
            return list;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Room r = Room.fromString(line);
                if (r != null) {
                    list.add(r);
                }
            }
        } catch (IOException e) {
            TerminalUI.tError("Failed to load rooms.");
        }

        return list;
    }

    private static final class Key {
        static final int UP = -101;
        static final int DOWN = -102;
        static final int ENTER = -103;
        static final int ESC = -104;
    }

    private int readNavKey() throws Exception {
        if (TerminalUI.getJLineTerminal() != null) {
            org.jline.terminal.Attributes saved = TerminalUI.getJLineTerminal().enterRawMode();
            org.jline.utils.NonBlockingReader reader = TerminalUI.getJLineTerminal().reader();

            try {
                while (true) {
                    int c = reader.read();
                    if (c == -1) {
                        continue;
                    }

                    if (c == 27) {
                        int n1 = reader.read(80);
                        if (n1 == '[' || n1 == 'O') {
                            int n2 = reader.read(80);
                            if (n2 == 'A') return Key.UP;
                            if (n2 == 'B') return Key.DOWN;
                        }
                        return Key.ESC;
                    }

                    if (c == 13 || c == 10) return Key.ENTER;
                    if (c == 3) return 0;
                    if (c >= '0' && c <= '9') return c - '0';
                }
            } finally {
                TerminalUI.getJLineTerminal().setAttributes(saved);
                System.out.print(TerminalUI.SHOW_CUR);
                System.out.flush();
            }
        }

        TerminalUI.setRaw();
        InputStream in = System.in;

        try {
            while (true) {
                int c = in.read();
                if (c == -1) {
                    continue;
                }

                if (c == 27) {
                    if (in.available() == 0) return Key.ESC;
                    int n1 = in.read();
                    if (n1 == '[' || n1 == 'O') {
                        int n2 = in.read();
                        if (n2 == 'A') return Key.UP;
                        if (n2 == 'B') return Key.DOWN;
                    }
                    return Key.ESC;
                }

                if (c == 13 || c == 10) return Key.ENTER;
                if (c >= '0' && c <= '9') return c - '0';
            }
        } finally {
            TerminalUI.setCooked();
            System.out.print(TerminalUI.SHOW_CUR);
            System.out.flush();
        }
    }
}