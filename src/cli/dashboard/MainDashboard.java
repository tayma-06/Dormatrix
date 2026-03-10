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

    // ── Theme constants ───────────────────────────────────────────
    private static final String BOX = ConsoleColors.Accent.BOX;
    private static final String THEME = ConsoleColors.ThemeText.SOFT_WHITE;
    private static final String BG = ConsoleColors.bgRGB(20, 14, 32);
    private static final String INPUT = ConsoleColors.Accent.INPUT;
    private static final String EXIT = ConsoleColors.Accent.EXIT;
    private static final String MUTED = ConsoleColors.Accent.MUTED;

    // ── JLine (initialised once, shared with TerminalUI) ─────────
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
            TerminalUI.setJLineTerminal(jlineTerminal);   // share with TerminalUI
        } catch (IOException e) {
            jlineReady = false;
        }
    }

    // ── Masked password reader ────────────────────────────────────
    private static final char PASSWORD_MASK = '\u2022';   // •

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

    // ── Main show loop ────────────────────────────────────────────
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
                // ── Phase 1: intro animations (first visit only) ──
                if (firstRun) {
                    firstRun = false;
                    quickDormRain();
                    quickMatrixRain();
                }

                // ── Phase 2: theme + canvas ───────────────────────
                BackgroundFiller.applyMainMenuTheme();
                setActiveTheme(BOX, THEME, BG);
                System.out.print(HIDE_CUR);

                // ── Phase 3: animated banner ──────────────────────
                int afterBanner = drawBanner(2);

                // ── Phase 4: subtitle typewriter ──────────────────
                String sub = "IUT Female Dormitory  \u00b7  Islamic University of Technology";
                Thread.sleep(100);
                typewrite(afterBanner + 1, sub, MUTED, 12);

                // ── Phase 5: animated menu box ────────────────────
                int menuStartRow = afterBanner + 3;
                MenuItem[] menuItems = {
                    new MenuItem(1, "Student"),
                    new MenuItem(2, "Attendant"),
                    new MenuItem(3, "Maintenance Worker"),
                    new MenuItem(4, "Store-in-Charge"),
                    new MenuItem(5, "Hall Office"),
                    new MenuItem(6, "Admin"),
                    new MenuItem(7, "Cafeteria Manager"),
                    new MenuItem(0, "Exit"),};

                drawDashboard(
                        "WELCOME TO IUT FEMALE DORMITORY",
                        "Select your role to continue",
                        menuItems, THEME, BOX, null, menuStartRow
                );

                // ── Phase 6: arrow-key selection ──────────────────
                int choice = readChoiceArrow();   // enters/exits raw mode internally
                System.out.print(RESET);

                // ── Handle exit ───────────────────────────────────
                if (choice == 0) {
                    TerminalUI.goodbyeRain();
                    ConsoleUtil.clearAndReset();
                    System.exit(0);
                }

                // ── Phase 7: login panel ──────────────────────────
                BackgroundFiller.applyMainMenuTheme();
                drawBanner(2);
                typewrite(afterBanner + 1, sub, MUTED, 0);

                int mid = afterBanner + 4;
                int col = boxCol();
                int iw = innerW();
                String panelBg = ConsoleColors.bgRGB(16, 11, 30);
                String inputBg = ConsoleColors.bgRGB(22, 16, 38);

                drawLoginBox(mid, col, iw, panelBg, inputBg);

                // ── Read username ─────────────────────────────────
                at(mid + 4, col + 14);
                System.out.print(inputBg + THEME);
                System.out.flush();
                MyString username = new MyString(FastInput.readNonEmptyLine());

                // ── Read password (masked) ────────────────────────
                at(mid + 6, col + 14);
                System.out.print(inputBg + THEME);
                System.out.flush();
                MyString password = readMaskedPassword();

                System.out.print(RESET);
                at(mid + 10, 1);

                TerminalUI.setActiveTheme(BOX, THEME, panelBg);
                controller.handleRoleInput(choice, username, password);

            } catch (Exception e) {
                cleanup();
                System.err.println("[MainDashboard] " + e.getMessage());
            }
        }
    }

    // ── Login credentials box ─────────────────────────────────────
    private static void drawLoginBox(int topRow, int col, int iw,
            String panelBg, String inputBg)
            throws InterruptedException {

        String b = BOX + panelBg;
        String t = THEME + panelBg;
        String r = RESET;
        int fieldW = Math.max(10, iw - 14);

        String[][] rows = {
            {b + "\u2554" + "\u2550".repeat(iw) + "\u2557" + r},
            {b + "\u2551" + r + BOLD + t + padC("LOGIN CREDENTIALS", iw) + r + b + "\u2551" + r},
            {b + "\u2560" + "\u2550".repeat(iw) + "\u2563" + r},
            {b + "\u2551" + panelBg + " ".repeat(iw) + b + "\u2551" + r},
            {b + "\u2551 " + r + INPUT + panelBg + "User ID   : " + r
                + inputBg + THEME + " ".repeat(fieldW) + " " + r + b + "\u2551" + r},
            {b + "\u2551" + panelBg + " ".repeat(iw) + b + "\u2551" + r},
            {b + "\u2551 " + r + INPUT + panelBg + "Password  : " + r
                + inputBg + THEME + " ".repeat(fieldW) + " " + r + b + "\u2551" + r},
            {b + "\u2551" + panelBg + " ".repeat(iw) + b + "\u2551" + r},
            {b + "\u255a" + "\u2550".repeat(iw) + "\u255d" + r},};

        for (int i = 0; i < rows.length; i++) {
            at(topRow + i, col);
            System.out.print(rows[i][0]);
            System.out.flush();
            Thread.sleep(14);
        }
    }

    private static String padC(String s, int w) {
        int p = Math.max(0, w - s.length());
        return " ".repeat(p / 2) + s + " ".repeat(p - p / 2);
    }
}
