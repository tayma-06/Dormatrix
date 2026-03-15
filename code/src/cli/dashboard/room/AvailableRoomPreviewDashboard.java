package cli.dashboard.room;

import controllers.room.RoomService;
import models.room.Room;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;
import utils.ConsoleColors;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import java.util.List;

public class AvailableRoomPreviewDashboard {

    private final RoomService roomService;

    public AvailableRoomPreviewDashboard(RoomService roomService) {
        this.roomService = roomService;
    }

    private enum NavKey {
        UP, DOWN, ENTER, ZERO, NONE
    }

    public void show() {
        int selected = 0;

        while (true) {
            List<Room> rooms = roomService.getAvailableRooms();

            if (rooms.isEmpty()) {
                ConsoleUtil.clearScreen();
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                TerminalUI.tError("No available rooms right now.");
                TerminalUI.tPause();
                return;
            }

            if (selected >= rooms.size()) selected = rooms.size() - 1;
            if (selected < 0) selected = 0;

            render(rooms, selected);
            NavKey key = readNavKey();

            if (key == NavKey.UP) {
                selected = (selected - 1 + rooms.size()) % rooms.size();
            } else if (key == NavKey.DOWN) {
                selected = (selected + 1) % rooms.size();
            } else if (key == NavKey.ENTER || key == NavKey.ZERO) {
                TerminalUI.cleanup();
                return;
            }
        }
    }

    private void render(List<Room> rooms, int selected) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        System.out.print(TerminalUI.HIDE_CUR);

        int termW = TerminalUI.termW();
        int leftCol = Math.max(3, termW / 14);
        int topRow = 5;
        int leftW = Math.min(42, Math.max(34, termW / 3));
        int rightCol = leftCol + leftW + 3;
        int rightW = Math.max(42, termW - rightCol - leftCol);

        drawPanel(topRow, leftCol, leftW, 16, "AVAILABLE ROOMS");
        drawPanel(topRow, rightCol, rightW, 16, "ROOM PREVIEW");

        put(topRow + 2, leftCol + 2,
                ConsoleColors.ThemeText.SOFT_WHITE
                        + "Available count: " + rooms.size()
                        + TerminalUI.RESET);

        int start = Math.max(0, selected - 4);
        int end = Math.min(rooms.size(), start + 10);
        if (end - start < 10) {
            start = Math.max(0, end - 10);
        }

        int row = topRow + 4;
        for (int i = start; i < end; i++) {
            Room r = rooms.get(i);
            boolean isSelected = i == selected;

            String line = String.format("%-12s  %s/%s",
                    r.getRoomId(),
                    r.getCurrentOccupancy(),
                    r.getCapacity());

            String fg = isSelected ? ConsoleColors.FG_BLACK : ConsoleColors.ThemeText.SOFT_WHITE;
            String bg = isSelected ? ConsoleColors.bgRGB(210, 195, 245) : TerminalUI.getActivePanelBgColor();

            put(row++, leftCol + 2, bg + fg + pad(line, leftW - 4) + TerminalUI.RESET);
        }

        Room room = rooms.get(selected);
        put(topRow + 2, rightCol + 2, kv("Room ID", room.getRoomId()));
        put(topRow + 3, rightCol + 2, kv("Capacity", String.valueOf(room.getCapacity())));
        put(topRow + 4, rightCol + 2, kv("Occupancy", room.getCurrentOccupancy() + "/" + room.getCapacity()));
        put(topRow + 5, rightCol + 2, kv("Free Seats", String.valueOf(room.getCapacity() - room.getCurrentOccupancy())));
        put(topRow + 6, rightCol + 2, kv("Status", room.isAvailable() ? "AVAILABLE" : "FULL"));

        String meter = buildMeter(room.getCurrentOccupancy(), room.getCapacity(), Math.max(12, rightW - 8));
        put(topRow + 8, rightCol + 2,
                ConsoleColors.ThemeText.SOFT_WHITE + "Occupancy Preview" + TerminalUI.RESET);
        put(topRow + 10, rightCol + 2, meter);

        put(topRow + 14, leftCol,
                ConsoleColors.Accent.MUTED
                        + "Up/Down browse   Enter/0 return"
                        + TerminalUI.RESET);

        System.out.flush();
    }

    private NavKey readNavKey() {
        Terminal terminal = TerminalUI.getJLineTerminal();

        if (terminal != null) {
            Attributes saved = terminal.enterRawMode();
            NonBlockingReader reader = terminal.reader();

            try {
                while (true) {
                    int ch = reader.read();
                    if (ch == -1) continue;

                    if (ch == 27) {
                        int n1 = reader.read(80);
                        if (n1 == '[' || n1 == 'O') {
                            int n2 = reader.read(80);
                            if (n2 == 'A') return NavKey.UP;
                            if (n2 == 'B') return NavKey.DOWN;
                        }
                        continue;
                    }

                    if (ch == 13 || ch == 10) return NavKey.ENTER;
                    if (ch == '0') return NavKey.ZERO;
                }
            } catch (Exception ignored) {
            } finally {
                terminal.setAttributes(saved);
            }
        }

        String line = FastInput.readLine().trim();
        if ("0".equals(line)) return NavKey.ZERO;
        if ("w".equalsIgnoreCase(line) || "k".equalsIgnoreCase(line)) return NavKey.UP;
        if ("s".equalsIgnoreCase(line) || "j".equalsIgnoreCase(line)) return NavKey.DOWN;
        return NavKey.ENTER;
    }

    private String buildMeter(int used, int total, int width) {
        if (total <= 0) {
            total = 1;
        }

        int inner = Math.max(10, width - 2);
        int fill = (int) Math.round((used * 1.0 / total) * inner);
        if (fill < 0) fill = 0;
        if (fill > inner) fill = inner;

        String left = ConsoleColors.bgRGB(110, 220, 160) + " ".repeat(fill);
        String right = ConsoleColors.bgRGB(55, 45, 85) + " ".repeat(inner - fill);

        return "│" + left + right + TerminalUI.RESET + "│";
    }

    private String kv(String key, String value) {
        return ConsoleColors.ThemeText.SOFT_WHITE
                + String.format("%-10s : ", key)
                + ConsoleColors.FG_BRIGHT_WHITE
                + (value == null ? "N/A" : value)
                + TerminalUI.RESET;
    }

    private void drawPanel(int row, int col, int width, int height, String title) {
        String box = TerminalUI.getActiveBoxColor();
        String panel = TerminalUI.getActivePanelBgColor();

        put(row, col, box + panel + "╔" + "═".repeat(width) + "╗" + TerminalUI.RESET);
        put(row + 1, col, box + panel + "║" + padCenter(title, width) + box + panel + "║" + TerminalUI.RESET);
        put(row + 2, col, box + panel + "╠" + "═".repeat(width) + "╣" + TerminalUI.RESET);

        for (int i = 3; i < height - 1; i++) {
            put(row + i, col, box + panel + "║" + " ".repeat(width) + "║" + TerminalUI.RESET);
        }

        put(row + height - 1, col, box + panel + "╚" + "═".repeat(width) + "╝" + TerminalUI.RESET);
    }

    private void put(int row, int col, String text) {
        TerminalUI.at(row, col);
        System.out.print(text);
    }

    private String pad(String s, int w) {
        if (s.length() >= w) return s.substring(0, w);
        return s + " ".repeat(w - s.length());
    }

    private String padCenter(String s, int w) {
        if (s.length() >= w) return s.substring(0, w);
        int left = (w - s.length()) / 2;
        int right = w - s.length() - left;
        return " ".repeat(left) + s + " ".repeat(right);
    }
}