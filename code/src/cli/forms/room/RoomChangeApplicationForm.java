package cli.forms.room;

import controllers.dashboard.room.StudentRoomDashboardController;
import models.room.Room;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;
import utils.ConsoleColors;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static utils.TerminalUI.*;

public class RoomChangeApplicationForm {

    private final StudentRoomDashboardController controller;

    public RoomChangeApplicationForm(StudentRoomDashboardController controller) {
        this.controller = controller;
    }

    private enum NavKey {
        UP, DOWN, ENTER, ZERO, ESC, EDIT_REASON, SUBMIT, CLEAR, NONE
    }

    public void show(String studentIdentifier, String currentRoom) {
        if (currentRoom == null || currentRoom.equals("UNASSIGNED") || currentRoom.equals("N/A")) {
            ConsoleUtil.clearScreen();
            fillBackground(getActiveBgColor());
            TerminalUI.tError("You do not have an assigned room yet.");
            TerminalUI.tPause();
            return;
        }

        int selected = 0;
        String requestedRoom = "";
        String reason = "";

        while (true) {
            List<Room> rooms = getCandidateRooms(currentRoom);

            if (rooms.isEmpty()) {
                ConsoleUtil.clearScreen();
                fillBackground(getActiveBgColor());
                TerminalUI.tError("No other rooms are currently available for room change.");
                TerminalUI.tPause();
                return;
            }

            if (!containsRoom(rooms, requestedRoom)) {
                requestedRoom = "";
            }

            if (selected >= rooms.size()) selected = rooms.size() - 1;
            if (selected < 0) selected = 0;

            drawLiveApplicationScreen(rooms, selected, currentRoom, requestedRoom, reason);
            NavKey key = readNavKey();

            if (key == NavKey.UP) {
                selected = (selected - 1 + rooms.size()) % rooms.size();
            } else if (key == NavKey.DOWN) {
                selected = (selected + 1) % rooms.size();
            } else if (key == NavKey.ENTER) {
                requestedRoom = rooms.get(selected).getRoomId();
            } else if (key == NavKey.EDIT_REASON) {
                reason = promptReason(reason);
            } else if (key == NavKey.CLEAR) {
                requestedRoom = "";
            } else if (key == NavKey.SUBMIT) {
                if (requestedRoom == null || requestedRoom.trim().isEmpty()) {
                    requestedRoom = rooms.get(selected).getRoomId();
                }

                String validation = validate(currentRoom, requestedRoom, reason);
                if (validation != null) {
                    showResult(validation);
                    continue;
                }

                Room targetRoom = findRoom(rooms, requestedRoom);
                int decision = showPreview(currentRoom, requestedRoom, reason, targetRoom);

                if (decision == 1) {
                    String result = controller.submitRoomChangeApplication(studentIdentifier, requestedRoom, reason);
                    showResult(result);
                    return;
                } else if (decision == 2) {
                    reason = promptReason(reason);
                }
            } else if (key == NavKey.ZERO || key == NavKey.ESC) {
                cleanup();
                return;
            }
        }
    }

    private List<Room> getCandidateRooms(String currentRoom) {
        List<Room> source = controller.getAvailableRooms();
        List<Room> filtered = new ArrayList<>();

        for (Room room : source) {
            if (room == null || room.getRoomId() == null) continue;
            if (!room.getRoomId().equalsIgnoreCase(currentRoom)) {
                filtered.add(room);
            }
        }
        return filtered;
    }

    private void drawLiveApplicationScreen(List<Room> rooms, int selected, String currentRoom, String requestedRoom, String reason) {
        ConsoleUtil.clearScreen();
        fillBackground(getActiveBgColor());
        System.out.print(HIDE_CUR);

        int totalW = Math.min(termW() - 4, 122);
        int leftW = 40;
        int rightW = totalW - leftW - 3;
        int totalCol = centerCol(totalW);
        int leftCol = totalCol;
        int rightCol = totalCol + leftW + 3;
        int topRow = 3;

        drawLeftPane(topRow, leftCol, leftW, rooms, selected, requestedRoom);
        drawRightPane(topRow, rightCol, rightW, rooms.get(selected), currentRoom, requestedRoom, reason);

        System.out.flush();
    }

