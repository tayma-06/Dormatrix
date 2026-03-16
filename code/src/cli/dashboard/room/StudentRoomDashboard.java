package cli.dashboard.room;

import cli.forms.room.RoomChangeApplicationForm;
import cli.views.room.StudentRoomView;
import controllers.dashboard.room.StudentRoomDashboardController;
import libraries.collections.MyArrayList;
import models.complaints.Complaint;
import models.room.Room;
import models.room.RoomChangeApplication;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;
import utils.ConsoleColors;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

public class StudentRoomDashboard {

    private final StudentRoomDashboardController controller;
    private final StudentRoomView view;

    public StudentRoomDashboard(StudentRoomDashboardController controller) {
        this.controller = controller;
        this.view = new StudentRoomView();
    }

    private enum NavKey {
        UP, DOWN, ENTER, ZERO, ESC, NONE
    }

    public void show(String studentIdentifier) {
        while (true) {
            String roomNumber = controller.getStudentRoomNumber(studentIdentifier);
            Room room = controller.getRoomDetails(roomNumber);

            ConsoleUtil.clearScreen();
            int choice = view.show(roomNumber, room);

            if (choice == 0) {
                ConsoleUtil.clearScreen();
                return;
            }

            if (choice == 1) {
                if (roomNumber.equals("UNASSIGNED") || roomNumber.equals("N/A")) {
                    TerminalUI.tError("You do not have a room assigned yet.");
                    TerminalUI.tPause();
                } else {
                    showComplaints(roomNumber);
                }
            } else if (choice == 2) {
                new RoomChangeApplicationForm(controller).show(studentIdentifier, roomNumber);
            } else if (choice == 3) {
                showMyApplications(studentIdentifier);
            }
        }
    }

    public void showComplaints(String roomNumber) {
        MyArrayList<Complaint> list = controller.getComplaints(roomNumber);

        if (list.size() == 0) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.tError("No complaints found for room " + roomNumber + ".");
            TerminalUI.tPause();
            return;
        }

        int selected = 0;

