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

    private static final String BOX = ConsoleColors.Accent.BOX;
    private static final String THEME = ConsoleColors.ThemeText.SOFT_WHITE;
    private static final String INPUT = ConsoleColors.Accent.INPUT;
    private static final String EXIT = ConsoleColors.Accent.EXIT;
    private static final String MUTED = ConsoleColors.Accent.MUTED;

    // ── JLine terminal (shared, initialised once) ─────────────────
    private static Terminal jlineTerminal = null;
    private static LineReader jlineReader = null;
    private static boolean jlineReady = false;

    private static void initJLine() {
        if (jlineTerminal != null) {
            return;
        }
        java.util.logging.Logger.getLogger("org.jline").setLevel(java.util.logging.Level.OFF);
        try {
            Terminal t = TerminalBuilder.builder().system(true).build();
            if (org.jline.terminal.Terminal.TYPE_DUMB.equals(t.getType())
                    || org.jline.terminal.Terminal.TYPE_DUMB_COLOR.equals(t.getType())) {
                t.close();
                jlineReady = false;
                return;
            }
            jlineTerminal = t;
            jlineReader = LineReaderBuilder.builder()
                    .terminal(jlineTerminal)
                    .build();
            jlineReady = true;
        } catch (IOException | IllegalStateException e) {
            jlineReady = false;
        }
    }

    // Change this to '·' (alt+0183) if you prefer dot masking instead of asterisks
    private static final char PASSWORD_MASK = '•';

    /**
     * Reads a password at the current cursor position. Each typed character is
     * echoed as PASSWORD_MASK ('*' or '·'). The caller is responsible for
     * positioning the cursor with at() first. Falls back to System.console()
     * then plain FastInput if JLine is unavailable.
     */
    private static MyString readMaskedPassword() {
        initJLine();

        if (jlineReady && jlineReader != null) {
            try {
                // Correct overload: readLine(prompt, rightPrompt, mask, buffer)
                // Empty prompt — caller already positioned cursor with at().
                // JLine echoes PASSWORD_MASK for every character typed.
                String raw = jlineReader.readLine("", null, (Character) PASSWORD_MASK, null);
                return new MyString(raw == null ? "" : raw);
            } catch (UserInterruptException | EndOfFileException e) {
                return new MyString("");
            }
        }

        // Fallback 1: System.console() — input is silent/invisible
        java.io.Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword();
            return new MyString(chars == null ? "" : new String(chars));
        }

        // Fallback 2: plain read — input is visible (last resort)
        return new MyString(FastInput.readNonEmptyLine());
    }

    // ── Role select menu ─────────────────────────────────────────
    private static final MenuItem[] MENU = {
        new MenuItem(1, "Student"),
        new MenuItem(2, "Attendant"),
        new MenuItem(3, "Maintenance Worker"),
        new MenuItem(4, "Store-in-Charge"),
        new MenuItem(5, "Hall Office"),
        new MenuItem(6, "Admin"),
        new MenuItem(7, "Cafeteria Manager"),
        new MenuItem(0, "Exit"),};

    public void show() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            TerminalUI.cleanup();
            // Close JLine terminal cleanly on shutdown
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

                // ── Phase 1: matrix rain (first visit only) ───────
                if (firstRun) {
                    firstRun = false;
                    quickDormRain();
                    quickMatrixRain();
                }

                // ── Phase 2: apply theme + fill canvas ────────────
                BackgroundFiller.applyMainMenuTheme();
                System.out.print(HIDE_CUR);

                // ── Phase 3: animated banner ──────────────────────
                int afterBanner = drawBanner(2);

                // ── Phase 4: subtitle typewriter ─────────────────
                String sub = "IUT Female Dormitory  ·  Islamic University of Technology";
                Thread.sleep(100);
                typewrite(afterBanner + 1, sub, MUTED, 12);

                // ── Phase 5: animated menu box ───────────────────
                int menuStartRow = afterBanner + 3;
                int promptRow = drawDashboard(
                        "WELCOME TO IUT FEMALE DORMITORY",
                        "Select your role to continue",
                        MENU, THEME, BOX,
                        null,
                        menuStartRow
                );

                // ── Phase 6: read role choice ────────────────────
                System.out.print(SHOW_CUR);
                int choice = FastInput.readInt();
                System.out.print(RESET);

                // ── Handle exit ───────────────────────────────────
                if (choice == 0) {
                    TerminalUI.goodbyeRain();
                    ConsoleUtil.clearAndReset();
                    System.exit(0);
                }

                // ── Phase 8: login panel ──────────────────────────
                BackgroundFiller.applyMainMenuTheme();
                drawBanner(2);
                typewrite(afterBanner + 1, sub, MUTED, 0);

                int mid = afterBanner + 4;
                int col = boxCol();
                int iw = innerW();

                String panelBg = ConsoleColors.bgRGB(16, 11, 30);
                String inputBg = ConsoleColors.bgRGB(22, 16, 38);

                drawLoginBox(mid, col, iw, panelBg, inputBg);

                // ── Read username ────────────────────────────────
                at(mid + 4, col + 14);
                System.out.print(inputBg + THEME);
                System.out.flush();
                MyString username = new MyString(FastInput.readNonEmptyLine());

                // ── Read password with '*' masking ───────────────
                at(mid + 6, col + 14);
                System.out.print(inputBg + THEME);
                System.out.flush();
                MyString password = readMaskedPassword();   // ← JLine masked input

                System.out.print(RESET);

                at(mid + 10, 1);

                // Pin the active theme to the main-menu palette so tSuccess /
                // tError always render with the same BOX + panelBg colours as
                // the login credentials box above — regardless of which
                // dashboard was visited on the previous login.
                TerminalUI.setActiveTheme(BOX, THEME, panelBg);

                controller.handleRoleInput(choice, username, password);

            } catch (Exception e) {
                cleanup();
                System.err.println("[MainDashboard] " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  LOGIN BOX
    //
    //    +0  ╔══════════════════════╗
    //    +1  ║   LOGIN CREDENTIALS  ║
    //    +2  ╠══════════════════════╣
    //    +3  ║                      ║   ← blank
    //    +4  ║  User ID   : [     ] ║   ← username input
    //    +5  ║                      ║   ← blank
    //    +6  ║  Password  : [     ] ║   ← masked password input
    //    +7  ║                      ║   ← blank
    //    +8  ╚══════════════════════╝
    // ─────────────────────────────────────────────────────────────
    private static void drawLoginBox(int topRow, int col, int iw,
            String panelBg, String inputBg)
            throws InterruptedException {

        String b = BOX + panelBg;
        String t = THEME + panelBg;
        String r = RESET;
        int fieldW = Math.max(10, iw - 14);

        String[][] rows = {
            /* +0 top    */{b + "╔" + "═".repeat(iw) + "╗" + r},
            /* +1 title  */ {b + "║" + r + BOLD + t + padC("LOGIN CREDENTIALS", iw) + r + b + "║" + r},
            /* +2 sep    */ {b + "╠" + "═".repeat(iw) + "╣" + r},
            /* +3 blank  */ {b + "║" + panelBg + " ".repeat(iw) + b + "║" + r},
            /* +4 userid */ {b + "║ " + r + INPUT + panelBg + "User ID   : " + r
                + inputBg + THEME + " ".repeat(fieldW) + " " + r + b + "║" + r},
            /* +5 blank  */ {b + "║" + panelBg + " ".repeat(iw) + b + "║" + r},
            /* +6 passwd */ {b + "║ " + r + INPUT + panelBg + "Password  : " + r
                + inputBg + THEME + " ".repeat(fieldW) + " " + r + b + "║" + r},
            /* +7 blank  */ {b + "║" + panelBg + " ".repeat(iw) + b + "║" + r},
            /* +8 bottom */ {b + "╚" + "═".repeat(iw) + "╝" + r},};

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
