package cli.dashboard.room;

import cli.dashboard.Dashboard;
import cli.views.room.StudentRoomView;
import controllers.dashboard.room.StudentRoomDashboardController;
import libraries.collections.MyArrayList;
import libraries.collections.MyString;
import models.complaints.Complaint;
import models.enums.ComplaintStatus;
import models.room.Room;
import utils.ConsoleColors;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StudentRoomDashboard implements Dashboard {

    private final StudentRoomDashboardController controller;
    private final StudentRoomView view;

    public StudentRoomDashboard(StudentRoomDashboardController controller) {
        this.controller = controller;
        this.view = new StudentRoomView();
    }

    @Override
    public void show(String studentIdentifier) {
        String roomNumber = controller.getStudentRoomNumber(studentIdentifier);
        Room room = controller.getRoomDetails(roomNumber);

        while (true) {
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
            }
        }
    }

    public void showComplaints(String roomNumber) {
        MyArrayList<Complaint> list = controller.getComplaints(roomNumber);

        if (list.size() == 0) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            TerminalUI.tBoxTop();
            TerminalUI.tBoxTitle("ROOM COMPLAINTS");
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("Room : " + roomNumber);
            TerminalUI.tBoxLine("No complaints found for this room.");
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("Press Enter to continue...");
            TerminalUI.tBoxBottom();
            FastInput.readLine();
            return;
        }

        int selected = 0;

        while (true) {
            drawComplaintBrowser(roomNumber, list, selected);

            int key;
            try {
                key = readNavKey();
            } catch (Exception e) {
                return;
            }

            if (key == Key.UP) {
                selected = (selected - 1 + list.size()) % list.size();
            } else if (key == Key.DOWN) {
                selected = (selected + 1) % list.size();
            } else if (key == Key.ENTER || key == Key.ESC || key == 0) {
                return;
            }
        }
    }

    private void drawComplaintBrowser(String roomNumber, MyArrayList<Complaint> list, int selected) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        System.out.print(TerminalUI.HIDE_CUR);

        int totalW = Math.min(TerminalUI.termW() - 4, 116);
        int totalCol = TerminalUI.centerCol(totalW);
        int leftW = 44;
        int rightW = totalW - leftW - 3;
        int topRow = 3;

        int visible = Math.max(5, Math.min(10, TerminalUI.termH() - 14));
        int start = Math.max(0, selected - visible / 2);
        if (start + visible > list.size()) {
            start = Math.max(0, list.size() - visible);
        }
        int end = Math.min(list.size(), start + visible);

        List<String> left = new ArrayList<>();
        left.add("Room: " + roomNumber + "   Total: " + list.size());
        left.add("");

        for (int i = start; i < end; i++) {
            Complaint c = list.get(i);
            String marker = i == selected ? "> " : "  ";
            String id = TerminalUI.padL(c.getComplaintId(), 12);
            String status = shortStatus(c.getStatus());
            left.add(marker + id + "  " + status);
        }

        if (start > 0 || end < list.size()) {
            left.add("");
            left.add("Showing " + (start + 1) + "-" + end + " of " + list.size());
        }

        Complaint c = list.get(selected);

        List<String> right = new ArrayList<>();
        right.add("Complaint ID   : " + c.getComplaintId());
        right.add("Status         : " + coloredStatus(c.getStatus()));
        right.add("Category       : " + pretty(c.getCategory().name()));
        right.add("Priority       : " + pretty(String.valueOf(c.getPriority())));
        right.add("AssignedWorker : " + workerText(c));
        right.add("Student        : " + valueOrDash(c.getStudentName()));
        right.add("Room           : " + valueOrDash(c.getStudentRoomNo()));
        right.add("");

        right.add("Description");
        String[] wrappedDesc = ConsoleUtil.wrapText(valueOrDash(c.getDescription()), Math.max(18, rightW - 4));
        for (String line : wrappedDesc) {
            right.add(line);
        }

        String tags = c.getTags();
        if (tags != null && !tags.trim().isEmpty()) {
            right.add("");
            right.add("Tags");
            String[] wrappedTags = ConsoleUtil.wrapText(tags.trim(), Math.max(18, rightW - 4));
            for (String line : wrappedTags) {
                right.add(line);
            }
        }

        drawPanel(topRow, totalCol, leftW, "COMPLAINT LIST", left, selected - start + 2, 2);
        drawPanel(topRow, totalCol + leftW + 3, rightW, "COMPLAINT DETAILS", right, -1, -1);

        int footerRow = topRow + Math.max(left.size(), right.size()) + 4;
        TerminalUI.at(footerRow, totalCol);
        System.out.print(
                ConsoleColors.Accent.MUTED
                        + "Use Up/Down keys to inspect complaint details. Press Enter or 0 to return."
                        + TerminalUI.RESET
        );
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

    private String pretty(String value) {
        return value == null ? "" : value.replace('_', ' ');
    }

    private String shortStatus(ComplaintStatus status) {
        if (status == null) return "UNKNOWN";
        return switch (status) {
            case SUBMITTED -> "SUBMITTED";
            case ASSIGNED -> "ASSIGNED";
            case IN_PROGRESS -> "IN-PROGRESS";
            case RESOLVED -> "RESOLVED";
        };
    }

    private String coloredStatus(ComplaintStatus status) {
        if (status == null) return "UNKNOWN";
        return switch (status) {
            case SUBMITTED -> ConsoleColors.Accent.WARNING + "SUBMITTED" + TerminalUI.RESET;
            case ASSIGNED -> ConsoleColors.Accent.INPUT + "ASSIGNED" + TerminalUI.RESET;
            case IN_PROGRESS -> ConsoleColors.ThemeText.STUDENT_TEXT + "IN_PROGRESS" + TerminalUI.RESET;
            case RESOLVED -> ConsoleColors.Accent.SUCCESS + "RESOLVED" + TerminalUI.RESET;
        };
    }

    private String workerText(Complaint c) {
        String wid = c.getAssignedWorkerId();
        boolean blank = wid == null || new MyString(wid).trim().isEmpty();
        return blank ? "(not assigned)" : wid;
    }

    private String valueOrDash(String s) {
        if (s == null || s.trim().isEmpty()) {
            return "(none)";
        }
        return s.trim();
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