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
        UP, DOWN, ENTER, ZERO, ESC, APPROVE, REJECT, NONE
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

            render(list, selected, officerName);
            NavKey key = readNavKey();

            if (key == NavKey.UP) {
                selected = (selected - 1 + list.size()) % list.size();
            } else if (key == NavKey.DOWN) {
                selected = (selected + 1) % list.size();
            } else if (key == NavKey.ZERO || key == NavKey.ENTER || key == NavKey.ESC) {
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

    private void render(MyArrayList<RoomChangeApplication> list, int selected, String officerName) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        System.out.print(TerminalUI.HIDE_CUR);

        int screenW = Math.max(100, TerminalUI.termW() - 4);
        int leftW = Math.min(44, Math.max(38, screenW / 2 - 2));
        int rightW = Math.max(50, screenW - leftW - 3);

        int totalW = leftW + rightW + 3;
        int leftCol = TerminalUI.centerCol(totalW);
        int rightCol = leftCol + leftW + 3;
        int topRow = 3;

        drawLeftPane(topRow, leftCol, leftW, list, selected, officerName);
        drawRightPane(topRow, rightCol, rightW, list.get(selected));

        System.out.flush();
    }

    private void drawLeftPane(int topRow, int col, int width, MyArrayList<RoomChangeApplication> list, int selected, String officerName) {
        int inner = width - 2;
        String box = TerminalUI.getActiveBoxColor();
        String panel = TerminalUI.getActivePanelBgColor();

        int row = topRow;
        int resultRows = 10;

        printRow(row++, col, box + panel + "╔" + "═".repeat(inner) + "╗" + TerminalUI.RESET);
        printRow(row++, col,
                box + panel + "║"
                        + TerminalUI.BOLD + TerminalUI.ACCENT + panel
                        + TerminalUI.padC("PENDING ROOM CHANGE REQUESTS", inner)
                        + box + panel + "║" + TerminalUI.RESET);
        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);

        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE
                        + "Pending: " + list.size()
                        + "   |   Officer: " + officerName,
                box, panel);

        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);

        int start = Math.max(0, selected - resultRows / 2);
        if (start + resultRows > list.size()) {
            start = Math.max(0, list.size() - resultRows);
        }
        int end = Math.min(list.size(), start + resultRows);

        for (int i = start; i < end; i++) {
            RoomChangeApplication app = list.get(i);
            boolean isSelected = i == selected;

            String line = app.getStudentId() + "  |  " + app.getRequestedRoom();
            printSuggestionRow(row++, col, inner, line, isSelected, box, panel);
        }

        while (row < topRow + 17) {
            printSuggestionRow(row++, col, inner, "", false, box, panel);
        }

        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);
        printTextRow(row++, col, inner,
                ConsoleColors.Accent.MUTED
                        + "Use Up/Down to browse. A approves, R rejects, Enter/0 returns.",
                box, panel);
        printRow(row, col, box + panel + "╚" + "═".repeat(inner) + "╝" + TerminalUI.RESET);
    }

    private void drawRightPane(int topRow, int col, int width, RoomChangeApplication app) {
        int inner = width - 2;
        String box = TerminalUI.getActiveBoxColor();
        String panel = TerminalUI.getActivePanelBgColor();

        int row = topRow;

        printRow(row++, col, box + panel + "╔" + "═".repeat(inner) + "╗" + TerminalUI.RESET);
        printRow(row++, col,
                box + panel + "║"
                        + TerminalUI.BOLD + TerminalUI.ACCENT + panel
                        + TerminalUI.padC("REQUEST REVIEW", inner)
                        + box + panel + "║" + TerminalUI.RESET);
        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);

        printTextRow(row++, col, inner, kv("Application ID", app.getApplicationId()), box, panel);
        printTextRow(row++, col, inner, kv("Student ID", app.getStudentId()), box, panel);
        printTextRow(row++, col, inner, kv("Student Name", app.getStudentName()), box, panel);
        printTextRow(row++, col, inner, kv("Current Room", app.getCurrentRoom()), box, panel);
        printTextRow(row++, col, inner, kv("Requested Room", app.getRequestedRoom()), box, panel);
        printTextRow(row++, col, inner, kv("Status", colorStatus(app.getStatus().name())), box, panel);
        printTextRow(row++, col, inner, kv("Submitted At", app.getSubmittedAt()), box, panel);

        printTextRow(row++, col, inner, "", box, panel);
        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE + "REASON",
                box, panel);

        String[] reason = wrap(app.getReason(), Math.max(20, inner - 2));
        for (int i = 0; i < 4; i++) {
            String line = i < reason.length ? reason[i] : "";
            printTextRow(row++, col, inner, ConsoleColors.FG_BRIGHT_WHITE + line, box, panel);
        }

        printTextRow(row++, col, inner, "", box, panel);
        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE + "REVIEW ACTIONS",
                box, panel);
        printTextRow(row++, col, inner,
                ConsoleColors.Accent.SUCCESS + "[A] Approve and move student",
                box, panel);
        printTextRow(row++, col, inner,
                ConsoleColors.Accent.ERROR + "[R] Reject application",
                box, panel);
        printTextRow(row++, col, inner,
                ConsoleColors.Accent.MUTED + "[Enter/0] Return to previous menu",
                box, panel);

        while (row < topRow + 19) {
            printTextRow(row++, col, inner, "", box, panel);
        }

        printRow(row, col, box + panel + "╚" + "═".repeat(inner) + "╝" + TerminalUI.RESET);
    }

    private void showResult(String result) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());

        String safe = result == null ? "Operation finished." : result;
        String lower = safe.toLowerCase();

        if (lower.contains("successfully") || lower.contains("completed")) {
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
                    if (ch == 'a' || ch == 'A') return NavKey.APPROVE;
                    if (ch == 'r' || ch == 'R') return NavKey.REJECT;
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
        if ("a".equalsIgnoreCase(line)) return NavKey.APPROVE;
        if ("r".equalsIgnoreCase(line)) return NavKey.REJECT;
        return NavKey.ENTER;
    }

    private String kv(String key, String value) {
        return ConsoleColors.ThemeText.SOFT_WHITE
                + String.format("%-14s : ", key)
                + ConsoleColors.FG_BRIGHT_WHITE
                + (value == null ? "N/A" : value)
                + TerminalUI.RESET;
    }

    private String colorStatus(String status) {
        if (status == null) return "N/A";

        String up = status.toUpperCase();
        if (up.contains("PENDING") || up.contains("OPEN")) {
            return ConsoleColors.Accent.WARNING + status + TerminalUI.RESET;
        }
        if (up.contains("REJECT")) {
            return ConsoleColors.Accent.ERROR + status + TerminalUI.RESET;
        }
        if (up.contains("APPROVED") || up.contains("COMPLETED")) {
            return ConsoleColors.Accent.SUCCESS + status + TerminalUI.RESET;
        }
        return status;
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