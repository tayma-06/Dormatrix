package cli.views.account;

import controllers.account.ViewAccountController;
import controllers.account.ViewAccountController.AccountSummary;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import java.util.ArrayList;
import java.util.List;

import static utils.TerminalUI.*;

public class ViewAccount {

    private static final MenuItem[] ROLE_MENU = {
            new MenuItem(1, "Student"),
            new MenuItem(2, "Hall Attendant"),
            new MenuItem(3, "Maintenance Worker"),
            new MenuItem(4, "Store-in-Charge"),
            new MenuItem(5, "Hall Officer"),
            new MenuItem(6, "Admin"),
            new MenuItem(7, "Cafeteria Manager"),
            new MenuItem(8, "View All Accounts"),
            new MenuItem(0, "Back")
    };

    private final ViewAccountController controller;

    public ViewAccount(ViewAccountController controller) {
        this.controller = controller;
    }

    public void show() {
        while (true) {
            try {
                ConsoleUtil.clearScreen();
                TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
                System.out.print(HIDE_CUR);

                drawDashboard(
                        "VIEW ACCOUNTS",
                        "Choose a role, then choose a user",
                        ROLE_MENU,
                        TerminalUI.getActiveTextColor(),
                        TerminalUI.getActiveBoxColor(),
                        new String[]{""},
                        3
                );

                int roleChoice = readChoiceArrow();
                System.out.print(RESET);

                if (roleChoice == 0) {
                    ConsoleUtil.clearScreen();
                    return;
                }

                List<AccountSummary> accounts = controller.getAccountsByChoice(roleChoice);
                if (accounts.isEmpty()) {
                    showMessage("No accounts found for this selection.");
                    continue;
                }

                showAccountPicker(accounts);

            } catch (Exception e) {
                cleanup();
                System.err.println("[ViewAccount] " + e.getMessage());
                return;
            }
        }
    }

    private void showAccountPicker(List<AccountSummary> accounts) throws Exception {
        int page = 0;
        final int pageSize = 9;

        while (true) {
            int start = page * pageSize;
            int end = Math.min(start + pageSize, accounts.size());
            List<AccountSummary> pageItems = new ArrayList<>(accounts.subList(start, end));

            List<MenuItem> menu = new ArrayList<>();
            int menuNumber = 1;

            for (AccountSummary item : pageItems) {
                menu.add(new MenuItem(
                        menuNumber++,
                        item.getId() + "  |  " + item.getName() + "  |  " + item.getRole()
                ));
            }

            if (end < accounts.size()) {
                menu.add(new MenuItem(98, "Next Page"));
            }
            if (page > 0) {
                menu.add(new MenuItem(99, "Previous Page"));
            }
            menu.add(new MenuItem(0, "Back"));

            ConsoleUtil.clearScreen();
            TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
            System.out.print(HIDE_CUR);

            drawDashboard(
                    "ACCOUNT LIST",
                    "Total: " + accounts.size() + " account(s)   |   Page " + (page + 1),
                    menu.toArray(new MenuItem[0]),
                    TerminalUI.getActiveTextColor(),
                    TerminalUI.getActiveBoxColor(),
                    new String[]{"Open a user to view full information."},
                    3
            );

            int picked = readChoiceArrow();
            System.out.print(RESET);

            if (picked == 0) {
                return;
            }
            if (picked == 98 && end < accounts.size()) {
                page++;
                continue;
            }
            if (picked == 99 && page > 0) {
                page--;
                continue;
            }
            if (picked >= 1 && picked <= pageItems.size()) {
                showDetails(pageItems.get(picked - 1).getRawData());
            }
        }
    }

    private void showDetails(String rawData) {
        String details = controller.formatAccountDetails(rawData);
        String[] lines = details == null
                ? new String[]{"User information not found."}
                : details.split("\\n");

        int extraHeight = Math.max(7, lines.length);

        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        System.out.print(HIDE_CUR);

        int col = TerminalUI.boxCol();
        int iw = TerminalUI.innerW();
        int row = 4;
        String box = TerminalUI.getActiveBoxColor();

        at(row++, col);
        System.out.print(box + "╔" + "═".repeat(iw) + "╗" + RESET);

        at(row++, col);
        System.out.print(
                box + "║" + RESET
                        + BOLD + ACCENT + padC("ACCOUNT DETAILS", iw)
                        + RESET + box + "║" + RESET
        );

        at(row++, col);
        System.out.print(box + "╠" + "═".repeat(iw) + "╣" + RESET);

        for (int i = 0; i < extraHeight; i++) {
            String line = i < lines.length ? TerminalUI.truncate(lines[i], iw - 2) : "";
            int visibleLen = TerminalUI.stripAnsi(line).length();

            at(row++, col);
            System.out.print(
                    box + "║ " + RESET
                            + colorize(line)
                            + " ".repeat(Math.max(0, iw - 2 - visibleLen))
                            + box + " ║" + RESET
            );
        }

        at(row++, col);
        System.out.print(box + "╠" + "═".repeat(iw) + "╣" + RESET);

        at(row++, col);
        System.out.print(
                box + "║" + RESET
                        + MUTED + padC("Press Enter to return to the account list", iw)
                        + RESET + box + "║" + RESET
        );

        at(row, col);
        System.out.print(box + "╚" + "═".repeat(iw) + "╝" + RESET);
        System.out.flush();

        silentWait();
    }

    private void showMessage(String message) {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());

        int col = TerminalUI.boxCol();
        int iw = TerminalUI.innerW();
        int row = 6;
        String box = TerminalUI.getActiveBoxColor();

        at(row++, col);
        System.out.print(box + "╔" + "═".repeat(iw) + "╗" + RESET);

        at(row++, col);
        System.out.print(
                box + "║" + RESET
                        + BOLD + ACCENT + padC("NOTICE", iw)
                        + RESET + box + "║" + RESET
        );

        at(row++, col);
        System.out.print(box + "╠" + "═".repeat(iw) + "╣" + RESET);

        at(row++, col);
        System.out.print(
                box + "║" + RESET
                        + ERROR + padC(message, iw)
                        + RESET + box + "║" + RESET
        );

        at(row++, col);
        System.out.print(box + "╠" + "═".repeat(iw) + "╣" + RESET);

        at(row++, col);
        System.out.print(
                box + "║" + RESET
                        + MUTED + padC("Press Enter to go back", iw)
                        + RESET + box + "║" + RESET
        );

        at(row, col);
        System.out.print(box + "╚" + "═".repeat(iw) + "╝" + RESET);
        System.out.flush();

        silentWait();
    }

    private void silentWait() {
        TerminalUI.setCooked();
        System.out.print(SHOW_CUR);
        TerminalUI.at(TerminalUI.termH(), 1);
        System.out.flush();
        FastInput.readLine();
    }

    private String colorize(String line) {
        if (!line.contains(" : ")) {
            return TerminalUI.getActiveTextColor() + line + RESET;
        }

        String[] split = line.split(" : ", 2);
        return ACCENT + split[0] + " : " + RESET
                + TerminalUI.getActiveTextColor() + split[1] + RESET;
    }
}