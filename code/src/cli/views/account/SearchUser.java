package cli.views.account;

import controllers.account.AccountRecordParser;
import controllers.account.SearchUserController;
import controllers.account.SearchUserController.SearchHit;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import java.util.Collections;
import java.util.List;

public class SearchUser {

    private final SearchUserController controller;

    private final int startRow = 3;
    private int inputRow;
    private int inputCol;

    public SearchUser(SearchUserController controller) {
        this.controller = controller;
    }

    public void show() {
        Terminal terminal = TerminalUI.getJLineTerminal();
        if (terminal == null) {
            showLegacy();
            return;
        }
        showInteractive(terminal);
    }

    private void showInteractive(Terminal terminal) {
        StringBuilder query = new StringBuilder();
        int selected = 0;

        Attributes saved = terminal.enterRawMode();
        NonBlockingReader reader = terminal.reader();

        try {
            while (true) {
                List<SearchHit> hits = query.length() == 0
                        ? Collections.emptyList()
                        : controller.searchSuggestions(query.toString(), 8);

                if (selected >= hits.size()) {
                    selected = Math.max(0, hits.size() - 1);
                }

                drawLiveSearch(query.toString(), hits, selected);

                int ch = reader.read();
                if (ch == -1) {
                    continue;
                }

                if (ch == 3) { // Ctrl+C
                    return;
                }

                if (ch == 27) { // arrows
                    int n1 = reader.read(25);
                    if (n1 == '[' || n1 == 'O') {
                        int n2 = reader.read(25);
                        if (n2 == 'A' && !hits.isEmpty()) {
                            selected = (selected - 1 + hits.size()) % hits.size();
                        } else if (n2 == 'B' && !hits.isEmpty()) {
                            selected = (selected + 1) % hits.size();
                        }
                    }
                    continue;
                }

                if (ch == 9 && !hits.isEmpty()) { // Tab fills selected ID
                    query.setLength(0);
                    query.append(hits.get(selected).getId());
                    selected = 0;
                    continue;
                }

                if (ch == 13 || ch == 10) { // Enter
                    String typed = query.toString().trim();

                    if ("0".equals(typed)) {
                        return;
                    }

                    if (!hits.isEmpty()) {
                        SearchHit hit = hits.get(selected);
                        openHit(reader, hit);
                        continue;
                    }

                    if (!typed.isEmpty()) {
                        String raw = controller.searchById(typed);
                        if (raw != null) {
                            String formatted = AccountRecordParser.formatDetails(raw);
                            String[] lines = formatted == null
                                    ? new String[]{"User information not found."}
                                    : formatted.split("\\n");

                            drawResultPanel(typed, lines, "Account found.");
                        } else {
                            drawResultPanel(
                                    typed,
                                    new String[]{
                                            "ID         : " + typed,
                                            "Status     : Not Found"
                                    },
                                    "No account matched this ID."
                            );
                        }
                        waitRawKey(reader);
                    }
                    continue;
                }

                if (ch == 127 || ch == 8) { // backspace
                    if (query.length() > 0) {
                        query.deleteCharAt(query.length() - 1);
                        selected = 0;
                    }
                    continue;
                }

                if (ch >= 32 && ch <= 126) {
                    query.append((char) ch);
                    selected = 0;
                }
            }
        } catch (Exception ignored) {
        } finally {
            terminal.setAttributes(saved);
            System.out.print(TerminalUI.SHOW_CUR + TerminalUI.RESET);
            System.out.flush();
        }
    }

    private void openHit(NonBlockingReader reader, SearchHit hit) throws Exception {
        String formatted = AccountRecordParser.formatDetails(hit.getRawData());
        String[] lines = formatted == null
                ? new String[]{"User information not found."}
                : formatted.split("\\n");

        drawResultPanel(hit.getId(), lines, "Account found.");
        waitRawKey(reader);
    }

    private void waitRawKey(NonBlockingReader reader) throws Exception {
        while (true) {
            int c = reader.read();
            if (c != -1) {
                return;
            }
        }
    }

