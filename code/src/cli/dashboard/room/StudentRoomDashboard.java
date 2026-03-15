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
        UP, DOWN, ENTER, ZERO, NONE
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
            } else if (key == NavKey.ENTER || key == NavKey.ZERO) {
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
            } else if (key == NavKey.ENTER || key == NavKey.ZERO) {
                TerminalUI.cleanup();
                return;
            }
        }
    }

    private void renderComplaintPreview(String roomNumber, MyArrayList<Complaint> list, int selected) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        System.out.print(TerminalUI.HIDE_CUR);

        int termW = TerminalUI.termW();
        int leftCol = Math.max(3, termW / 14);
        int topRow = 5;
        int leftW = Math.min(44, Math.max(36, termW / 3));
        int rightCol = leftCol + leftW + 3;
        int rightW = Math.max(44, termW - rightCol - leftCol);

        drawPanel(topRow, leftCol, leftW, 17, "ROOM COMPLAINTS");
        drawPanel(topRow, rightCol, rightW, 18, "COMPLAINT DETAILS");

        put(topRow + 2, leftCol + 2,
                ConsoleColors.ThemeText.SOFT_WHITE
                        + "Room " + roomNumber + "   |   Total: " + list.size()
                        + TerminalUI.RESET);

        int start = Math.max(0, selected - 4);
        int end = Math.min(list.size(), start + 10);
        if (end - start < 10) {
            start = Math.max(0, end - 10);
        }

        int row = topRow + 4;
        for (int i = start; i < end; i++) {
            Complaint c = list.get(i);
            boolean isSelected = i == selected;

            String line = String.format("%-14s %-12s",
                    c.getComplaintId(),
                    c.getStatus().name());

            String fg = isSelected ? ConsoleColors.FG_BLACK : ConsoleColors.ThemeText.SOFT_WHITE;
            String bg = isSelected ? ConsoleColors.bgRGB(210, 195, 245) : TerminalUI.getActivePanelBgColor();

            put(row++, leftCol + 2, bg + fg + pad(line, leftW - 4) + TerminalUI.RESET);
        }

        Complaint c = list.get(selected);

        put(topRow + 2, rightCol + 2, kv("Complaint ID", c.getComplaintId()));
        put(topRow + 3, rightCol + 2, kv("Status", c.getStatus().name()));
        put(topRow + 4, rightCol + 2, kv("Category", c.getCategory().name()));
        put(topRow + 5, rightCol + 2, kv("Priority", c.getPriority().name()));

        String worker = c.getAssignedWorkerId();
        if (worker == null || worker.trim().isEmpty()) {
            worker = "(none)";
        }
        put(topRow + 6, rightCol + 2, kv("Worker", worker));

        put(topRow + 8, rightCol + 2,
                ConsoleColors.ThemeText.SOFT_WHITE + "Description" + TerminalUI.RESET);

        String[] desc = wrap(c.getDescription(), Math.max(20, rightW - 6));
        for (int i = 0; i < Math.min(desc.length, 5); i++) {
            put(topRow + 9 + i, rightCol + 2,
                    ConsoleColors.FG_BRIGHT_WHITE + desc[i] + TerminalUI.RESET);
        }

        put(topRow + 17, leftCol,
                ConsoleColors.Accent.MUTED
                        + "Up/Down browse   Enter/0 return"
                        + TerminalUI.RESET);

        System.out.flush();
    }

    private void renderApplicationPreview(MyArrayList<RoomChangeApplication> list, int selected) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        System.out.print(TerminalUI.HIDE_CUR);

        int termW = TerminalUI.termW();
        int leftCol = Math.max(3, termW / 14);
        int topRow = 5;
        int leftW = Math.min(46, Math.max(38, termW / 3));
        int rightCol = leftCol + leftW + 3;
        int rightW = Math.max(44, termW - rightCol - leftCol);

        drawPanel(topRow, leftCol, leftW, 17, "MY ROOM CHANGE APPLICATIONS");
        drawPanel(topRow, rightCol, rightW, 19, "APPLICATION DETAILS");

        put(topRow + 2, leftCol + 2,
                ConsoleColors.ThemeText.SOFT_WHITE
                        + "Applications: " + list.size()
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
                    app.getStatus().name());

            String fg = isSelected ? ConsoleColors.FG_BLACK : ConsoleColors.ThemeText.SOFT_WHITE;
            String bg = isSelected ? ConsoleColors.bgRGB(210, 195, 245) : TerminalUI.getActivePanelBgColor();

            put(row++, leftCol + 2, bg + fg + pad(line, leftW - 4) + TerminalUI.RESET);
        }

        RoomChangeApplication app = list.get(selected);
        put(topRow + 2, rightCol + 2, kv("Application ID", app.getApplicationId()));
        put(topRow + 3, rightCol + 2, kv("Current Room", app.getCurrentRoom()));
        put(topRow + 4, rightCol + 2, kv("Requested Room", app.getRequestedRoom()));
        put(topRow + 5, rightCol + 2, kv("Status", app.getStatus().name()));
        put(topRow + 6, rightCol + 2, kv("Submitted At", app.getSubmittedAt()));

        String reviewedBy = app.getReviewedBy();
        if (reviewedBy == null || reviewedBy.trim().isEmpty()) reviewedBy = "(not reviewed)";
        put(topRow + 7, rightCol + 2, kv("Reviewed By", reviewedBy));

        String reviewedAt = app.getReviewedAt();
        if (reviewedAt == null || reviewedAt.trim().isEmpty()) reviewedAt = "(not reviewed)";
        put(topRow + 8, rightCol + 2, kv("Reviewed At", reviewedAt));

        put(topRow + 10, rightCol + 2,
                ConsoleColors.ThemeText.SOFT_WHITE + "Reason" + TerminalUI.RESET);
        String[] reason = wrap(app.getReason(), Math.max(20, rightW - 6));
        for (int i = 0; i < Math.min(reason.length, 3); i++) {
            put(topRow + 11 + i, rightCol + 2,
                    ConsoleColors.FG_BRIGHT_WHITE + reason[i] + TerminalUI.RESET);
        }

        put(topRow + 15, rightCol + 2,
                ConsoleColors.ThemeText.SOFT_WHITE + "Review Note" + TerminalUI.RESET);
        String review = app.getReviewNote();
        if (review == null || review.trim().isEmpty()) review = "(none)";
        String[] note = wrap(review, Math.max(20, rightW - 6));
        for (int i = 0; i < Math.min(note.length, 3); i++) {
            put(topRow + 16 + i, rightCol + 2,
                    ConsoleColors.FG_BRIGHT_WHITE + note[i] + TerminalUI.RESET);
        }

        put(topRow + 18, leftCol,
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