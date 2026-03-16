package cli.dashboard;

import controllers.dashboard.MainDashboardController;
import libraries.collections.MyString;
import utils.BackgroundFiller;
import utils.ConsoleColors;
import utils.ConsoleUtil;
import utils.FastInput;
import utils.TerminalUI;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

import static utils.TerminalUI.*;

public class MainDashboard {

    private final MainDashboardController controller = new MainDashboardController();
    private static final BackgroundFiller.Theme THEME = BackgroundFiller.MAIN;

    private static Terminal jlineTerminal = null;
    private static LineReader jlineReader = null;
    private static boolean jlineReady = false;

    private static final char PASSWORD_MASK = '\u2022';

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

    private static MyString readMaskedPassword() {
        initJLine();

        if (jlineReady && jlineReader != null) {
            try {
                String raw = jlineReader.readLine("", null, PASSWORD_MASK, null);
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

                boolean compact = termH() < 24;

                int menuStartRow;
                TerminalUI.MenuItem[] menuItems = {
                        new TerminalUI.MenuItem(1, "Student"),
                        new TerminalUI.MenuItem(2, "Attendant"),
                        new TerminalUI.MenuItem(3, "Maintenance Worker"),
                        new TerminalUI.MenuItem(4, "Store In Charge"),
                        new TerminalUI.MenuItem(5, "Hall Office"),
                        new TerminalUI.MenuItem(6, "Admin"),
                        new TerminalUI.MenuItem(7, "Cafeteria Manager"),
                        new TerminalUI.MenuItem(0, "Exit"),
                };

                if (compact) {
                    menuStartRow = 2;
                } else {
                    int afterBanner = drawBanner(2);
                    animateBannerShimmer(2, 1, 12);

                    String sub = "IUT Female Dormitory  ·  Islamic University of Technology";
                    Thread.sleep(80);
                    typewrite(afterBanner + 1, sub, ConsoleColors.Accent.MUTED, 8);

                    menuStartRow = afterBanner + 3;
                }

                int choice = readChoiceArrowScrollable(
                        "WELCOME TO IUT FEMALE DORMITORY",
                        "Select your role to continue",
                        menuItems,
                        THEME.text(),
                        THEME.box(),
                        null,
                        menuStartRow
                );

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

                int loginTopRow = compact
                        ? Math.max(2, centerRow(9) - 1)
                        : Math.max(8, centerRow(9));

                if (!compact) {
                    int afterBanner = drawBanner(2);
                    String sub = "IUT Female Dormitory  ·  Islamic University of Technology";
                    typewrite(afterBanner + 1, sub, ConsoleColors.Accent.MUTED, 0);
                    loginTopRow = afterBanner + 4;
                }

                drawLoginBox(loginTopRow, boxCol(), innerW(), THEME.panelBg(), THEME.inputBg());

                at(loginTopRow + 4, boxCol() + 14);
                System.out.print(THEME.inputBg() + THEME.text());
                System.out.flush();
                MyString username = new MyString(FastInput.readNonEmptyLine());

                at(loginTopRow + 6, boxCol() + 14);
                System.out.print(THEME.inputBg() + THEME.text());
                System.out.flush();
                MyString password = readMaskedPassword();

                System.out.print(RESET);
                at(loginTopRow + 10, 1);

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