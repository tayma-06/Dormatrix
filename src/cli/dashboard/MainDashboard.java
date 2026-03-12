package cli.dashboard;

import controllers.dashboard.MainDashboardController;
import libraries.collections.MyString;
import utils.*;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.EndOfFileException;

import java.io.IOException;

import static utils.TerminalUI.*;

public class MainDashboard {

    private final MainDashboardController controller = new MainDashboardController();
    private static final BackgroundFiller.Theme THEME = BackgroundFiller.MAIN;

    private static Terminal jlineTerminal = null;
    private static LineReader jlineReader = null;
    private static boolean jlineReady = false;

    private static void initJLine() {
        if (jlineTerminal != null) {
            return;
        }
        java.util.logging.Logger.getLogger("org.jline").setLevel(java.util.logging.Level.OFF);
        try {
            jlineTerminal = TerminalBuilder.builder().system(true).build();
            jlineReader = LineReaderBuilder.builder().terminal(jlineTerminal).build();
            jlineReady = true;
            TerminalUI.setJLineTerminal(jlineTerminal);
        } catch (IOException e) {
            jlineReady = false;
        }
    }

    private static final char PASSWORD_MASK = '\u2022';

    private static MyString readMaskedPassword() {
        initJLine();
        if (jlineReady && jlineReader != null) {
            try {
                String raw = jlineReader.readLine("", null, (Character) PASSWORD_MASK, null);
                return new MyString(raw == null ? "" : raw);
            } catch (UserInterruptException | EndOfFileException e) {
                return new MyString("");
            }
        }
        java.io.Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword();
            return new MyString(chars == null ? "" : new String(chars));
        }
        return new MyString(FastInput.readNonEmptyLine());
    }

    public void show() {
        initJLine();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            TerminalUI.cleanup();
            if (jlineTerminal != null) {
                try {
                    jlineTerminal.close();
                } catch (IOException ignored) {
                }
            }
        }));

        boolean firstRun = true;

        while (true) {
            try {
                if (firstRun) {
                    firstRun = false;
                    quickDormRain();
                    quickMatrixRain();
                }

                BackgroundFiller.applyTheme(THEME);
                setActiveTheme(
                        THEME.box(),
                        THEME.text(),
                        THEME.canvasBg(),
                        THEME.panelBg(),
                        THEME.inputBg()
                );
                System.out.print(HIDE_CUR);

                int afterBanner = drawBanner(2);

                String sub = "IUT Female Dormitory  ·  Islamic University of Technology";
                Thread.sleep(100);
                typewrite(afterBanner + 1, sub, ConsoleColors.Accent.MUTED, 12);

                int menuStartRow = afterBanner + 3;
                MenuItem[] menuItems = {
                        new MenuItem(1, "Student"),
                        new MenuItem(2, "Attendant"),
                        new MenuItem(3, "Maintenance Worker"),
                        new MenuItem(4, "Store-in-Charge"),
                        new MenuItem(5, "Hall Office"),
                        new MenuItem(6, "Admin"),
                        new MenuItem(7, "Cafeteria Manager"),
                        new MenuItem(0, "Exit"),
                };

                drawDashboard(
                        "WELCOME TO IUT FEMALE DORMITORY",
                        "Select your role to continue",
                        menuItems,
                        THEME.text(),
                        THEME.box(),
                        null,
                        menuStartRow
                );

                int choice = readChoiceArrow();
                System.out.print(RESET);

                if (choice == 0) {
                    TerminalUI.goodbyeRain();
                    ConsoleUtil.clearAndReset();
                    System.exit(0);
                }

                BackgroundFiller.applyTheme(THEME);
                setActiveTheme(
                        THEME.box(),
                        THEME.text(),
                        THEME.canvasBg(),
                        THEME.panelBg(),
                        THEME.inputBg()
                );

                drawBanner(2);
                typewrite(afterBanner + 1, sub, ConsoleColors.Accent.MUTED, 0);

                int mid = afterBanner + 4;
                int col = boxCol();
                int iw = innerW();

                drawLoginBox(mid, col, iw, THEME.panelBg(), THEME.inputBg());

                at(mid + 4, col + 14);
                System.out.print(THEME.inputBg() + THEME.text());
                System.out.flush();
                MyString username = new MyString(FastInput.readNonEmptyLine());

                at(mid + 6, col + 14);
                System.out.print(THEME.inputBg() + THEME.text());
                System.out.flush();
                MyString password = readMaskedPassword();

                System.out.print(RESET);
                at(mid + 10, 1);

                TerminalUI.setActiveTheme(
                        THEME.box(),
                        THEME.text(),
                        THEME.canvasBg(),
                        THEME.panelBg(),
                        THEME.inputBg()
                );
                controller.handleRoleInput(choice, username, password);

            } catch (Exception e) {
                cleanup();
                System.err.println("[MainDashboard] " + e.getMessage());
            }
        }
    }

    private static void drawLoginBox(int topRow, int col, int iw,
                                     String panelBg, String inputBg)
            throws InterruptedException {

        String b = THEME.box() + panelBg;
        String t = THEME.text() + panelBg;
        String inputLabel = ConsoleColors.Accent.INPUT + panelBg;
        String r = RESET;
        int fieldW = Math.max(10, iw - 14);

        String[] rows = {
                b + "╔" + "═".repeat(iw) + "╗" + r,
                b + "║" + BOLD + t + padC("LOGIN CREDENTIALS", iw) + b + "║" + r,
                b + "╠" + "═".repeat(iw) + "╣" + r,
                b + "║" + panelBg + " ".repeat(iw) + b + "║" + r,
                b + "║ " + inputLabel + "User ID   : "
                        + inputBg + THEME.text() + " ".repeat(fieldW) + " "
                        + b + "║" + r,
                b + "║" + panelBg + " ".repeat(iw) + b + "║" + r,
                b + "║ " + inputLabel + "Password  : "
                        + inputBg + THEME.text() + " ".repeat(fieldW) + " "
                        + b + "║" + r,
                b + "║" + panelBg + " ".repeat(iw) + b + "║" + r,
                b + "╚" + "═".repeat(iw) + "╝" + r,
        };

        for (int i = 0; i < rows.length; i++) {
            at(topRow + i, col);
            System.out.print(rows[i]);
            System.out.flush();
            Thread.sleep(14);
        }
    }

    private static String padC(String s, int w) {
        int p = Math.max(0, w - s.length());
        return " ".repeat(p / 2) + s + " ".repeat(p - p / 2);
    }
}