    private void drawLiveSearch(String query, List<SearchHit> hits, int selected) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());

        int screenW = Math.max(90, TerminalUI.termW() - 4);
        int leftW = Math.min(46, Math.max(38, screenW / 2 - 2));
        int rightW = Math.max(36, screenW - leftW - 3);

        int totalW = leftW + rightW + 3;
        int leftCol = TerminalUI.centerCol(totalW);
        int rightCol = leftCol + leftW + 3;
        int topRow = startRow;

        drawLeftPane(topRow, leftCol, leftW, query, hits, selected);
        drawRightPane(topRow, rightCol, rightW, query, hits, selected);

        System.out.flush();
    }

    private void drawLeftPane(int topRow, int col, int width, String query, List<SearchHit> hits, int selected) {
        int inner = width - 2;
        String box = TerminalUI.getActiveBoxColor();
        String panel = TerminalUI.getActivePanelBgColor();
        String inputBg = TerminalUI.getActiveInputBgColor();

        int resultRows = 10;
        int row = topRow;

        printRow(row++, col, box + panel + "╔" + "═".repeat(inner) + "╗" + TerminalUI.RESET);
        printRow(row++, col,
                box + panel + "║"
                        + TerminalUI.BOLD + TerminalUI.ACCENT + panel
                        + TerminalUI.padC("SEARCH ACCOUNTS", inner)
                        + box + panel + "║" + TerminalUI.RESET);
        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);
        printRow(row++, col,
                box + panel + "║ "
                        + TerminalUI.MUTED + panel
                        + fit("Type ID or name ", inner - 2)
                        + spaces(inner - 2 - fit("Type ID or name ", inner - 2).length())
                        + box + panel + " ║" + TerminalUI.RESET);

        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);

        String label = "Search : ";
        int fieldW = Math.max(1, inner - label.length() - 2);
        String shown = query.length() > fieldW ? query.substring(query.length() - fieldW) : query;

        printRow(row, col,
                box + panel + "║ "
                        + TerminalUI.ACCENT + panel + label
                        + inputBg + TerminalUI.getActiveTextColor() + shown
                        + spaces(fieldW - shown.length())
                        + box + panel + " ║" + TerminalUI.RESET);

        inputRow = row;
        inputCol = col + 2 + label.length();
        row++;

        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);

        for (int i = 0; i < resultRows; i++) {
            String line = "";
            boolean isSelected = false;

            if (i < hits.size()) {
                SearchHit hit = hits.get(i);
                line = hit.getId() + "  |  " + hit.getName() + "  |  " + hit.getRole();
                isSelected = i == selected;
            } else if (i == 0 && !query.isEmpty() && hits.isEmpty()) {
                line = "No matching account.";
            } else if (i == 0 && query.isEmpty()) {
                line = "Start typing to see suggestions.";
            }

            printSuggestionRow(row++, col, inner, line, isSelected, box, panel);
        }

        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);
        printRow(row++, col,
                box + panel + "║"
                        + TerminalUI.MUTED + panel
                        + TerminalUI.padC("Enter 0 and press Enter to go back", inner)
                        + box + panel + "║" + TerminalUI.RESET);
        printRow(row, col, box + panel + "╚" + "═".repeat(inner) + "╝" + TerminalUI.RESET);
    }

    private void drawRightPane(int topRow, int col, int width, String query, List<SearchHit> hits, int selected) {
        int inner = width - 2;
        String box = TerminalUI.getActiveBoxColor();
        String panel = TerminalUI.getActivePanelBgColor();

        int row = topRow;
        int bodyRows = 14;

        printRow(row++, col, box + panel + "╔" + "═".repeat(inner) + "╗" + TerminalUI.RESET);
        printRow(row++, col,
                box + panel + "║"
                        + TerminalUI.BOLD + TerminalUI.ACCENT + panel
                        + TerminalUI.padC("LIVE PREVIEW", inner)
                        + box + panel + "║" + TerminalUI.RESET);
        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);

        String[] previewLines;

        if (query.isEmpty()) {
            previewLines = new String[]{};
        } else if (hits.isEmpty()) {
            previewLines = new String[]{
                    "No live preview available.",
                    "",
                    "Nothing matches:",
                    "  " + query
            };
        } else {
            String formatted = AccountRecordParser.formatDetails(hits.get(selected).getRawData());
            previewLines = formatted == null
                    ? new String[]{"User information not found."}
                    : formatted.split("\\n");
        }

        for (int i = 0; i < bodyRows; i++) {
            String line = i < previewLines.length ? previewLines[i] : "";
            line = fit(line, inner - 2);

            printRow(row++, col,
                    box + panel + "║ "
                            + colorize(line)
                            + spaces(Math.max(0, inner - 2 - TerminalUI.stripAnsi(line).length()))
                            + box + panel + " ║" + TerminalUI.RESET);
        }

        printRow(row++, col, box + panel + "╠" + "═".repeat(inner) + "╣" + TerminalUI.RESET);
        printRow(row++, col,
                box + panel + "║"
                        + TerminalUI.MUTED + panel
                        + TerminalUI.padC("Selected result updates while you move", inner)
                        + box + panel + "║" + TerminalUI.RESET);
        printRow(row, col, box + panel + "╚" + "═".repeat(inner) + "╝" + TerminalUI.RESET);
    }

    private void printSuggestionRow(int row, int col, int inner, String text, boolean selected, String box, String panel) {
        String display = fit(text, inner - 2);

        if (selected) {
            String bg = "\u001B[48;2;185;165;220m";
            String fg = "\u001B[38;2;35;20;70m";

            printRow(row, col,
                    box + panel + "║ "
                            + bg + fg + display + spaces(inner - 2 - display.length())
                            + box + panel + " ║" + TerminalUI.RESET);
        } else {
            printRow(row, col,
                    box + panel + "║ "
                            + TerminalUI.getActiveTextColor() + panel
                            + display + spaces(inner - 2 - display.length())
                            + box + panel + " ║" + TerminalUI.RESET);
        }
    }

    private void printRow(int row, int col, String text) {
        TerminalUI.at(row, col);
        System.out.print(text);
    }

    private String fit(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        if (max <= 1) {
            return s.substring(0, max);
        }
        return s.substring(0, max - 1) + "…";
    }

    private String spaces(int n) {
        return " ".repeat(Math.max(0, n));
    }

    private void showLegacy() {
        while (true) {
            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());

            drawInputPanel();

            TerminalUI.setCooked();
            System.out.print(TerminalUI.SHOW_CUR);
            TerminalUI.at(inputRow, inputCol);
            System.out.flush();

            String id = FastInput.readLine().trim();

            if ("0".equals(id)) {
                return;
            }
            if (id.isEmpty()) {
                continue;
            }

            String raw = controller.searchById(id);

            if (raw == null) {
                drawResultPanel(
                        id,
                        new String[]{
                                "ID         : " + id,
                                "Status     : Not Found"
                        },
                        "No account matched this ID."
                );
            } else {
                String formatted = AccountRecordParser.formatDetails(raw);
                String[] lines = formatted == null
                        ? new String[]{"User information not found."}
                        : formatted.split("\\n");

                drawResultPanel(
                        id,
                        lines,
                        "Account found."
                );
            }

            silentWait();
        }
    }

    private void drawInputPanel() {
        int col = TerminalUI.boxCol();
        int iw = TerminalUI.innerW();
        int row = startRow;
        String box = TerminalUI.getActiveBoxColor();

        String helper = "Search the accounts";
        String footer = "Type an ID and press Enter   |   Type 0 to go back";
        String label = "User ID : ";

        TerminalUI.at(row++, col);
        System.out.print(box + "╔" + "═".repeat(iw) + "╗" + TerminalUI.RESET);

        TerminalUI.at(row++, col);
        System.out.print(
                box + "║" + TerminalUI.RESET
                        + TerminalUI.BOLD + TerminalUI.ACCENT
                        + TerminalUI.padC("SEARCH ACCOUNTS", iw)
                        + TerminalUI.RESET + box + "║" + TerminalUI.RESET
        );

        TerminalUI.at(row++, col);
        System.out.print(box + "╠" + "═".repeat(iw) + "╣" + TerminalUI.RESET);

        TerminalUI.at(row++, col);
        System.out.print(
                box + "║" + TerminalUI.RESET
                        + TerminalUI.MUTED
                        + TerminalUI.padC(helper, iw)
                        + TerminalUI.RESET + box + "║" + TerminalUI.RESET
        );

        TerminalUI.at(row++, col);
        System.out.print(box + "╠" + "═".repeat(iw) + "╣" + TerminalUI.RESET);

        inputRow = row;
        int fieldWidth = Math.max(1, iw - 1 - visibleLength(label));

        TerminalUI.at(row, col);
        System.out.print(
                box + "║ " + TerminalUI.RESET
                        + TerminalUI.ACCENT + label + TerminalUI.RESET
                        + " ".repeat(fieldWidth)
                        + box + "║" + TerminalUI.RESET
        );

        inputCol = col + 2 + visibleLength(label);
        row++;

        TerminalUI.at(row++, col);
        System.out.print(box + "╠" + "═".repeat(iw) + "╣" + TerminalUI.RESET);

        TerminalUI.at(row++, col);
        System.out.print(
                box + "║" + TerminalUI.RESET
                        + TerminalUI.MUTED
                        + TerminalUI.padC(footer, iw)
                        + TerminalUI.RESET + box + "║" + TerminalUI.RESET
        );

        TerminalUI.at(row, col);
        System.out.print(box + "╚" + "═".repeat(iw) + "╝" + TerminalUI.RESET);

        System.out.flush();
    }

    private void drawResultPanel(String searchedId, String[] lines, String status) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());

        int col = TerminalUI.boxCol();
        int iw = TerminalUI.innerW();
        int row = startRow;
        String box = TerminalUI.getActiveBoxColor();

        int bodyRows = Math.max(7, lines.length);

        TerminalUI.at(row++, col);
        System.out.print(box + "╔" + "═".repeat(iw) + "╗" + TerminalUI.RESET);

        TerminalUI.at(row++, col);
        System.out.print(
                box + "║" + TerminalUI.RESET
                        + TerminalUI.BOLD + TerminalUI.ACCENT
                        + TerminalUI.padC("SEARCH RESULT", iw)
                        + TerminalUI.RESET + box + "║" + TerminalUI.RESET
        );

        TerminalUI.at(row++, col);
        System.out.print(box + "╠" + "═".repeat(iw) + "╣" + TerminalUI.RESET);

        String statusLine = TerminalUI.truncate(status, iw - 2);
        int statusLen = TerminalUI.stripAnsi(statusLine).length();

        TerminalUI.at(row++, col);
        System.out.print(
                box + "║ " + TerminalUI.RESET
                        + TerminalUI.MUTED + statusLine + TerminalUI.RESET
                        + " ".repeat(Math.max(0, iw - 1 - statusLen))
                        + box + "║" + TerminalUI.RESET
        );

        for (int i = 0; i < bodyRows; i++) {
            String line = i < lines.length ? TerminalUI.truncate(lines[i], iw - 2) : "";
            int visibleLen = TerminalUI.stripAnsi(line).length();

            TerminalUI.at(row++, col);
            System.out.print(
                    box + "║ " + TerminalUI.RESET
                            + colorize(line)
                            + " ".repeat(Math.max(0, iw - 1 - visibleLen))
                            + box + "║" + TerminalUI.RESET
            );
        }

        TerminalUI.at(row++, col);
        System.out.print(box + "╠" + "═".repeat(iw) + "╣" + TerminalUI.RESET);

        TerminalUI.at(row++, col);
        System.out.print(
                box + "║" + TerminalUI.RESET
                        + TerminalUI.MUTED
                        + TerminalUI.padC("Press any key to return", iw)
                        + TerminalUI.RESET + box + "║" + TerminalUI.RESET
        );

        TerminalUI.at(row, col);
        System.out.print(box + "╚" + "═".repeat(iw) + "╝" + TerminalUI.RESET);

        System.out.flush();
    }

    private void silentWait() {
        TerminalUI.setCooked();
        System.out.print(TerminalUI.SHOW_CUR);
        TerminalUI.at(TerminalUI.termH(), 1);
        System.out.flush();
        FastInput.readLine();
    }

    private int visibleLength(String s) {
        return TerminalUI.stripAnsi(s).length();
    }

    private String colorize(String line) {
        if (!line.contains(" : ")) {
            return TerminalUI.getActiveTextColor() + line + TerminalUI.RESET;
        }

        String[] split = line.split(" : ", 2);
        return TerminalUI.ACCENT + split[0] + " : " + TerminalUI.RESET
                + TerminalUI.getActiveTextColor() + split[1] + TerminalUI.RESET;
    }
}