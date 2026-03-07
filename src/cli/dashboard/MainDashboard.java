package cli.dashboard;

import controllers.dashboard.MainDashboardController;
import libraries.collections.MyString;
import utils.*;

import static utils.TerminalUI.*;

public class MainDashboard {

    private final MainDashboardController controller = new MainDashboardController();

    private static final String BOX = ConsoleColors.Accent.BOX;
    private static final String THEME = ConsoleColors.ThemeText.SOFT_WHITE;
    private static final String INPUT = ConsoleColors.Accent.INPUT;
    private static final String EXIT = ConsoleColors.Accent.EXIT;
    private static final String MUTED = ConsoleColors.Accent.MUTED;

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
        Runtime.getRuntime().addShutdownHook(new Thread(TerminalUI::cleanup));

        boolean firstRun = true;

        while (true) {
            try {

                // ── Phase 1: matrix rain (first visit only) ───────
                if (firstRun) {
                    firstRun = false;
                    quickMatrixRain();            // plays 2 s of pink/magenta rain
                }

                // ── Phase 2: apply theme + fill canvas ────────────
                BackgroundFiller.applyMainMenuTheme();   // sets bg escape + fillCanvas()
                System.out.print(HIDE_CUR);

                // ── Phase 3: animated banner ──────────────────────
                int afterBanner = drawBanner(2);         // rows 2-6, returns row 7

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

                // ── Phase 6: read input ─────────────────────────
                System.out.print(SHOW_CUR);
                int choice = FastInput.readInt();
                System.out.print(RESET);

                // ── Handle exit ───────────────────────────────────
                if (choice == 0) {
                    BackgroundFiller.applyMainMenuTheme();
                    flash(termH() / 2,
                            "Exiting Dormatrix  —  Goodbye!",
                            EXIT,
                            ConsoleColors.bgRGB(61, 0, 8));
                    Thread.sleep(400);
                    BackgroundFiller.resetTheme();
                    ConsoleUtil.clearScreen();
                    System.exit(0);
                }

                // ── Phase 8: login panel (absolute positioned) ────
                //
                //  We redraw the background so the login box appears
                //  cleanly over it instead of over the old menu text.
                //
                BackgroundFiller.applyMainMenuTheme();
                drawBanner(2);                           // keep banner visible

                // Subtitle
                typewrite(afterBanner + 1, sub, MUTED, 0);  // instant replay

                int mid = afterBanner + 4;
                int col = boxCol();
                int iw = innerW();

                // Panel bg is slightly lighter than the screen bg
                String panelBg = ConsoleColors.bgRGB(16, 11, 30);
                String inputBg = ConsoleColors.bgRGB(22, 16, 38);

                // Draw login box — fully absolute, no println()
                drawLoginBox(mid, col, iw, panelBg, inputBg);

                // ── Position cursor inside User ID field and read ─
                at(mid + 4, col + 14);
                System.out.print(inputBg + THEME);
                System.out.flush();
                MyString username = new MyString(FastInput.readNonEmptyLine());

                // Redraw password row (username input may have scrolled)
                at(mid + 6, col + 14);
                System.out.print(inputBg + THEME);
                System.out.flush();
                MyString password = InputHelper.readPassword();

                System.out.print(RESET);

                // Move cursor below the login box so success/error messages don't overlap
                at(mid + 10, 1);

                // Hand off to controller
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
    //  Draws a credential panel starting at `topRow`.
    //  Layout (rows relative to topRow):
    //
    //    +0  ╔══════════════════════╗
    //    +1  ║   LOGIN CREDENTIALS  ║
    //    +2  ╠══════════════════════╣
    //    +3  ║                      ║   ← blank
    //    +4  ║  User ID   : [     ] ║   ← input here
    //    +5  ║                      ║   ← blank
    //    +6  ║  Password  : [     ] ║   ← input here
    //    +7  ║                      ║   ← blank
    //    +8  ╚══════════════════════╝
    //
    // ─────────────────────────────────────────────────────────────
    private static void drawLoginBox(int topRow, int col, int iw,
            String panelBg, String inputBg)
            throws InterruptedException {

        String b = BOX + panelBg;
        String t = THEME + panelBg;
        String r = RESET;

        // Field label width = 12 chars ("User ID   : ")
        // Input field width = iw - 14 chars
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
