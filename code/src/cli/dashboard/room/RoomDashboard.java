package cli.dashboard.room;

import cli.dashboard.Dashboard;
import controllers.dashboard.room.RoomDashboardController;
import models.room.Room;
import utils.ConsoleColors;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static utils.TerminalUI.*;

public class RoomDashboard implements Dashboard {

    private final RoomDashboardController controller;

    private static final MenuItem[] MENU = {
            new MenuItem(1, "Add New Room"),
            new MenuItem(2, "View Available Rooms"),
            new MenuItem(0, "Back")
    };

    public RoomDashboard(RoomDashboardController controller) {
        this.controller = controller;
    }

    @Override
    public void show(String username) {
        while (true) {
            try {
                int choice = showHomeMenu();
                if (choice == 0) {
                    ConsoleUtil.clearScreen();
                    return;
                }

                if (choice == 1) {
                    showAddRoomWizard();
                } else if (choice == 2) {
                    showAvailableRoomsBrowser();
                }

            } catch (Exception e) {
                cleanup();
                ConsoleUtil.clearScreen();
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                TerminalUI.tError("[RoomDashboard] " + e.getMessage());
                TerminalUI.tPause();
            }
        }
    }

    private int showHomeMenu() throws Exception {
        int selected = 0;

        while (true) {
            drawHomeScreen(selected);

            int key = readNavKey();
            if (key == Key.UP) {
                selected = (selected - 1 + MENU.length) % MENU.length;
            } else if (key == Key.DOWN) {
                selected = (selected + 1) % MENU.length;
            } else if (key == Key.ENTER) {
                return MENU[selected].number();
            } else if (key >= 0 && key <= 9) {
                for (MenuItem item : MENU) {
                    if (item.number() == key) {
                        return key;
                    }
                }
            } else if (key == Key.ESC) {
                return 0;
            }
        }
    }

    private void drawHomeScreen(int selected) {
        ConsoleUtil.clearScreen();
        fillBackground(getActiveBgColor());
        System.out.print(HIDE_CUR);

        int totalW = Math.min(termW() - 4, 110);
        int totalCol = centerCol(totalW);
        int leftW = 40;
        int rightW = totalW - leftW - 3;
        int topRow = 3;

        List<String> left = new ArrayList<>();
        left.add("Select an action");
        left.add("");

        for (int i = 0; i < MENU.length; i++) {
            MenuItem item = MENU[i];
            String marker = i == selected ? "▶ " : "  ";
            left.add(marker + "[" + item.number() + "] " + item.label());
        }

        List<String> right = new ArrayList<>();
        String[] preview = controller.buildHomePreviewLines(MENU[selected].number());
        for (String line : preview) {
            right.add(line);
        }

        drawBox(topRow, totalCol, leftW, "ROOM MANAGEMENT", left, selected, 2);
        drawBox(topRow, totalCol + leftW + 3, rightW, "PREVIEW", right, -1, -1);

        int footerRow = topRow + Math.max(left.size(), right.size()) + 4;
        at(footerRow, totalCol);
        System.out.print(
                ConsoleColors.Accent.MUTED
                        + "Use ↑ ↓ to move, Enter to open, or press 0 to go back."
                        + RESET
        );
        System.out.flush();
    }

