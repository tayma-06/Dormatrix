package cli.dashboard.room;

import controllers.dashboard.room.RoomChangeRequestDashboardController;
import libraries.collections.MyArrayList;
import models.room.RoomChangeApplication;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;
import utils.ConsoleColors;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

public class RoomChangeRequestDashboard {

    private final RoomChangeRequestDashboardController controller;

    public RoomChangeRequestDashboard(RoomChangeRequestDashboardController controller) {
        this.controller = controller;
    }

    private enum NavKey {
        UP, DOWN, ENTER, ZERO, APPROVE, REJECT, NONE
    }

    public void show(String officerName) {
        int selected = 0;

        while (true) {
            MyArrayList<RoomChangeApplication> list = controller.getPendingApplications();

            if (list.size() == 0) {
                ConsoleUtil.clearScreen();
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                TerminalUI.tError("No pending room change applications.");
                TerminalUI.tPause();
                return;
            }

            if (selected >= list.size()) selected = list.size() - 1;
            if (selected < 0) selected = 0;

            render(list, selected);
            NavKey key = readNavKey();

            if (key == NavKey.UP) {
                selected = (selected - 1 + list.size()) % list.size();
            } else if (key == NavKey.DOWN) {
                selected = (selected + 1) % list.size();
            } else if (key == NavKey.ZERO || key == NavKey.ENTER) {
                TerminalUI.cleanup();
                return;
            } else if (key == NavKey.APPROVE) {
                String result = controller.approveAndMove(
                        list.get(selected).getApplicationId(),
                        officerName,
                        "Approved and moved by Hall Officer."
                );
                showResult(result);
            } else if (key == NavKey.REJECT) {
                String result = controller.reject(
                        list.get(selected).getApplicationId(),
                        officerName,
                        "Rejected by Hall Officer."
                );
                showResult(result);
            }
        }
    }

    private void render(MyArrayList<RoomChangeApplication> list, int selected) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        System.out.print(TerminalUI.HIDE_CUR);

        int termW = TerminalUI.termW();
        int leftCol = Math.max(3, termW / 14);
        int topRow = 5;
        int leftW = Math.min(46, Math.max(38, termW / 3));
        int rightCol = leftCol + leftW + 3;
        int rightW = Math.max(44, termW - rightCol - leftCol);

        drawPanel(topRow, leftCol, leftW, 17, "ROOM CHANGE REQUESTS");
        drawPanel(topRow, rightCol, rightW, 19, "APPLICATION DETAILS");

        put(topRow + 2, leftCol + 2,
                ConsoleColors.ThemeText.SOFT_WHITE
                        + "Pending requests: " + list.size()
                        + TerminalUI.RESET);

        int start = Math.max(0, selected - 4);
        int end = Math.min(list.size(), start + 10);
        if (end - start < 10) {
            start = Math.max(0, end - 10);
        }

        int row = topRow + 4;
        for (int i = start; i < end; i++) {
            RoomChangeApplication app = list.get(i);
            boolean isSelected = i == selected;

            String line = String.format("%-14s %-10s",
                    app.getApplicationId(),
                    app.getRequestedRoom());

            String fg = isSelected ? ConsoleColors.FG_BLACK : ConsoleColors.ThemeText.SOFT_WHITE;
            String bg = isSelected ? ConsoleColors.bgRGB(210, 195, 245) : TerminalUI.getActivePanelBgColor();

            put(row++, leftCol + 2, bg + fg + pad(line, leftW - 4) + TerminalUI.RESET);
        }

        RoomChangeApplication app = list.get(selected);
        put(topRow + 2, rightCol + 2, kv("Application ID", app.getApplicationId()));
        put(topRow + 3, rightCol + 2, kv("Student ID", app.getStudentId()));
        put(topRow + 4, rightCol + 2, kv("Student Name", app.getStudentName()));
        put(topRow + 5, rightCol + 2, kv("Current Room", app.getCurrentRoom()));
        put(topRow + 6, rightCol + 2, kv("Requested Room", app.getRequestedRoom()));
        put(topRow + 7, rightCol + 2, kv("Status", app.getStatus().name()));
        put(topRow + 8, rightCol + 2, kv("Submitted At", app.getSubmittedAt()));

        put(topRow + 10, rightCol + 2,
                ConsoleColors.ThemeText.SOFT_WHITE + "Reason" + TerminalUI.RESET);

        String[] reason = wrap(app.getReason(), Math.max(18, rightW - 6));
        for (int i = 0; i < Math.min(reason.length, 4); i++) {
            put(topRow + 11 + i, rightCol + 2,
                    ConsoleColors.FG_BRIGHT_WHITE + reason[i] + TerminalUI.RESET);
        }

        put(topRow + 18, leftCol,
                ConsoleColors.Accent.MUTED
                        + "Up/Down browse   A approve+move   R reject   Enter/0 return"
                        + TerminalUI.RESET);

        System.out.flush();
    }

    private void showResult(String result) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());

        if (result.toLowerCase().contains("successfully") || result.toLowerCase().contains("completed")) {
            TerminalUI.tSuccess(result);
        } else {
            TerminalUI.tError(result);
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
                        continue;
                    }

                    if (ch == 13 || ch == 10) return NavKey.ENTER;
                    if (ch == '0') return NavKey.ZERO;
                    if (ch == 'a' || ch == 'A') return NavKey.APPROVE;
                    if (ch == 'r' || ch == 'R') return NavKey.REJECT;
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
        if ("a".equalsIgnoreCase(line)) return NavKey.APPROVE;
        if ("r".equalsIgnoreCase(line)) return NavKey.REJECT;
        return NavKey.ENTER;
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

    private String kv(String key, String value) {
        return ConsoleColors.ThemeText.SOFT_WHITE
                + String.format("%-14s : ", key)
                + ConsoleColors.FG_BRIGHT_WHITE
                + (value == null ? "N/A" : value)
                + TerminalUI.RESET;
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

    private String[] wrap(String text, int max) {
        if (text == null || text.trim().isEmpty()) {
            return new String[]{"(none)"};
        }

        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        String remaining = text.trim();

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
}