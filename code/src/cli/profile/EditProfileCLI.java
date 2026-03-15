package cli.profile;

import controllers.profile.ProfileController;
import libraries.collections.MyString;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;
import utils.ConsoleColors;
import utils.FastInput;
import utils.TerminalUI;
import utils.TerminalUI.MenuItem;

import static utils.TerminalUI.*;

public class EditProfileCLI {

    private final ProfileController controller = new ProfileController();
    private final String username;
    private final String role;

    public EditProfileCLI(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public void start() {
        while (true) {
            try {
                fillBackground(getActiveBgColor());
                setActiveTheme(
                        getActiveBoxColor(),
                        getActiveTextColor(),
                        getActiveBgColor(),
                        getActivePanelBgColor(),
                        getActiveInputBgColor()
                );
                System.out.print(HIDE_CUR);

                MenuItem[] menu = {
                        new MenuItem(1, "Change Password"),
                        new MenuItem(2, "Change Phone Number"),
                        new MenuItem(0, "Back")
                };

                int menuStartRow = Math.max(3, centerRow(11) - 3);

                drawDashboard(
                        "EDIT PROFILE",
                        "Manage your account settings",
                        menu,
                        getActiveTextColor(),
                        getActiveBoxColor(),
                        null,
                        menuStartRow
                );

                int choice = readChoiceArrow();
                System.out.print(RESET);

                if (choice == 0) {
                    return;
                }

                if (choice == 1) {
                    changePasswordFlow();
                } else if (choice == 2) {
                    changePhoneFlow();
                } else {
                    fillBackground(getActiveBgColor());
                    at(2, 1);
                    tError("Invalid choice.");
                    tPause();
                }

            } catch (Exception e) {
                fillBackground(getActiveBgColor());
                at(2, 1);
                tError(e.getMessage());
                tPause();
                return;
            }
        }
    }

    private void changePasswordFlow() throws InterruptedException {
        fillBackground(getActiveBgColor());
        at(2, 1);

        int iw = innerW();
        int col = boxCol();
        int topRow = Math.max(3, centerRow(11) - 2);

        drawChangePasswordBox(topRow, col, iw, getActivePanelBgColor(), getActiveInputBgColor());

        String currentLabel = "Current Pass : ";
        String newLabel = "New Pass     : ";
        String confirmLabel = "Confirm Pass : ";

        int fieldCol = col + 2 + currentLabel.length();
        int fieldW = Math.max(10, iw - currentLabel.length() - 3);

        MyString oldPass = readMaskedField(topRow + 4, fieldCol, fieldW);
        MyString newPass = readMaskedField(topRow + 6, fieldCol, fieldW);
        MyString confirm = readMaskedField(topRow + 8, fieldCol, fieldW);

        String result = controller.changePassword(
                new MyString(username),
                new MyString(role),
                oldPass,
                newPass,
                confirm
        );

        fillBackground(getActiveBgColor());
        at(2, 1);

        if (result.toLowerCase().contains("success")) {
            tSuccess(result);
        } else {
            tError(result);
        }
        tPause();
    }

    private void changePhoneFlow() throws InterruptedException {
        fillBackground(getActiveBgColor());
        at(2, 1);

        int iw = innerW();
        int col = boxCol();
        int topRow = Math.max(3, centerRow(8) - 2);

        drawPhoneBox(topRow, col, iw, getActivePanelBgColor(), getActiveInputBgColor());

        String phoneLabel = "New Phone    : ";
        int fieldCol = col + 2 + phoneLabel.length();
        int fieldW = Math.max(10, iw - phoneLabel.length() - 3);

        MyString newPhone = readVisibleField(topRow + 4, fieldCol, fieldW);

        String result = controller.updatePhoneNumber(
                new MyString(username),
                new MyString(role),
                newPhone
        );

        fillBackground(getActiveBgColor());
        at(2, 1);

        if (result.toLowerCase().contains("success")) {
            tSuccess(result);
        } else {
            tError(result);
        }
        tPause();
    }

    private void drawChangePasswordBox(int topRow, int col, int iw,
                                       String panelBg, String inputBg)
            throws InterruptedException {

        String box = getActiveBoxColor() + panelBg;
        String text = getActiveTextColor() + panelBg;
        String inputLabel = ConsoleColors.Accent.INPUT + panelBg;
        String r = RESET;

        String currentLabel = "Current Pass : ";
        int fieldW = Math.max(10, iw - currentLabel.length() - 3);

        String[] rows = {
                box + "╔" + "═".repeat(iw) + "╗" + r,
                box + "║" + BOLD + text + padC("CHANGE PASSWORD", iw) + box + "║" + r,
                box + "╠" + "═".repeat(iw) + "╣" + r,
                box + "║" + panelBg + " ".repeat(iw) + box + "║" + r,
                box + "║ " + inputLabel + "Current Pass : "
                        + inputBg + getActiveTextColor() + " ".repeat(fieldW) + " "
                        + box + " ║" + r,
                box + "║" + panelBg + " ".repeat(iw) + box + "║" + r,
                box + "║ " + inputLabel + "New Pass     : "
                        + inputBg + getActiveTextColor() + " ".repeat(fieldW) + " "
                        + box + " ║" + r,
                box + "║" + panelBg + " ".repeat(iw) + box + "║" + r,
                box + "║ " + inputLabel + "Confirm Pass : "
                        + inputBg + getActiveTextColor() + " ".repeat(fieldW) + " "
                        + box + " ║" + r,
                box + "║" + panelBg + " ".repeat(iw) + box + "║" + r,
                box + "╚" + "═".repeat(iw) + "╝" + r,
        };

        for (int i = 0; i < rows.length; i++) {
            at(topRow + i, col);
            System.out.print(rows[i]);
            System.out.flush();
            Thread.sleep(10);
        }
    }

    private void drawPhoneBox(int topRow, int col, int iw,
                              String panelBg, String inputBg)
            throws InterruptedException {

        String box = getActiveBoxColor() + panelBg;
        String text = getActiveTextColor() + panelBg;
        String inputLabel = ConsoleColors.Accent.INPUT + panelBg;
        String r = RESET;

        String phoneLabel = "New Phone    : ";
        int fieldW = Math.max(10, iw - phoneLabel.length() - 3);

        String info = "Allowed: +8801XXXXXXXXX or 017XXXXXXXX";
        int infoPad = Math.max(0, iw - 2 - info.length());

        String[] rows = {
                box + "╔" + "═".repeat(iw) + "╗" + r,
                box + "║" + BOLD + text + padC("CHANGE PHONE NUMBER", iw) + box + "║" + r,
                box + "╠" + "═".repeat(iw) + "╣" + r,
                box + "║" + panelBg + " ".repeat(iw) + box + "║" + r,
                box + "║ " + inputLabel + "New Phone    : "
                        + inputBg + getActiveTextColor() + " ".repeat(fieldW) + " "
                        + box + "║" + r,
                box + "║" + panelBg + " ".repeat(iw) + box + "║" + r,
                box + "║ " + text + info + " ".repeat(infoPad) + box + "║" + r,
                box + "╚" + "═".repeat(iw) + "╝" + r,
        };

        for (int i = 0; i < rows.length; i++) {
            at(topRow + i, col);
            System.out.print(rows[i]);
            System.out.flush();
            Thread.sleep(10);
        }
    }

    private MyString readMaskedField(int row, int col, int maxLen) {
        Terminal terminal = TerminalUI.getJLineTerminal();

        if (terminal == null) {
            at(row, col);
            System.out.flush();
            String raw = FastInput.readNonEmptyLine();
            if (raw.length() > maxLen) {
                raw = raw.substring(0, maxLen);
            }
            return new MyString(raw);
        }

        StringBuilder sb = new StringBuilder();
        Attributes saved = terminal.enterRawMode();
        NonBlockingReader reader = terminal.reader();

        try {
            at(row, col);
            System.out.print(getActiveInputBgColor() + getActiveTextColor() + " ".repeat(maxLen));
            at(row, col);
            System.out.flush();

            while (true) {
                int ch = reader.read();
                if (ch == -1) {
                    continue;
                }

                if (ch == 13 || ch == 10) {
                    break;
                }

                if (ch == 127 || ch == 8) {
                    if (sb.length() > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                        at(row, col);
                        System.out.print(getActiveInputBgColor() + ConsoleColors.FG_WHITE + "*".repeat(sb.length()));
                        System.out.print(getActiveInputBgColor() + " ".repeat(maxLen - sb.length()));
                        at(row, col + sb.length());
                        System.out.flush();
                    }
                    continue;
                }

                if (ch == 3) {
                    return new MyString("");
                }

                if (ch >= 32 && ch <= 126 && sb.length() < maxLen) {
                    sb.append((char) ch);
                    at(row, col);
                    System.out.print(getActiveInputBgColor() + ConsoleColors.FG_WHITE + "*".repeat(sb.length()));
                    System.out.print(getActiveInputBgColor() + " ".repeat(maxLen - sb.length()));
                    at(row, col + sb.length());
                    System.out.flush();
                }
            }
        } catch (Exception ignored) {
        } finally {
            terminal.setAttributes(saved);
            System.out.print(RESET);
            System.out.flush();
        }

        return new MyString(sb.toString());
    }

    private MyString readVisibleField(int row, int col, int maxLen) {
        Terminal terminal = TerminalUI.getJLineTerminal();

        if (terminal == null) {
            at(row, col);
            System.out.flush();
            String raw = FastInput.readNonEmptyLine();
            if (raw.length() > maxLen) {
                raw = raw.substring(0, maxLen);
            }
            return new MyString(raw);
        }

        StringBuilder sb = new StringBuilder();
        Attributes saved = terminal.enterRawMode();
        NonBlockingReader reader = terminal.reader();

        try {
            at(row, col);
            System.out.print(getActiveInputBgColor() + getActiveTextColor() + " ".repeat(maxLen));
            at(row, col);
            System.out.flush();

            while (true) {
                int ch = reader.read();
                if (ch == -1) {
                    continue;
                }

                if (ch == 13 || ch == 10) {
                    break;
                }

                if (ch == 127 || ch == 8) {
                    if (sb.length() > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                        at(row, col);
                        System.out.print(getActiveInputBgColor() + getActiveTextColor() + sb);
                        System.out.print(getActiveInputBgColor() + " ".repeat(maxLen - sb.length()));
                        at(row, col + sb.length());
                        System.out.flush();
                    }
                    continue;
                }

                if (ch == 3) {
                    return new MyString("");
                }

                if (ch >= 32 && ch <= 126 && sb.length() < maxLen) {
                    sb.append((char) ch);
                    at(row, col);
                    System.out.print(getActiveInputBgColor() + getActiveTextColor() + sb);
                    System.out.print(getActiveInputBgColor() + " ".repeat(maxLen - sb.length()));
                    at(row, col + sb.length());
                    System.out.flush();
                }
            }
        } catch (Exception ignored) {
        } finally {
            terminal.setAttributes(saved);
            System.out.print(RESET);
            System.out.flush();
        }

        return new MyString(sb.toString());
    }
}