    private void drawLeftPane(int topRow, int col, int width, List<Room> rooms, int selected, String requestedRoom) {
        int inner = width - 2;
        String box = getActiveBoxColor();
        String panel = getActivePanelBgColor();

        int resultRows = 12;
        int row = topRow;

        printRow(row++, col, box + panel + "╔" + "═".repeat(inner) + "╗" + RESET);
        printRow(row++, col,
                box + panel + "║"
                        + BOLD + ACCENT + panel
                        + padC("AVAILABLE ROOMS", inner)
                        + box + panel + "║" + RESET);
        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + RESET);

        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE + "Available now: " + rooms.size(),
                box, panel);

        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + RESET);

        int start = Math.max(0, selected - resultRows / 2);
        if (start + resultRows > rooms.size()) {
            start = Math.max(0, rooms.size() - resultRows);
        }
        int end = Math.min(rooms.size(), start + resultRows);

        for (int i = start; i < end; i++) {
            Room room = rooms.get(i);

            boolean isSelected = i == selected;
            boolean isPicked = room.getRoomId().equalsIgnoreCase(requestedRoom);

            int free = Math.max(0, room.getCapacity() - room.getCurrentOccupancy());

            String marker = isSelected ? ">" : " ";
            String picked = isPicked ? "*" : " ";
            String line = marker + picked + " "
                    + padPlain(room.getRoomId(), 8)
                    + "  "
                    + free + " free";

            printSuggestionRow(row++, col, inner, line, isSelected, box, panel);
        }

        while (row < topRow + 17) {
            printSuggestionRow(row++, col, inner, "", false, box, panel);
        }

        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + RESET);
        printTextRow(row++, col, inner,
                ConsoleColors.Accent.MUTED + "Showing " + (start + 1) + "-" + end + " of " + rooms.size(),
                box, panel);
        printRow(row, col, box + panel + "╚" + "═".repeat(inner) + "╝" + RESET);
    }

    private void drawRightPane(int topRow, int col, int width, Room highlighted, String currentRoom, String requestedRoom, String reason) {
        int inner = width - 2;
        String box = getActiveBoxColor();
        String panel = getActivePanelBgColor();

        int row = topRow;

        printRow(row++, col, box + panel + "╔" + "═".repeat(inner) + "╗" + RESET);
        printRow(row++, col,
                box + panel + "║"
                        + BOLD + ACCENT + panel
                        + padC("ROOM CHANGE APPLICATION", inner)
                        + box + panel + "║" + RESET);
        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + RESET);

        printTextRow(row++, col, inner, kv("Current Room", currentRoom), box, panel);
        printTextRow(row++, col, inner, kv("Highlighted", highlighted.getRoomId()), box, panel);
        printTextRow(row++, col, inner, kv("Requested", valueOrHint(requestedRoom, "Press Enter to pick")), box, panel);
        printTextRow(row++, col, inner, kv("Capacity", String.valueOf(highlighted.getCapacity())), box, panel);
        printTextRow(row++, col, inner, kv("Occupancy",
                highlighted.getCurrentOccupancy() + "/" + highlighted.getCapacity()), box, panel);
        printTextRow(row++, col, inner, kv("Free Seats",
                String.valueOf(Math.max(0, highlighted.getCapacity() - highlighted.getCurrentOccupancy()))), box, panel);
        printTextRow(row++, col, inner, kv("Status", colorRoomStatus(highlighted)), box, panel);

        printTextRow(row++, col, inner, "", box, panel);
        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE + "LIVE PREVIEW",
                box, panel);

        printTextRow(row++, col, inner,
                buildMeterLine(highlighted.getCurrentOccupancy(), highlighted.getCapacity(), Math.max(16, inner - 18)),
                box, panel);

        printTextRow(row++, col, inner,
                buildSeatSummary(highlighted),
                box, panel);

        printTextRow(row++, col, inner, "", box, panel);
        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE + "REASON",
                box, panel);

        String[] wrappedReason = wrapLines(
                reason == null || reason.trim().isEmpty() ? "(Press R to write a reason)" : reason,
                Math.max(18, inner - 2)
        );

        for (int i = 0; i < 3; i++) {
            String line = i < wrappedReason.length ? wrappedReason[i] : "";
            String color = (reason == null || reason.trim().isEmpty())
                    ? ConsoleColors.Accent.MUTED
                    : ConsoleColors.FG_BRIGHT_WHITE;
            printTextRow(row++, col, inner, color + line, box, panel);
        }

        printTextRow(row++, col, inner, "", box, panel);
        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE + "CONTROLS",
                box, panel);

        printTextRow(row++, col, inner,
                ConsoleColors.Accent.SUCCESS + "Up/Down Browse   Enter Pick Room   R Edit Reason",
                box, panel);

        printTextRow(row++, col, inner,
                ConsoleColors.Accent.WARNING + "S Submit Preview   C Clear Pick   0 Cancel",
                box, panel);

        printRow(row, col, box + panel + "╚" + "═".repeat(inner) + "╝" + RESET);
    }

    private int showPreview(String currentRoom, String requestedRoom, String reason, Room targetRoom) {
        while (true) {
            ConsoleUtil.clearScreen();
            fillBackground(getActiveBgColor());
            System.out.print(HIDE_CUR);

            int totalW = Math.min(termW() - 4, 112);
            int leftW = 38;
            int rightW = totalW - leftW - 3;
            int totalCol = centerCol(totalW);
            int topRow = 5;

            drawPreviewLeft(topRow, totalCol, leftW);
            drawPreviewRight(topRow, totalCol + leftW + 3, rightW, currentRoom, requestedRoom, reason, targetRoom);

            System.out.flush();

            NavKey key = readNavKey();
            if (key == NavKey.ENTER || key == NavKey.SUBMIT) {
                return 1;
            }
            if (key == NavKey.EDIT_REASON) {
                return 2;
            }
            if (key == NavKey.ZERO || key == NavKey.ESC) {
                return 0;
            }
        }
    }

    private void drawPreviewLeft(int topRow, int col, int width) {
        int inner = width - 2;
        String box = getActiveBoxColor();
        String panel = getActivePanelBgColor();
        int row = topRow;

        printRow(row++, col, box + panel + "╔" + "═".repeat(inner) + "╗" + RESET);
        printRow(row++, col,
                box + panel + "║"
                        + BOLD + ACCENT + panel
                        + padC("SUBMIT REQUEST", inner)
                        + box + panel + "║" + RESET);
        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + RESET);

        printTextRow(row++, col, inner, ConsoleColors.ThemeText.SOFT_WHITE + "Ready to send this application?", box, panel);
        printTextRow(row++, col, inner, "", box, panel);
        printTextRow(row++, col, inner, ConsoleColors.Accent.SUCCESS + "[Enter / S] Submit", box, panel);
        printTextRow(row++, col, inner, ConsoleColors.Accent.WARNING + "[R / E] Edit Reason", box, panel);
        printTextRow(row++, col, inner, ConsoleColors.Accent.MUTED + "[0 / Esc] Back", box, panel);

        while (row < topRow + 12) {
            printTextRow(row++, col, inner, "", box, panel);
        }

        printRow(row, col, box + panel + "╚" + "═".repeat(inner) + "╝" + RESET);
    }

    private void drawPreviewRight(int topRow, int col, int width, String currentRoom, String requestedRoom, String reason, Room targetRoom) {
        int inner = width - 2;
        String box = getActiveBoxColor();
        String panel = getActivePanelBgColor();
        int row = topRow;

        printRow(row++, col, box + panel + "╔" + "═".repeat(inner) + "╗" + RESET);
        printRow(row++, col,
                box + panel + "║"
                        + BOLD + ACCENT + panel
                        + padC("REQUEST PREVIEW", inner)
                        + box + panel + "║" + RESET);
        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + RESET);

        printTextRow(row++, col, inner, kv("Current Room", currentRoom), box, panel);
        printTextRow(row++, col, inner, kv("Requested Room", requestedRoom), box, panel);

        if (targetRoom != null) {
            printTextRow(row++, col, inner, kv("Capacity", String.valueOf(targetRoom.getCapacity())), box, panel);
            printTextRow(row++, col, inner, kv("Occupancy",
                    targetRoom.getCurrentOccupancy() + "/" + targetRoom.getCapacity()), box, panel);
            printTextRow(row++, col, inner, kv("Free Seats",
                    String.valueOf(Math.max(0, targetRoom.getCapacity() - targetRoom.getCurrentOccupancy()))), box, panel);
            printTextRow(row++, col, inner, kv("Status", colorRoomStatus(targetRoom)), box, panel);
            printTextRow(row++, col, inner, "", box, panel);
            printTextRow(row++, col, inner, buildMeterLine(targetRoom.getCurrentOccupancy(), targetRoom.getCapacity(), Math.max(16, inner - 18)), box, panel);
        } else {
            printTextRow(row++, col, inner, "", box, panel);
        }

        printTextRow(row++, col, inner, "", box, panel);
        printTextRow(row++, col, inner, ConsoleColors.ThemeText.SOFT_WHITE + "REASON", box, panel);

        String[] wrapped = wrapLines(reason, Math.max(18, inner - 2));
        for (int i = 0; i < 3; i++) {
            String line = i < wrapped.length ? wrapped[i] : "";
            printTextRow(row++, col, inner, ConsoleColors.FG_BRIGHT_WHITE + line, box, panel);
        }

        while (row < topRow + 12) {
            printTextRow(row++, col, inner, "", box, panel);
        }

        printRow(row, col, box + panel + "╚" + "═".repeat(inner) + "╝" + RESET);
    }

    private String validate(String currentRoom, String requestedRoom, String reason) {
        if (requestedRoom == null || requestedRoom.trim().isEmpty()) {
            return "Please pick a requested room first.";
        }

        if (requestedRoom.equalsIgnoreCase(currentRoom)) {
            return "You are already assigned to that room.";
        }

        if (reason == null || reason.trim().isEmpty()) {
            return "Please write a reason before submitting.";
        }

        return null;
    }

    private String promptReason(String currentReason) {
        ConsoleUtil.clearScreen();
        fillBackground(getActiveBgColor());
        System.out.print(HIDE_CUR);

        int col = boxCol();
        int iw = innerW();
        int row = Math.max(5, centerRow(10));

        String box = getActiveBoxColor();
        String panel = getActivePanelBgColor();
        String text = getActiveTextColor();
        String inputBg = getActiveInputBgColor();

        at(row++, col);
        System.out.print(box + panel + "╔" + "═".repeat(iw) + "╗" + RESET);

        at(row++, col);
        System.out.print(
                box + panel + "║"
                        + BOLD + ACCENT + panel
                        + padC("EDIT ROOM CHANGE REASON", iw)
                        + box + panel + "║" + RESET
        );

        at(row++, col);
        System.out.print(box + panel + "╠" + "═".repeat(iw) + "╣" + RESET);

        at(row++, col);
        System.out.print(
                box + panel + "║ "
                        + ConsoleColors.Accent.MUTED + panel
                        + padL("Write a short reason. Leave blank to keep the current one. Enter 0 to cancel.", iw - 2)
                        + box + panel + " ║" + RESET
        );

        String current = (currentReason == null || currentReason.trim().isEmpty())
                ? "(none)"
                : TerminalUI.truncate(currentReason, iw - 12);

        at(row++, col);
        System.out.print(
                box + panel + "║ "
                        + ConsoleColors.ThemeText.SOFT_WHITE + panel
                        + padL("Current : " + current, iw - 2)
                        + box + panel + " ║" + RESET
        );

        at(row++, col);
        System.out.print(box + panel + "╠" + "═".repeat(iw) + "╣" + RESET);

        String label = "Reason  : ";
        int fieldW = Math.max(10, iw - label.length() - 2);

        at(row, col);
        System.out.print(
                box + panel + "║ "
                        + ConsoleColors.Accent.INPUT + panel + label
                        + inputBg + text + " ".repeat(fieldW)
                        + box + panel + " ║" + RESET
        );

        int inputRow = row;
        int inputCol = col + 2 + label.length();
        row++;

        at(row, col);
        System.out.print(box + panel + "╚" + "═".repeat(iw) + "╝" + RESET);

        setCooked();
        System.out.print(SHOW_CUR);
        at(inputRow, inputCol);
        System.out.print(inputBg + text);
        System.out.flush();

        String line = FastInput.readLine().trim();

        if ("0".equals(line)) {
            return currentReason == null ? "" : currentReason;
        }

        if (line.isEmpty()) {
            return currentReason == null ? "" : currentReason;
        }

        return line;
    }

    private void showResult(String result) {
        ConsoleUtil.clearScreen();
        fillBackground(getActiveBgColor());

        String safe = result == null ? "Operation finished." : result;
        String lower = safe.toLowerCase();

        if (lower.contains("successfully") || lower.contains("submitted")) {
            TerminalUI.tSuccess(safe);
        } else {
            TerminalUI.tError(safe);
        }
        TerminalUI.tPause();
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
                    if (ch == 'r' || ch == 'R' || ch == 'e' || ch == 'E') return NavKey.EDIT_REASON;
                    if (ch == 's' || ch == 'S' || ch == 'p' || ch == 'P') return NavKey.SUBMIT;
                    if (ch == 'c' || ch == 'C') return NavKey.CLEAR;
                }
            } catch (Exception ignored) {
                return NavKey.NONE;
            } finally {
                terminal.setAttributes(saved);
                System.out.print(SHOW_CUR);
                System.out.flush();
            }
        }

        setRaw();
        InputStream in = System.in;

        try {
            while (true) {
                int c = in.read();
                if (c == -1) continue;

                if (c == 27) {
                    if (in.available() == 0) return NavKey.ESC;
                    int n1 = in.read();
                    if (n1 == '[' || n1 == 'O') {
                        int n2 = in.read();
                        if (n2 == 'A') return NavKey.UP;
                        if (n2 == 'B') return NavKey.DOWN;
                    }
                    return NavKey.ESC;
                }

                if (c == 13 || c == 10) return NavKey.ENTER;
                if (c == '0') return NavKey.ZERO;
                if (c == 'r' || c == 'R' || c == 'e' || c == 'E') return NavKey.EDIT_REASON;
                if (c == 's' || c == 'S' || c == 'p' || c == 'P') return NavKey.SUBMIT;
                if (c == 'c' || c == 'C') return NavKey.CLEAR;
            }
        } catch (Exception ignored) {
            return NavKey.NONE;
        } finally {
            setCooked();
            System.out.print(SHOW_CUR);
            System.out.flush();
        }
    }

    private boolean containsRoom(List<Room> rooms, String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) return false;
        for (Room room : rooms) {
            if (room.getRoomId().equalsIgnoreCase(roomId.trim())) {
                return true;
            }
        }
        return false;
    }

    private Room findRoom(List<Room> rooms, String roomId) {
        if (roomId == null) return null;
        for (Room room : rooms) {
            if (room.getRoomId().equalsIgnoreCase(roomId.trim())) {
                return room;
            }
        }
        return null;
    }

    private String kv(String key, String value) {
        return ConsoleColors.ThemeText.SOFT_WHITE
                + String.format("%-12s : ", key)
                + ConsoleColors.FG_BRIGHT_WHITE
                + (value == null ? "N/A" : value)
                + RESET;
    }

    private String valueOrHint(String value, String hint) {
        if (value == null || value.trim().isEmpty()) {
            return ConsoleColors.Accent.MUTED + hint + RESET;
        }
        return value;
    }

    private String colorRoomStatus(Room room) {
        if (room == null) return "N/A";
        int capacity = Math.max(1, room.getCapacity());
        int occ = Math.max(0, room.getCurrentOccupancy());
        double ratio = occ / (double) capacity;

        if (ratio >= 1.0) {
            return ConsoleColors.Accent.ERROR + "FULL" + RESET;
        } else if (ratio >= 0.75) {
            return ConsoleColors.Accent.WARNING + "LIMITED" + RESET;
        }
        return ConsoleColors.Accent.SUCCESS + "AVAILABLE" + RESET;
    }

    private String buildSeatSummary(Room room) {
        int free = Math.max(0, room.getCapacity() - room.getCurrentOccupancy());
        String msg;
        if (free == 0) {
            msg = "No free seat remains in this room.";
        } else if (free == 1) {
            msg = "Only 1 seat is available in this room.";
        } else {
            msg = free + " seats are currently available.";
        }
        return ConsoleColors.Accent.MUTED + msg + RESET;
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
                + RESET + " "
                + ConsoleColors.FG_BRIGHT_WHITE + safeUsed + "/" + total
                + RESET;
    }

    private String[] wrapLines(String text, int max) {
        if (text == null || text.trim().isEmpty()) {
            return new String[]{"(none)"};
        }

        ArrayList<String> lines = new ArrayList<>();
        String remaining = TerminalUI.stripAnsi(text).trim();

        while (remaining.length() > max) {
            int cut = remaining.lastIndexOf(' ', max);
            if (cut <= 0) cut = max;
            lines.add(remaining.substring(0, cut).trim());
            remaining = remaining.substring(cut).trim();
        }

        if (!remaining.isEmpty()) {
            lines.add(remaining);
        }

        return lines.toArray(new String[0]);
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
                            + box + panel + " ║" + RESET);
        } else {
            printRow(row, col,
                    box + panel + "║ "
                            + panel + getActiveTextColor() + display
                            + panel + getActiveTextColor() + spaces(inner - 2 - TerminalUI.stripAnsi(display).length())
                            + box + panel + " ║" + RESET);
        }
    }

    private void printTextRow(int row, int col, int inner, String text, String box, String panel) {
        String display = fitVisible(text, inner - 2);

        printRow(row, col,
                box + panel + "║ "
                        + panel + getActiveTextColor() + display
                        + panel + getActiveTextColor() + spaces(inner - 2 - TerminalUI.stripAnsi(display).length())
                        + box + panel + " ║" + RESET);
    }

    private void printRow(int row, int col, String text) {
        at(row, col);
        System.out.print(text);
    }

    private String fitVisible(String s, int max) {
        if (s == null) return "";
        String plain = TerminalUI.stripAnsi(s);
        if (plain.length() <= max) return s;
        if (max <= 1) return plain.substring(0, max);
        return plain.substring(0, max - 1) + "…";
    }

    private String padPlain(String s, int w) {
        if (s == null) s = "";
        if (s.length() >= w) return s.substring(0, w);
        return s + spaces(w - s.length());
    }

    private String spaces(int n) {
        return " ".repeat(Math.max(0, n));
    }
}