        while (true) {
            if (selected >= list.size()) selected = list.size() - 1;
            if (selected < 0) selected = 0;

            renderComplaintPreview(roomNumber, list, selected);
            NavKey key = readNavKey();

            if (key == NavKey.UP) {
                selected = (selected - 1 + list.size()) % list.size();
            } else if (key == NavKey.DOWN) {
                selected = (selected + 1) % list.size();
            } else if (key == NavKey.ENTER || key == NavKey.ZERO || key == NavKey.ESC) {
                TerminalUI.cleanup();
                return;
            }
        }
    }

    public void showMyApplications(String studentIdentifier) {
        MyArrayList<RoomChangeApplication> list = controller.getRoomChangeApplications(studentIdentifier);

        if (list.size() == 0) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.tError("You have not submitted any room change application.");
            TerminalUI.tPause();
            return;
        }

        int selected = 0;

        while (true) {
            if (selected >= list.size()) selected = list.size() - 1;
            if (selected < 0) selected = 0;

            renderApplicationPreview(list, selected);
            NavKey key = readNavKey();

            if (key == NavKey.UP) {
                selected = (selected - 1 + list.size()) % list.size();
            } else if (key == NavKey.DOWN) {
                selected = (selected + 1) % list.size();
            } else if (key == NavKey.ENTER || key == NavKey.ZERO || key == NavKey.ESC) {
                TerminalUI.cleanup();
                return;
            }
        }
    }

    private void renderComplaintPreview(String roomNumber, MyArrayList<Complaint> list, int selected) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        System.out.print(TerminalUI.HIDE_CUR);

        int screenW = Math.max(96, TerminalUI.termW() - 4);
        int leftW = Math.min(44, Math.max(38, screenW / 2 - 2));
        int rightW = Math.max(44, screenW - leftW - 3);

        int totalW = leftW + rightW + 3;
        int leftCol = TerminalUI.centerCol(totalW);
        int rightCol = leftCol + leftW + 3;
        int topRow = 3;

        drawComplaintLeftPane(topRow, leftCol, leftW, roomNumber, list, selected);
        drawComplaintRightPane(topRow, rightCol, rightW, list.get(selected));

        System.out.flush();
    }

    private void renderApplicationPreview(MyArrayList<RoomChangeApplication> list, int selected) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        System.out.print(TerminalUI.HIDE_CUR);

        int screenW = Math.max(98, TerminalUI.termW() - 4);
        int leftW = Math.min(46, Math.max(40, screenW / 2 - 2));
        int rightW = Math.max(46, screenW - leftW - 3);

        int totalW = leftW + rightW + 3;
        int leftCol = TerminalUI.centerCol(totalW);
        int rightCol = leftCol + leftW + 3;
        int topRow = 3;

        drawApplicationLeftPane(topRow, leftCol, leftW, list, selected);
        drawApplicationRightPane(topRow, rightCol, rightW, list.get(selected));

        System.out.flush();
    }

    private void drawComplaintLeftPane(int topRow, int col, int width, String roomNumber, MyArrayList<Complaint> list, int selected) {
        int inner = width - 2;
        String box = TerminalUI.getActiveBoxColor();
        String panel = TerminalUI.getActivePanelBgColor();
        int row = topRow;
        int resultRows = 10;

        printRow(row++, col, box + panel + "╔" + "═".repeat(inner) + "╗" + TerminalUI.RESET);
        printRow(row++, col,
                box + panel + "║"
                        + TerminalUI.BOLD + TerminalUI.ACCENT + panel
                        + TerminalUI.padC("ROOM COMPLAINTS", inner)
                        + box + panel + "║" + TerminalUI.RESET);
        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);

        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE + "Room: " + roomNumber + "   |   Total: " + list.size(),
                box, panel);

        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);

        int start = Math.max(0, selected - resultRows / 2);
        if (start + resultRows > list.size()) {
            start = Math.max(0, list.size() - resultRows);
        }
        int end = Math.min(list.size(), start + resultRows);

        for (int i = start; i < end; i++) {
            Complaint c = list.get(i);
            boolean isSelected = i == selected;

            String line = c.getComplaintId() + "  |  " + c.getStatus().name();
            printSuggestionRow(row++, col, inner, line, isSelected, box, panel);
        }

        while (row < topRow + 17) {
            printSuggestionRow(row++, col, inner, "", false, box, panel);
        }

        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);
        printTextRow(row++, col, inner,
                ConsoleColors.Accent.MUTED + "Use Up/Down to browse. Enter or 0 returns.",
                box, panel);
        printRow(row, col, box + panel + "╚" + "═".repeat(inner) + "╝" + TerminalUI.RESET);
    }

    private void drawComplaintRightPane(int topRow, int col, int width, Complaint c) {
        int inner = width - 2;
        String box = TerminalUI.getActiveBoxColor();
        String panel = TerminalUI.getActivePanelBgColor();
        int row = topRow;

        printRow(row++, col, box + panel + "╔" + "═".repeat(inner) + "╗" + TerminalUI.RESET);
        printRow(row++, col,
                box + panel + "║"
                        + TerminalUI.BOLD + TerminalUI.ACCENT + panel
                        + TerminalUI.padC("COMPLAINT DETAILS", inner)
                        + box + panel + "║" + TerminalUI.RESET);
        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);

        printTextRow(row++, col, inner, kv("Complaint ID", c.getComplaintId()), box, panel);
        printTextRow(row++, col, inner, kv("Status", colorStatus(c.getStatus().name())), box, panel);
        printTextRow(row++, col, inner, kv("Category", c.getCategory().name()), box, panel);
        printTextRow(row++, col, inner, kv("Priority", colorPriority(c.getPriority().name())), box, panel);

        String worker = c.getAssignedWorkerId();
        if (worker == null || worker.trim().isEmpty()) {
            worker = ConsoleColors.Accent.MUTED + "(not assigned)" + TerminalUI.RESET;
        }
        printTextRow(row++, col, inner, kv("Worker", worker), box, panel);

        printTextRow(row++, col, inner, "", box, panel);
        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE + "DESCRIPTION",
                box, panel);

        String[] desc = wrap(c.getDescription(), Math.max(20, inner - 2));
        for (int i = 0; i < 5; i++) {
            String line = i < desc.length ? desc[i] : "";
            printTextRow(row++, col, inner, ConsoleColors.FG_BRIGHT_WHITE + line, box, panel);
        }

        printTextRow(row++, col, inner, "", box, panel);
        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE + "QUICK VIEW",
                box, panel);

        String hint;
        String status = c.getStatus().name().toUpperCase();
        if (status.contains("RESOLVED") || status.contains("DONE") || status.contains("CLOSED")) {
            hint = "This complaint looks resolved.";
        } else if (status.contains("PROGRESS") || status.contains("WORK")) {
            hint = "Work appears to be in progress.";
        } else {
            hint = "This complaint is still waiting for action.";
        }

        printTextRow(row++, col, inner, ConsoleColors.Accent.MUTED + hint, box, panel);

        while (row < topRow + 19) {
            printTextRow(row++, col, inner, "", box, panel);
        }

        printRow(row, col, box + panel + "╚" + "═".repeat(inner) + "╝" + TerminalUI.RESET);
    }

    private void drawApplicationLeftPane(int topRow, int col, int width, MyArrayList<RoomChangeApplication> list, int selected) {
        int inner = width - 2;
        String box = TerminalUI.getActiveBoxColor();
        String panel = TerminalUI.getActivePanelBgColor();
        int row = topRow;
        int resultRows = 10;

        printRow(row++, col, box + panel + "╔" + "═".repeat(inner) + "╗" + TerminalUI.RESET);
        printRow(row++, col,
                box + panel + "║"
                        + TerminalUI.BOLD + TerminalUI.ACCENT + panel
                        + TerminalUI.padC("MY ROOM CHANGE APPLICATIONS", inner)
                        + box + panel + "║" + TerminalUI.RESET);
        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);

        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE + "Applications: " + list.size(),
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

            String line = app.getApplicationId() + "  |  " + app.getRequestedRoom();
            printSuggestionRow(row++, col, inner, line, isSelected, box, panel);
        }

        while (row < topRow + 17) {
            printSuggestionRow(row++, col, inner, "", false, box, panel);
        }

        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);
        printTextRow(row++, col, inner,
                ConsoleColors.Accent.MUTED + "Use Up/Down to browse. Enter or 0 returns.",
                box, panel);
        printRow(row, col, box + panel + "╚" + "═".repeat(inner) + "╝" + TerminalUI.RESET);
    }

    private void drawApplicationRightPane(int topRow, int col, int width, RoomChangeApplication app) {
        int inner = width - 2;
        String box = TerminalUI.getActiveBoxColor();
        String panel = TerminalUI.getActivePanelBgColor();
        int row = topRow;

        printRow(row++, col, box + panel + "╔" + "═".repeat(inner) + "╗" + TerminalUI.RESET);
        printRow(row++, col,
                box + panel + "║"
                        + TerminalUI.BOLD + TerminalUI.ACCENT + panel
                        + TerminalUI.padC("APPLICATION DETAILS", inner)
                        + box + panel + "║" + TerminalUI.RESET);
        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);

        printTextRow(row++, col, inner, kv("Application ID", app.getApplicationId()), box, panel);
        printTextRow(row++, col, inner, kv("Current Room", app.getCurrentRoom()), box, panel);
        printTextRow(row++, col, inner, kv("Requested Room", app.getRequestedRoom()), box, panel);
        printTextRow(row++, col, inner, kv("Status", colorStatus(app.getStatus().name())), box, panel);
        printTextRow(row++, col, inner, kv("Submitted At", app.getSubmittedAt()), box, panel);

        String reviewedBy = app.getReviewedBy();
        if (reviewedBy == null || reviewedBy.trim().isEmpty()) {
            reviewedBy = ConsoleColors.Accent.MUTED + "(not reviewed)" + TerminalUI.RESET;
        }
        printTextRow(row++, col, inner, kv("Reviewed By", reviewedBy), box, panel);

        String reviewedAt = app.getReviewedAt();
        if (reviewedAt == null || reviewedAt.trim().isEmpty()) {
            reviewedAt = ConsoleColors.Accent.MUTED + "(not reviewed)" + TerminalUI.RESET;
        }
        printTextRow(row++, col, inner, kv("Reviewed At", reviewedAt), box, panel);

        printTextRow(row++, col, inner, "", box, panel);
        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE + "REASON",
                box, panel);

        String[] reason = wrap(app.getReason(), Math.max(20, inner - 2));
        for (int i = 0; i < 3; i++) {
            String line = i < reason.length ? reason[i] : "";
            printTextRow(row++, col, inner, ConsoleColors.FG_BRIGHT_WHITE + line, box, panel);
        }

        printTextRow(row++, col, inner, "", box, panel);
        printTextRow(row++, col, inner,
                ConsoleColors.ThemeText.SOFT_WHITE + "REVIEW NOTE",
                box, panel);

        String review = app.getReviewNote();
        if (review == null || review.trim().isEmpty()) review = "(none)";
        String[] note = wrap(review, Math.max(20, inner - 2));
        for (int i = 0; i < 3; i++) {
            String line = i < note.length ? note[i] : "";
            printTextRow(row++, col, inner, ConsoleColors.FG_BRIGHT_WHITE + line, box, panel);
        }

        while (row < topRow + 19) {
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
                + String.format("%-12s : ", key)
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
        if (up.contains("PROGRESS") || up.contains("ASSIGNED") || up.contains("WORK")) {
            return ConsoleColors.Accent.SUCCESS + status + TerminalUI.RESET;
        }
        if (up.contains("REJECT") || up.contains("FAILED")) {
            return ConsoleColors.Accent.ERROR + status + TerminalUI.RESET;
        }
        if (up.contains("COMPLETED") || up.contains("RESOLVED") || up.contains("CLOSED")) {
            return ConsoleColors.Accent.SUCCESS + status + TerminalUI.RESET;
        }
        return status;
    }

    private String colorPriority(String priority) {
        if (priority == null) return "N/A";

        String up = priority.toUpperCase();
        if (up.contains("HIGH") || up.contains("URGENT")) {
            return ConsoleColors.Accent.ERROR + priority + TerminalUI.RESET;
        }
        if (up.contains("MEDIUM")) {
            return ConsoleColors.Accent.WARNING + priority + TerminalUI.RESET;
        }
        return ConsoleColors.Accent.SUCCESS + priority + TerminalUI.RESET;
    }

    private void printSuggestionRow(int row, int col, int inner, String text, boolean selected, String box, String panel) {
        String display = fitVisible(text, inner - 2);

        if (selected) {
            String bg = ConsoleColors.bgRGB(210, 195, 245);
            String fg = ConsoleColors.fgRGB(35, 20, 70);

            printRow(row, col,
                    box + panel + "║ "
                            + bg + fg + padVisible(display, inner - 2)
                            + box + panel + " ║" + TerminalUI.RESET);
        } else {
            printRow(row, col,
                    box + panel + "║ "
                            + TerminalUI.getActiveTextColor() + panel + padVisible(display, inner - 2)
                            + box + panel + " ║" + TerminalUI.RESET);
        }
    }

    private void printTextRow(int row, int col, int inner, String text, String box, String panel) {
        String display = fitVisible(text, inner - 2);
        printRow(row, col,
                box + panel + "║ "
                        + panel + display + spaces(inner - 2 - TerminalUI.stripAnsi(display).length())
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

    private String padVisible(String s, int w) {
        int len = TerminalUI.stripAnsi(s).length();
        if (len >= w) return fitVisible(s, w);
        return s + spaces(w - len);
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