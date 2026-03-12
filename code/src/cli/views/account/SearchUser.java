package cli.views.account;

import controllers.account.AccountRecordParser;
import controllers.account.SearchUserController;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

public class SearchUser {

    private final SearchUserController controller;

    private final int startRow = 4;
    private int inputRow;
    private int inputCol;

    public SearchUser(SearchUserController controller) {
        this.controller = controller;
    }

    public void show() {
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
                        + TerminalUI.padC("Press Enter to search again", iw)
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