    private void showAddRoomWizard() throws Exception {
        while (true) {
            ConsoleUtil.clearScreen();
            fillBackground(getActiveBgColor());
            System.out.print(HIDE_CUR);

            int col = boxCol();
            int iw = innerW();
            int row = 4;

            at(row++, col);
            System.out.print(getActiveBoxColor() + getActivePanelBgColor() + "╔" + "═".repeat(iw) + "╗" + RESET);

            at(row++, col);
            System.out.print(
                    getActiveBoxColor() + getActivePanelBgColor() + "║"
                            + BOLD + getActiveTextColor() + getActivePanelBgColor()
                            + padC("ADD NEW ROOM", iw)
                            + getActiveBoxColor() + getActivePanelBgColor() + "║" + RESET
            );

            at(row++, col);
            System.out.print(getActiveBoxColor() + getActivePanelBgColor() + "╠" + "═".repeat(iw) + "╣" + RESET);

            at(row++, col);
            System.out.print(
                    getActiveBoxColor() + getActivePanelBgColor() + "║ "
                            + ConsoleColors.Accent.MUTED + getActivePanelBgColor()
                            + padL("Create a room, review the preview, then confirm.", iw - 2)
                            + getActiveBoxColor() + getActivePanelBgColor() + " ║" + RESET
            );

            at(row++, col);
            System.out.print(
                    getActiveBoxColor() + getActivePanelBgColor() + "║ "
                            + ConsoleColors.Accent.MUTED + getActivePanelBgColor()
                            + padL("Example room IDs: R101, A-204, South-12", iw - 2)
                            + getActiveBoxColor() + getActivePanelBgColor() + " ║" + RESET
            );

            at(row++, col);
            System.out.print(getActiveBoxColor() + getActivePanelBgColor() + "╠" + "═".repeat(iw) + "╣" + RESET);

            at(row++, col);
            System.out.print(
                    getActiveBoxColor() + getActivePanelBgColor() + "║ "
                            + ConsoleColors.Accent.INPUT + getActivePanelBgColor()
                            + padL("Room ID   : ", 12)
                            + getActiveTextColor() + getActivePanelBgColor()
                            + padL("", iw - 14)
                            + getActiveBoxColor() + getActivePanelBgColor() + " ║" + RESET
            );

            at(row++, col);
            System.out.print(
                    getActiveBoxColor() + getActivePanelBgColor() + "║ "
                            + ConsoleColors.Accent.INPUT + getActivePanelBgColor()
                            + padL("Capacity  : ", 12)
                            + getActiveTextColor() + getActivePanelBgColor()
                            + padL("", iw - 14)
                            + getActiveBoxColor() + getActivePanelBgColor() + " ║" + RESET
            );

            at(row++, col);
            System.out.print(getActiveBoxColor() + getActivePanelBgColor() + "╚" + "═".repeat(iw) + "╝" + RESET);

            setCooked();
            System.out.print(SHOW_CUR);

            at(10, col + 14);
            System.out.flush();
            String roomId = FastInput.readLine().trim();

            if ("0".equals(roomId)) {
                return;
            }

            at(11, col + 14);
            System.out.flush();
            String capRaw = FastInput.readLine().trim();

            int capacity;
            try {
                capacity = Integer.parseInt(capRaw);
            } catch (NumberFormatException e) {
                showNotice("Capacity must be a valid number.");
                continue;
            }

            if (roomId.isEmpty()) {
                showNotice("Room ID cannot be empty.");
                continue;
            }

            if (capacity <= 0) {
                showNotice("Capacity must be greater than zero.");
                continue;
            }

            int decision = showAddPreview(roomId, capacity);
            if (decision == 0) {
                return;
            }
            if (decision == 2) {
                continue;
            }

            boolean ok = controller.addRoom(roomId, capacity);
            if (ok) {
                showNotice("Room " + roomId + " added successfully.");
            } else {
                showNotice("Could not add room. It may already exist or input was invalid.");
            }
            return;
        }
    }

    private int showAddPreview(String roomId, int capacity) throws Exception {
        int selected = 0;
        MenuItem[] confirmMenu = {
                new MenuItem(1, "Create Room"),
                new MenuItem(2, "Re-enter Details"),
                new MenuItem(0, "Cancel")
        };

        while (true) {
            ConsoleUtil.clearScreen();
            fillBackground(getActiveBgColor());
            System.out.print(HIDE_CUR);

            int totalW = Math.min(termW() - 4, 110);
            int totalCol = centerCol(totalW);
            int leftW = 42;
            int rightW = totalW - leftW - 3;
            int topRow = 4;

            List<String> left = new ArrayList<>();
            left.add("Confirm new room");
            left.add("");
            for (int i = 0; i < confirmMenu.length; i++) {
                MenuItem item = confirmMenu[i];
                String marker = i == selected ? "▶ " : "  ";
                left.add(marker + "[" + item.number() + "] " + item.label());
            }

            List<String> right = new ArrayList<>();
            right.add("Room ID      : " + roomId);
            right.add("Capacity     : " + capacity);
            right.add("Occupancy    : 0/" + capacity);
            right.add("Status       : AVAILABLE");
            right.add("Free Seats   : " + capacity);
            right.add("");
            right.add("Occupancy Bar");
            right.add(buildOccupancyBar(0, capacity, Math.max(10, rightW - 10)));

            drawBox(topRow, totalCol, leftW, "CONFIRMATION", left, selected, 2);
            drawBox(topRow, totalCol + leftW + 3, rightW, "ROOM PREVIEW", right, -1, -1);

            int key = readNavKey();
            if (key == Key.UP) {
                selected = (selected - 1 + confirmMenu.length) % confirmMenu.length;
            } else if (key == Key.DOWN) {
                selected = (selected + 1) % confirmMenu.length;
            } else if (key == Key.ENTER) {
                return confirmMenu[selected].number();
            } else if (key >= 0 && key <= 9) {
                for (MenuItem item : confirmMenu) {
                    if (item.number() == key) {
                        return item.number();
                    }
                }
            } else if (key == Key.ESC) {
                return 0;
            }
        }
    }

    private void showAvailableRoomsBrowser() throws Exception {
        List<Room> rooms = controller.getAvailableRooms();
        if (rooms.isEmpty()) {
            showNotice("No rooms are currently available.");
            return;
        }

        int selected = 0;

        while (true) {
            drawAvailableBrowser(rooms, selected);

            int key = readNavKey();
            if (key == Key.UP) {
                selected = (selected - 1 + rooms.size()) % rooms.size();
            } else if (key == Key.DOWN) {
                selected = (selected + 1) % rooms.size();
            } else if (key == Key.ENTER || key == Key.ESC || key == 0) {
                return;
            }
        }
    }

