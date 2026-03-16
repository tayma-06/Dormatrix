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
        UP, DOWN, ENTER, ZERO, ESC, NONE
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
            } else if (key == NavKey.ENTER || key == NavKey.ZERO || key == NavKey.ESC) {
                TerminalUI.cleanup();
                return;
            }
        }
    }

    private void render(List<Room> rooms, int selected) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        System.out.print(TerminalUI.HIDE_CUR);

        int screenW = Math.max(100, TerminalUI.termW() - 4);
        int leftW = Math.min(42, Math.max(36, screenW / 2 - 2));
        int rightW = Math.max(50, screenW - leftW - 3);

        int totalW = leftW + rightW + 3;
        int leftCol = TerminalUI.centerCol(totalW);
        int rightCol = leftCol + leftW + 3;
        int topRow = 3;

        drawLeftPane(topRow, leftCol, leftW, rooms, selected);
        drawRightPane(topRow, rightCol, rightW, rooms.get(selected));

        System.out.flush();
    }

    private void drawLeftPane(int topRow, int col, int width, List<Room> rooms, int selected) {
        int inner = width - 2;
        String box = TerminalUI.getActiveBoxColor();
        String panel = TerminalUI.getActivePanelBgColor();

        int row = topRow;
        int resultRows = 10;

        printRow(row++, col, box + panel + "╔" + "═".repeat(inner) + "╗" + TerminalUI.RESET);
        printRow(row++, col,
                box + panel + "║"
                        + TerminalUI.BOLD + TerminalUI.ACCENT + panel
                        + TerminalUI.padC("AVAILABLE ROOMS", inner)
                        + box + panel + "║" + TerminalUI.RESET);
        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);

        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE + "Available count: " + rooms.size(),
                box, panel);

        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);

        int start = Math.max(0, selected - resultRows / 2);
        if (start + resultRows > rooms.size()) {
            start = Math.max(0, rooms.size() - resultRows);
        }
        int end = Math.min(rooms.size(), start + resultRows);

        for (int i = start; i < end; i++) {
            Room room = rooms.get(i);
            boolean isSelected = i == selected;

            String line = room.getRoomId()
                    + "  |  "
                    + room.getCurrentOccupancy() + "/" + room.getCapacity();

            printSuggestionRow(row++, col, inner, line, isSelected, box, panel);
        }

        while (row < topRow + 17) {
            printSuggestionRow(row++, col, inner, "", false, box, panel);
        }

        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);
        printTextRow(row++, col, inner,
                ConsoleColors.Accent.MUTED
                        + "Use Up/Down to browse. Enter or 0 returns.",
                box, panel);
        printRow(row, col, box + panel + "╚" + "═".repeat(inner) + "╝" + TerminalUI.RESET);
    }

    private void drawRightPane(int topRow, int col, int width, Room room) {
        int inner = width - 2;
        String box = TerminalUI.getActiveBoxColor();
        String panel = TerminalUI.getActivePanelBgColor();

        int row = topRow;
        List<String> students = roomService.getStudentsAllocatedToRoom(room.getRoomId());
        int freeSeats = Math.max(0, room.getCapacity() - room.getCurrentOccupancy());

        printRow(row++, col, box + panel + "╔" + "═".repeat(inner) + "╗" + TerminalUI.RESET);
        printRow(row++, col,
                box + panel + "║"
                        + TerminalUI.BOLD + TerminalUI.ACCENT + panel
                        + TerminalUI.padC("ROOM PREVIEW", inner)
                        + box + panel + "║" + TerminalUI.RESET);
        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);

        printTextRow(row++, col, inner, kv("Room ID", room.getRoomId()), box, panel);
        printTextRow(row++, col, inner, kv("Capacity", String.valueOf(room.getCapacity())), box, panel);
        printTextRow(row++, col, inner, kv("Occupancy", room.getCurrentOccupancy() + "/" + room.getCapacity()), box, panel);
        printTextRow(row++, col, inner, kv("Free Seats", String.valueOf(freeSeats)), box, panel);
        printTextRow(row++, col, inner, kv("Status", colorRoomStatus(room)), box, panel);

        printTextRow(row++, col, inner, "", box, panel);
        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE + "LIVE PREVIEW",
                box, panel);
        printTextRow(row++, col, inner,
                buildMeterLine(room.getCurrentOccupancy(), room.getCapacity(), Math.max(16, inner - 18)),
                box, panel);

        printTextRow(row++, col, inner, "", box, panel);
        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE + "ALLOCATED STUDENTS",
                box, panel);

        if (students.isEmpty()) {
            printTextRow(row++, col, inner,
                    ConsoleColors.Accent.MUTED + "(no student allocated yet)",
                    box, panel);
            printTextRow(row++, col, inner, "", box, panel);
            printTextRow(row++, col, inner, "", box, panel);
        } else {
            int visible = 4;
            for (int i = 0; i < visible; i++) {
                String line = i < students.size() ? students.get(i) : "";
                String color = i < students.size()
                        ? ConsoleColors.FG_BRIGHT_WHITE
                        : "";
                printTextRow(row++, col, inner, color + line, box, panel);
            }
        }

        printTextRow(row++, col, inner, "", box, panel);
        printTextRow(row++, col, inner,
                ConsoleColors.Accent.MUTED + buildSeatSummary(room),
                box, panel);

        while (row < topRow + 17) {
            printTextRow(row++, col, inner, "", box, panel);
        }

        printRow(row, col, box + panel + "╚" + "═".repeat(inner) + "╝" + TerminalUI.RESET);
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
                        return NavKey.ESC;
                    }

                    if (ch == 13 || ch == 10) return NavKey.ENTER;
                    if (ch == '0') return NavKey.ZERO;
                }
            } catch (Exception ignored) {
                return NavKey.NONE;
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

    private String kv(String key, String value) {
        return ConsoleColors.ThemeText.SOFT_WHITE
                + String.format("%-10s : ", key)
                + ConsoleColors.FG_BRIGHT_WHITE
                + (value == null ? "N/A" : value)
                + TerminalUI.RESET;
    }

    private String colorRoomStatus(Room room) {
        if (room == null) return "N/A";

        int capacity = Math.max(1, room.getCapacity());
        int occ = Math.max(0, room.getCurrentOccupancy());
        double ratio = occ / (double) capacity;

        if (ratio >= 1.0) {
            return ConsoleColors.Accent.ERROR + "FULL" + TerminalUI.RESET;
        } else if (ratio >= 0.75) {
            return ConsoleColors.Accent.WARNING + "LIMITED" + TerminalUI.RESET;
        }
        return ConsoleColors.Accent.SUCCESS + "AVAILABLE" + TerminalUI.RESET;
    }

    private String buildSeatSummary(Room room) {
        int free = Math.max(0, room.getCapacity() - room.getCurrentOccupancy());
        if (free == 0) return "No free seat remains in this room.";
        if (free == 1) return "Only 1 seat is available in this room.";
        return free + " seats are currently available.";
    }

    private String buildMeterLine(int used, int total, int barWidth) {
        if (total <= 0) total = 1;

        int safeUsed = Math.max(0, Math.min(used, total));
        int fill = (int) Math.round((safeUsed * 1.0 / total) * barWidth);
        fill = Math.max(0, Math.min(fill, barWidth));

        String color;
        double ratio = safeUsed / (double) total;
        if (ratio >= 1.0) {
            color = ConsoleColors.Accent.ERROR;
        } else if (ratio >= 0.75) {
            color = ConsoleColors.Accent.WARNING;
        } else {
            color = ConsoleColors.Accent.SUCCESS;
        }

        String filled = "=".repeat(fill);
        String empty = "-".repeat(barWidth - fill);

        return ConsoleColors.ThemeText.SOFT_WHITE + "Seats : "
                + color + "[" + filled + ConsoleColors.Accent.MUTED + empty + color + "]"
                + TerminalUI.RESET + " "
                + ConsoleColors.FG_BRIGHT_WHITE + safeUsed + "/" + total
                + TerminalUI.RESET;
    }

    private void printSuggestionRow(int row, int col, int inner, String text, boolean selected, String box, String panel) {
        String display = fitVisible(text, inner - 2);

        if (selected) {
            String bg = ConsoleColors.bgRGB(210, 195, 245);
            String fg = ConsoleColors.fgRGB(35, 20, 70);

            printRow(row, col,
                    box + panel + "║ "
                            + bg + fg + display
                            + bg + fg + spaces(inner - 2 - TerminalUI.stripAnsi(display).length())
                            + box + panel + " ║" + TerminalUI.RESET);
        } else {
            printRow(row, col,
                    box + panel + "║ "
                            + panel + TerminalUI.getActiveTextColor() + display
                            + panel + TerminalUI.getActiveTextColor() + spaces(inner - 2 - TerminalUI.stripAnsi(display).length())
                            + box + panel + " ║" + TerminalUI.RESET);
        }
    }

    private void printTextRow(int row, int col, int inner, String text, String box, String panel) {
        String display = fitVisible(text, inner - 2);

        printRow(row, col,
                box + panel + "║ "
                        + panel + TerminalUI.getActiveTextColor() + display
                        + panel + TerminalUI.getActiveTextColor() + spaces(inner - 2 - TerminalUI.stripAnsi(display).length())
                        + box + panel + " ║" + TerminalUI.RESET);
    }

    private void printRow(int row, int col, String text) {
        TerminalUI.at(row, col);
        System.out.print(text);
    }

    private String fitVisible(String s, int max) {
        if (s == null) return "";
        String plain = TerminalUI.stripAnsi(s);
        if (plain.length() <= max) return s;
        if (max <= 1) return plain.substring(0, max);
        return plain.substring(0, max - 1) + "…";
    }

    private String spaces(int n) {
        return " ".repeat(Math.max(0, n));
    }
}