    private void drawAvailableBrowser(List<Room> rooms, int selected) {
        ConsoleUtil.clearScreen();
        fillBackground(getActiveBgColor());
        System.out.print(HIDE_CUR);

        int totalW = Math.min(termW() - 4, 112);
        int totalCol = centerCol(totalW);
        int leftW = 44;
        int rightW = totalW - leftW - 3;
        int topRow = 3;

        int visible = Math.max(5, Math.min(10, termH() - 14));
        int start = Math.max(0, selected - visible / 2);
        if (start + visible > rooms.size()) {
            start = Math.max(0, rooms.size() - visible);
        }
        int end = Math.min(rooms.size(), start + visible);

        List<String> left = new ArrayList<>();
        left.add("Available rooms: " + rooms.size());
        left.add("");

        for (int i = start; i < end; i++) {
            Room room = rooms.get(i);
            String prefix = i == selected ? "▶ " : "  ";
            String status = room.getCurrentOccupancy() + "/" + room.getCapacity();
            left.add(prefix + padL(room.getRoomId(), 10) + "  " + status);
        }

        if (start > 0 || end < rooms.size()) {
            left.add("");
            left.add("Showing " + (start + 1) + "-" + end + " of " + rooms.size());
        }

        Room selectedRoom = rooms.get(selected);
        List<String> right = new ArrayList<>();
        right.add("Room ID        : " + selectedRoom.getRoomId());
        right.add("Status         : " + (selectedRoom.isAvailable() ? "AVAILABLE" : "FULL"));
        right.add("Occupancy      : " + selectedRoom.getCurrentOccupancy() + "/" + selectedRoom.getCapacity());
        right.add("Free Seats     : " + Math.max(0, selectedRoom.getCapacity() - selectedRoom.getCurrentOccupancy()));
        right.add("");
        right.add("Occupancy Bar");
        right.add(buildOccupancyBar(selectedRoom.getCurrentOccupancy(), selectedRoom.getCapacity(), Math.max(12, rightW - 10)));
        right.add("");
        right.add("Summary");
        right.add(selectedRoom.toString());
        right.add("");
        right.add("Tip");
        if (selectedRoom.getCurrentOccupancy() == 0) {
            right.add("This room is fully empty.");
        } else if (selectedRoom.getCurrentOccupancy() == selectedRoom.getCapacity() - 1) {
            right.add("Only one free seat remains.");
        } else {
            right.add("This room still has multiple open seats.");
        }

        drawBox(topRow, totalCol, leftW, "AVAILABLE ROOM LIST", left, selected - start + 2, 2);
        drawBox(topRow, totalCol + leftW + 3, rightW, "ROOM PREVIEW", right, -1, -1);

        int footerRow = topRow + Math.max(left.size(), right.size()) + 4;
        at(footerRow, totalCol);
        System.out.print(
                ConsoleColors.Accent.MUTED
                        + "Use ↑ ↓ to inspect rooms. Press Enter or 0 to return."
                        + RESET
        );
        System.out.flush();
    }

    private void drawBox(int row, int col, int width, String title, List<String> lines, int selectedLineIndex, int contentStartLine) {
        String box = getActiveBoxColor();
        String panel = getActivePanelBgColor();
        String text = getActiveTextColor();

        int inner = Math.max(10, width - 2);

        at(row++, col);
        System.out.print(box + panel + "╔" + "═".repeat(inner) + "╗" + RESET);

        at(row++, col);
        System.out.print(
                box + panel + "║"
                        + BOLD + text + panel + padC(title, inner)
                        + box + panel + "║" + RESET
        );

        at(row++, col);
        System.out.print(box + panel + "╠" + "═".repeat(inner) + "╣" + RESET);

        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i) == null ? "" : lines.get(i);
            String display = raw.length() > inner - 2 ? raw.substring(0, inner - 2) : raw;

            boolean selected = selectedLineIndex >= 0 && i >= contentStartLine && i == selectedLineIndex;
            String rowBg = selected ? ConsoleColors.bgRGB(185, 165, 220) : panel;
            String rowFg = selected ? ConsoleColors.fgRGB(25, 15, 55) : text;

            at(row++, col);
            System.out.print(
                    box + panel + "║ "
                            + rowBg + rowFg + padL(display, inner - 2)
                            + box + panel + " ║" + RESET
            );
        }

        at(row, col);
        System.out.print(box + panel + "╚" + "═".repeat(inner) + "╝" + RESET);
        System.out.flush();
    }

    private void showNotice(String message) {
        ConsoleUtil.clearScreen();
        fillBackground(getActiveBgColor());
        tBoxTop();
        tBoxTitle("ROOM MANAGEMENT");
        tBoxSep();
        tBoxLine(message);
        tBoxSep();
        tBoxLine("Press Enter to continue...");
        tBoxBottom();
        FastInput.readLine();
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
                System.out.print(SHOW_CUR);
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
            System.out.print(SHOW_CUR);
            System.out.flush();
        }
    }
}