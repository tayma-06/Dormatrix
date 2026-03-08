package utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TerminalUI — WezTerm / Kitty true-color terminal engine.
 *
 * • Queries ACTUAL terminal size at runtime (resize-safe centering) • Proper
 * background canvas fill on every draw cycle • Animated gradient banner •
 * Animated box drawing (line by line) • Pulsing prompt on daemon thread • SGR
 * mouse + raw keyboard unified readChoice() • Hover highlight on menu rows
 */
public final class TerminalUI {

    private TerminalUI() {
    }

    // ══════════════════════════════════════════════════════════════
    //  DYNAMIC TERMINAL SIZE
    //  Queried fresh on every screen draw — handles resize.
    //  Uses only fast, non-blocking approaches to avoid app freezes.
    // ══════════════════════════════════════════════════════════════
    private static volatile int cachedTermW = 120;
    private static volatile int cachedTermH = 30;
    private static volatile long lastTermProbeMs = 0L;
    private static final long TERM_PROBE_INTERVAL_MS = 3000L;
    private static final Pattern COLON_NUMBER = Pattern.compile(":\\s*(\\d+)");

    /**
     * Safely run a short-lived subprocess, returning its stdout. Destroys the
     * process if it doesn't finish in time. Returns null on any failure so the
     * caller can fall through.
     */
    private static String runProbe(String[] cmd, int timeoutMs) {
        try {
            Process p = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();
            boolean done = p.waitFor(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!done) {
                p.destroyForcibly();
                return null;
            }
            return new String(p.getInputStream().readAllBytes()).trim();
        } catch (Exception e) {
            return null;
        }
    }

    private static void refreshTerminalSizeIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastTermProbeMs < TERM_PROBE_INTERVAL_MS) {
            return;
        }

        synchronized (TerminalUI.class) {
            now = System.currentTimeMillis();
            if (now - lastTermProbeMs < TERM_PROBE_INTERVAL_MS) {
                return;
            }

            int w = -1, h = -1;

            // ── Priority 1: env vars (WezTerm, Kitty, most modern terminals) ──
            try {
                String ec = System.getenv("COLUMNS");
                String el = System.getenv("LINES");
                if (ec != null) {
                    w = Integer.parseInt(ec.trim());
                }
                if (el != null) {
                    h = Integer.parseInt(el.trim());
                }
            } catch (Exception ignored) {
            }

            // ── Priority 2: tput (Unix only) ─────────────────────────────────
            if (!IS_WINDOWS) {
                if (w <= 0) {
                    String s = runProbe(new String[]{"sh", "-c", "tput cols 2>/dev/null"}, 1000);
                    if (s != null) try {
                        w = Integer.parseInt(s.trim());
                    } catch (Exception ignored) {
                    }
                }
                if (h <= 0) {
                    String s = runProbe(new String[]{"sh", "-c", "tput lines 2>/dev/null"}, 1000);
                    if (s != null) try {
                        h = Integer.parseInt(s.trim());
                    } catch (Exception ignored) {
                    }
                }
            }

            // ── Priority 3: mode con (Windows, width only — height unreliable) ─
            if (IS_WINDOWS && w <= 0) {
                String out = runProbe(new String[]{"cmd", "/c", "mode con"}, 1500);
                if (out != null) {
                    for (String line : out.split("\\R")) {
                        String t = line.trim().toLowerCase();
                        if (t.contains("column")) {
                            Matcher m = COLON_NUMBER.matcher(t);
                            if (m.find()) {
                                w = Integer.parseInt(m.group(1));
                            }
                        }
                    }
                }
            }

            // ── Sanity clamp ───────────────────────────────────────────────────
            if (w > 20) {
                cachedTermW = w;
            }
            if (h > 10) {
                cachedTermH = h;
            }
            lastTermProbeMs = now;
        }
    }

    public static int termW() {
        refreshTerminalSizeIfNeeded();
        return cachedTermW;
    }

    public static int termH() {
        refreshTerminalSizeIfNeeded();
        return cachedTermH;
    }

    /**
     * Center a block of contentW chars in the current terminal.
     */
    public static int centerCol(int contentW) {
        int tw = termW();
        return Math.max(1, (tw - contentW) / 2 + 1);
    }

    /**
     * Width of the dashboard box — 71 chars, never exceeds terminal.
     */
    public static int boxW() {
        return Math.min(71, termW() - 4);
    }

    /**
     * Left col of the box.
     */
    public static int boxCol() {
        return centerCol(boxW());
    }

    /**
     * Inner width (box minus the two border chars).
     */
    public static int innerW() {
        return boxW() - 2;
    }

    // ══════════════════════════════════════════════════════════════
    //  ESCAPE CODES
    // ══════════════════════════════════════════════════════════════
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String HIDE_CUR = "\u001B[?25l";
    public static final String SHOW_CUR = "\u001B[?25h";
    public static final String MOUSE_ON = "\u001B[?1000h\u001B[?1006h";
    public static final String MOUSE_OFF = "\u001B[?1006l\u001B[?1000l";

    public static void at(int row, int col) {
        System.out.print("\u001B[" + row + ";" + col + "H");
    }

    public static void cls() {
        System.out.print("\u001B[2J\u001B[H");
    }

    // ══════════════════════════════════════════════════════════════
    //  BACKGROUND CANVAS FILL
    //  Fills every cell with the current background attribute.
    //  Avoids \u001B[2J because on Windows it clears to the
    //  default background, not the current SGR attribute.
    // ══════════════════════════════════════════════════════════════
    public static void fillCanvas() {
        int w = termW();
        int h = termH();
        StringBuilder sb = new StringBuilder(w * h + h * 20 + 40);
        sb.append("\u001B[2J\u001B[H");
        String row = " ".repeat(w);
        for (int r = 1; r <= h; r++) {
            sb.append("\u001B[").append(r).append(";1H").append(row);
        }
        sb.append("\u001B[H");
        System.out.print(sb);
        System.out.flush();
    }

    // ══════════════════════════════════════════════════════════════
    //  MATRIX RAIN EFFECT — Hot Pink / Magenta theme
    //  Animated falling characters filling the entire terminal
    // ══════════════════════════════════════════════════════════════
    // Use only half-width ASCII glyphs for consistent column alignment
    private static final char[] MATRIX_GLYPHS
            = "DORMATRIX0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%&*<>=/+".toCharArray();
    private static final Random RAND = new Random();

    // Background color for matrix rain — deep black-magenta
    private static final int[] RAIN_BG = {20, 5, 20};

    // Hot pink / magenta palette on dark background
    private static final int[] RAIN_HEAD = {255, 240, 255};  // near-white pink head
    private static final int[] RAIN_BODY = {255, 50, 180};   // hot magenta
    private static final int[] RAIN_MID = {200, 20, 130};    // deep hot pink
    private static final int[] RAIN_GHOST = {80, 5, 55};     // faint dark magenta trail

    private static class RainColumn {

        int col, head, len, speed, tick;

        RainColumn(int c, int maxH, boolean stagger) {
            col = c;
            // Stagger start: some columns begin on-screen immediately,
            // others start just a few rows above. This ensures visible
            // rain from the first frame instead of a long blank wait.
            if (stagger) {
                head = RAND.nextInt(maxH);          // already on-screen
            } else {
                head = -RAND.nextInt(maxH / 3 + 1); // slightly above
            }
            len = 6 + RAND.nextInt(12);
            speed = 1 + RAND.nextInt(2);
            tick = 0;
        }

        void step(int maxH) {
            if (++tick >= speed) {
                tick = 0;
                head++;
            }
            if (head - len > maxH) {
                head = -RAND.nextInt(6);   // quick reset — appear again fast
                len = 6 + RAND.nextInt(12);
                speed = 1 + RAND.nextInt(2);
            }
        }
    }

    /**
     * Plays a matrix rain animation (hot pink/magenta) for the specified
     * duration. Fills the entire terminal with the effect.
     */
    public static void matrixRain(int durationMs) throws InterruptedException {
        // Force a fresh size probe so we use the real WezTerm window size
        lastTermProbeMs = 0L;
        int w = termW();
        int h = termH();

        // Init rain columns — every other column, ~40% start on-screen
        List<RainColumn> cols = new ArrayList<>();
        for (int c = 0; c < w; c += 2) {
            cols.add(new RainColumn(c, h, RAND.nextFloat() < 0.4f));
        }

        char[][] screen = new char[h][w];
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                screen[r][c] = ' ';
            }
        }

        // Build background + clear strings once
        String bgCode = ConsoleColors.bgRGB(RAIN_BG[0], RAIN_BG[1], RAIN_BG[2]);

        // Clear and fill background — CLS works in WezTerm
        System.out.print(HIDE_CUR);
        System.out.print(bgCode);
        System.out.print("\u001B[2J\u001B[H");
        // Explicit cell fill so the bg color is painted everywhere
        StringBuilder init = new StringBuilder(w * h + h * 10);
        String blank = bgCode + " ".repeat(w);
        for (int r = 1; r <= h; r++) {
            init.append("\u001B[").append(r).append(";1H").append(blank);
        }
        init.append("\u001B[H");
        System.out.print(init);
        System.out.flush();

        long end = System.currentTimeMillis() + durationMs;

        while (System.currentTimeMillis() < end) {
            StringBuilder frame = new StringBuilder(cols.size() * h * 25);

            for (RainColumn rc : cols) {
                for (int r = 0; r < h; r++) {
                    int dist = rc.head - r;
                    if (dist < 0 || dist > rc.len) {
                        continue;
                    }

                    int[] color;
                    if (dist == 0) {
                        color = RAIN_HEAD;
                        screen[r][rc.col] = MATRIX_GLYPHS[RAND.nextInt(MATRIX_GLYPHS.length)];
                    } else if (dist <= 2) {
                        color = RAIN_BODY;
                    } else if (dist <= 6) {
                        color = RAIN_MID;
                    } else {
                        color = RAIN_GHOST;
                    }

                    char ch = screen[r][rc.col];
                    if (ch == ' ') {
                        ch = MATRIX_GLYPHS[RAND.nextInt(MATRIX_GLYPHS.length)];
                    }

                    frame.append("\u001B[").append(r + 1).append(";").append(rc.col + 1).append("H");
                    frame.append(bgCode);
                    frame.append(ConsoleColors.fgRGB(color[0], color[1], color[2]));
                    if (dist == 0) {
                        frame.append(BOLD);
                    }
                    frame.append(ch);
                    // Use SGR reset only for BOLD — keep bg color active
                    if (dist == 0) {
                        frame.append("\u001B[22m");  // bold off only

                    }
                }

                // Erase tail cell
                int tail = rc.head - rc.len;
                if (tail >= 0 && tail < h) {
                    frame.append("\u001B[").append(tail + 1).append(";").append(rc.col + 1).append("H");
                    frame.append(bgCode).append(' ');
                    screen[tail][rc.col] = ' ';
                }

                rc.step(h);
            }

            System.out.print(frame);
            System.out.flush();
            Thread.sleep(45);
        }

        // Restore — do NOT print RESET here because applyMainMenuTheme()
        // will set the correct bg immediately after this returns.
        System.out.print(SHOW_CUR);
        System.out.flush();
    }

    /**
     * Quick matrix rain intro (1000ms) - for dashboard transitions
     */
    public static void quickMatrixRain() throws InterruptedException {
        matrixRain(2000);
    }

    // ── Dot-art image ─────────────────────────────────────────────
// 65 columns × 23 rows  (moon + dorm-building silhouette)
// Characters used: · for structure dots, spaces for empty cells.
// You can swap this with any other dot art; the animation is
// fully data-driven from this array.
    private static final String[] DORM_DOT_ART = {
        "        *       *            *      *                  *         ",
        "  *   *          ▒▒▒▒▒    *                 ██████               ",
        "             ▒▒▒▒▒▒▒▒▒▒▒         *        ██████████             ",
        "     *       ▒▒▒▒▒▒▒▒▒▒▒▒▒                ████  ████      *      ",
        "             ▒▒▒▒▒▒▒▒▒▒▒▒▒   *            ████    ██             ",
        "         *                          *     ████  ████  *      *   ",
        "  *                           ▒▒▒▒    *   ██████████     *       ",
        "            *      *        ▒▒▒▒▒▒▒▒        ██████               ",
        "   *     *       *    *     ▒▒▒▒▒▒▒▒     *        *        *     ",
        "           ▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄             ",
        "      *    █████████████████████████████████████████     *       ",
        "           █████████████████████████████████████████             ",
        "           █████████████████████████████████████████             ",
        "           ████▒▒▄▄▄▒▒██████▒▒▄▄▄▒▒██████▒▒▒▒▒▒▒████             ",
        "           ████▒█████▒██████▒████▒▒██████▒▒▓▓▓▒▒████             ",
        "           ████▒█▒█▒█▒██████▒▒▒█▒▒▒██████▒▓▓▓▓▓▒████             ",
        "           ████▒█▒▒▒█▒██████▒▒▀▀▀▒▒██████▒▒▓▓▓▒▒████             ",
        "           █████████████████████████████████████████             ",
        "           ████▒▒▒▒▒▒▒██████▒▒▀▀▀▒▒██████▒▒▒▒▒▒▒████             ",
        "           ████▒▒▓▓▒▒▒██████▒▒▒█▒▒▒██████▒▒▒▓▓▒▒████             ",
        "           ████▒▒▒▒▒▒▒██████▒▒▄▄▄▒▒██████▒▒▒▒▒▒▒████             ",
        "   ▀▀▀▀▀▀▀▀█████████████████████████████████████████▀▀▀▀▀▀▀▀▀▀   ",
        "   ███████████████████████████████████████████████████████████   ",};

    // ── Color palette — hot pink / magenta theme ──
    private static final int[] DR_BG = {20, 5, 20};     // deep black-magenta bg
    private static final int[] DR_HEAD = {255, 235, 255};  // near-white pink head
    private static final int[] DR_FALL_MID = {255, 60, 190};   // hot magenta falling trail
    private static final int[] DR_SETTLE = {200, 20, 130};   // deep hot pink — settled dot
    private static final int[] DR_STAR = {160, 15, 100};   // muted dark pink — stars

    /**
     * Full dorm-rain intro animation.
     *
     * @param durationMs total ms to spend animating (≈ 2000 recommended)
     */
    public static void dormRain(int durationMs) throws InterruptedException {
        // Always re-probe terminal size so we use the live window dimensions
        lastTermProbeMs = 0L;
        int w = termW();
        int h = termH();

        // ── Pad / measure the art ───────────────────────────────────
        int artH = DORM_DOT_ART.length;
        int artW = 0;
        for (String row : DORM_DOT_ART) {
            artW = Math.max(artW, row.length());
        }

        char[][] art = new char[artH][artW];
        for (int r = 0; r < artH; r++) {
            String line = DORM_DOT_ART[r];
            for (int c = 0; c < artW; c++) {
                art[r][c] = c < line.length() ? line.charAt(c) : ' ';
            }
        }

        // Center art in the terminal
        int artStartRow = Math.max(1, (h - artH) / 2);
        int artStartCol = Math.max(1, (w - artW) / 2 + 1);

        // ── Paint blank lavender canvas ─────────────────────────────
        String bgCode = ConsoleColors.bgRGB(DR_BG[0], DR_BG[1], DR_BG[2]);
        System.out.print(HIDE_CUR + bgCode + "\u001B[2J\u001B[H");
        StringBuilder init = new StringBuilder(w * h + h * 30);
        String blankRow = bgCode + " ".repeat(w);
        for (int r = 1; r <= h; r++) {
            init.append("\u001B[").append(r).append(";1H").append(blankRow);
        }
        init.append("\u001B[H");
        System.out.print(init);
        System.out.flush();

        // ── Per-column rain state ───────────────────────────────────
        // head  = current terminal row of the falling droplet tip (1-based)
        // delay = frames to wait before this column starts falling
        // done  = true once the column has fully settled
        int[] head = new int[artW];
        int[] delay = new int[artW];
        boolean[] done = new boolean[artW];
        boolean[] settled = new boolean[artW]; // full column rendered

        for (int c = 0; c < artW; c++) {
            head[c] = 0;
            delay[c] = c;          // stagger: column 0 starts first, c=artW-1 starts last
        }

        // ── Animation loop ──────────────────────────────────────────
        long endTime = System.currentTimeMillis() + durationMs;

        while (System.currentTimeMillis() < endTime) {
            StringBuilder frame = new StringBuilder(artW * h * 30);
            boolean anyActive = false;

            for (int c = 0; c < artW; c++) {
                if (done[c]) {
                    continue;
                }

                if (delay[c] > 0) {
                    delay[c]--;
                    anyActive = true;
                    continue;
                }
                anyActive = true;

                int termCol = artStartCol + c;

                // ── Draw current head character ─────────────────────
                int r = head[c];
                if (r >= 1 && r <= h) {
                    frame.append("\u001B[").append(r).append(";").append(termCol).append("H");
                    frame.append(bgCode);
                    // Use art char if we're inside the art area, else a trail glyph
                    int artRow = r - artStartRow;
                    char ch;
                    if (artRow >= 0 && artRow < artH) {
                        ch = art[artRow][c];
                        if (ch == ' ') {
                            ch = '·';
                        }
                    } else {
                        ch = '·';
                    }
                    frame.append(ConsoleColors.fgRGB(DR_HEAD[0], DR_HEAD[1], DR_HEAD[2]));
                    frame.append(BOLD).append(ch).append("\u001B[22m");
                }

                // ── Render mid-trail one row back ───────────────────
                int trail = r - 1;
                if (trail >= 1 && trail <= h) {
                    int artRow = trail - artStartRow;
                    boolean hasDot = artRow >= 0 && artRow < artH && art[artRow][c] != ' ';
                    frame.append("\u001B[").append(trail).append(";").append(termCol).append("H");
                    frame.append(bgCode);
                    if (hasDot) {
                        // Dot is settling — render in mid-purple
                        frame.append(ConsoleColors.fgRGB(DR_FALL_MID[0], DR_FALL_MID[1], DR_FALL_MID[2]));
                        frame.append(art[artRow][c]);
                    } else {
                        frame.append(' ');
                    }
                }

                // ── Settle the row two back into final color ────────
                int settle = r - 2;
                if (settle >= 1 && settle <= h) {
                    int artRow = settle - artStartRow;
                    boolean hasDot = artRow >= 0 && artRow < artH && art[artRow][c] != ' ';
                    frame.append("\u001B[").append(settle).append(";").append(termCol).append("H");
                    frame.append(bgCode);
                    if (hasDot) {
                        frame.append(ConsoleColors.fgRGB(DR_SETTLE[0], DR_SETTLE[1], DR_SETTLE[2]));
                        frame.append(art[artRow][c]);
                    } else {
                        frame.append(' ');
                    }
                }

                head[c]++;

                // ── Check if this column is fully past the art bottom ─
                if (head[c] > artStartRow + artH + 2) {
                    done[c] = true;
                    // Guarantee all art dots in this column are settled
                    for (int ar = 0; ar < artH; ar++) {
                        char ch = art[ar][c];
                        if (ch != ' ') {
                            int tr = artStartRow + ar;
                            frame.append("\u001B[").append(tr).append(";").append(termCol).append("H");
                            frame.append(bgCode);
                            frame.append(ConsoleColors.fgRGB(DR_SETTLE[0], DR_SETTLE[1], DR_SETTLE[2]));
                            frame.append(ch);
                        }
                    }
                    // Erase any leftover head pixel below art
                    int below = artStartRow + artH;
                    if (below <= h) {
                        frame.append("\u001B[").append(below).append(";").append(termCol).append("H");
                        frame.append(bgCode).append(' ');
                    }
                }
            }

            System.out.print(frame);
            System.out.flush();

            if (!anyActive) {
                break;   // all columns settled — done early

            }
            Thread.sleep(22);
        }

        // ── One gentle brightness pulse over the finished art ───────
        dormArtPulse(artStartRow, artStartCol, art, artH, artW, bgCode);

        // Restore cursor — do NOT print RESET; the caller's applyMainMenuTheme()
        // will set the correct bg immediately after we return.
        System.out.print(SHOW_CUR);
        System.out.flush();
    }

    /**
     * Quick dorm-rain intro (~2 s) for use before the main dashboard. Drop-in
     * replacement for quickMatrixRain().
     */
    public static void quickDormRain() throws InterruptedException {
        dormRain(2000);
    }

    /**
     * Single brightness pulse: sweeps the art from faded → vivid → settled.
     * Called once after all columns have landed.
     */
    private static void dormArtPulse(
            int artStartRow, int artStartCol,
            char[][] art, int artH, int artW,
            String bgCode) throws InterruptedException {

        int steps = 12;
        for (int step = 0; step <= steps; step++) {
            float t = step <= steps / 2
                    ? (float) step / (steps / 2)
                    : 1f - (float) (step - steps / 2) / (steps / 2);
            // Interpolate settled color → bright head color → back
            int fr = lerp(DR_SETTLE[0], DR_HEAD[0], t);
            int fg = lerp(DR_SETTLE[1], DR_HEAD[1], t);
            int fb = lerp(DR_SETTLE[2], DR_HEAD[2], t);

            StringBuilder frame = new StringBuilder(artH * artW * 20);
            for (int r = 0; r < artH; r++) {
                for (int c = 0; c < artW; c++) {
                    char ch = art[r][c];
                    if (ch == ' ') {
                        continue;
                    }
                    frame.append("\u001B[")
                            .append(artStartRow + r).append(";")
                            .append(artStartCol + c).append("H");
                    frame.append(bgCode);
                    frame.append(ConsoleColors.fgRGB(fr, fg, fb));
                    frame.append(ch);
                }
            }
            System.out.print(frame);
            System.out.flush();
            Thread.sleep(40);
        }

        // Final pass: lock all dots to settled color
        StringBuilder fin = new StringBuilder(artH * artW * 20);
        for (int r = 0; r < artH; r++) {
            for (int c = 0; c < artW; c++) {
                char ch = art[r][c];
                if (ch == ' ') {
                    continue;
                }
                fin.append("\u001B[")
                        .append(artStartRow + r).append(";")
                        .append(artStartCol + c).append("H");
                fin.append(bgCode);
                fin.append(ConsoleColors.fgRGB(DR_SETTLE[0], DR_SETTLE[1], DR_SETTLE[2]));
                fin.append(ch);
            }
        }
        System.out.print(fin);
        System.out.flush();
        Thread.sleep(300);  // brief pause so the user sees the finished art
    }

    // ══════════════════════════════════════════════════════════════
    //  COLOR INTERPOLATION
    // ══════════════════════════════════════════════════════════════
    public static int lerp(int a, int b, float t) {
        return (int) (a + t * (b - a));
    }

    public static String gradient(String text, int[] from, int[] to) {
        int n = text.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            float t = n < 2 ? 0f : (float) i / (n - 1);
            sb.append(ConsoleColors.fgRGB(
                    lerp(from[0], to[0], t),
                    lerp(from[1], to[1], t),
                    lerp(from[2], to[2], t)
            )).append(text.charAt(i));
        }
        return sb.toString();
    }

    // ══════════════════════════════════════════════════════════════
    //  BANNER  — violet→cyan gradient, animated line by line
    // ══════════════════════════════════════════════════════════════
    private static final String[] BANNER_LINES = {
        "██████╗  ██████╗ ██████╗ ███╗   ███╗ █████╗ ████████╗██████╗ ██╗██╗  ██╗",
        "██╔══██╗██╔═══██╗██╔══██╗████╗ ████║██╔══██╗╚══██╔══╝██╔══██╗██║╚██╗██╔╝",
        "██║  ██║██║   ██║██████╔╝██╔████╔██║███████║   ██║   ██████╔╝██║ ╚███╔╝ ",
        "██║  ██║██║   ██║██╔══██╗██║╚██╔╝██║██╔══██║   ██║   ██╔══██╗██║ ██╔██╗ ",
        "██████╔╝╚██████╔╝██║  ██║██║ ╚═╝ ██║██║  ██║   ██║   ██║  ██║██║██╔╝ ██╗",
        "╚═════╝  ╚═════╝ ╚═╝  ╚═╝╚═╝     ╚═╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚═╝╚═╝  ╚═╝"
    };

    // ══════════════════════════════════════════════════════════════
    //  BANNER  — paste this block into TerminalUI right after the
    //  GRAD_B declaration.  It was missing from the shared file.
    // ══════════════════════════════════════════════════════════════
    /**
     * Draws the animated gradient DORMATRIX banner, centered. Each line fades
     * in with a 52 ms delay.
     *
     * @param startRow first terminal row to draw on (1-based)
     * @return the row directly after the last banner line
     */
    public static int drawBanner(int startRow) throws InterruptedException {
        int col = centerCol(BANNER_W);
        for (String line : BANNER_LINES) {
            at(startRow, col);
            System.out.print(BOLD + gradient(line, GRAD_A, GRAD_B) + RESET);
            System.out.flush();
            Thread.sleep(52);
            startRow++;
        }
        return startRow;   // row directly after last banner line
    }
    private static final int BANNER_W = 73;
    private static final int[] GRAD_A = {100, 40, 200};  // vivid violet
    private static final int[] GRAD_B = {20, 160, 170};  // deep cyan-teal

    // ══════════════════════════════════════════════════════════════
    //  TYPEWRITER
    // ══════════════════════════════════════════════════════════════
    public static void typewrite(int row, String text, String colorCode, long msPerChar)
            throws InterruptedException {
        at(row, centerCol(text.length()));
        for (char c : text.toCharArray()) {
            System.out.print(colorCode + c + RESET);
            System.out.flush();
            Thread.sleep(msPerChar);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  DIVIDER
    // ══════════════════════════════════════════════════════════════
    public static void divider(int row, String colorCode) {
        int w = Math.min(BANNER_W, termW() - 4);
        at(row, centerCol(w));
        System.out.print(colorCode + "─".repeat(w) + RESET);
        System.out.flush();
    }

    // ══════════════════════════════════════════════════════════════
    //  ANIMATED BOX DRAW
    //
    //  drawDashboard():
    //   • centers box based on live terminal width
    //   • draws each line with a small delay (animated reveal)
    //   • registers each menu row as a clickable region
    //   • returns the row number for the input prompt
    // ══════════════════════════════════════════════════════════════
    public record MenuItem(int number, String label) {

    }

    /**
     * @param extraHeader optional lines inserted between welcome and menu
     * separator (null = skip). Each string is the full pre-colored content.
     * @param startRow first row to draw the box at
     * @return row for the input prompt (2 below bottom border)
     */
    public static int drawDashboard(
            String title,
            String welcome,
            MenuItem[] items,
            String themeColor,
            String boxColor,
            String[] extraHeader,
            int startRow) throws InterruptedException {

        clearRegions();
        clearItemData();

        int col = boxCol();
        int bw = boxW();
        int iw = innerW();
        String b = boxColor + RESET + boxColor;   // border segment
        String t = themeColor;
        String hi = ConsoleColors.Accent.HIGHLIGHT;
        String ex = ConsoleColors.Accent.EXIT;
        String mu = ConsoleColors.Accent.MUTED;

        // ── Top ──────────────────────────────────────────────────
        boxRow(startRow++, col, boxColor + "╔" + "═".repeat(iw) + "╗" + RESET);

        // ── Title ────────────────────────────────────────────────
        boxRow(startRow++, col,
                boxColor + "║" + RESET + BOLD + t + padC(title, iw) + RESET + boxColor + "║" + RESET);

        // ── Welcome separator ────────────────────────────────────
        boxRow(startRow++, col, boxColor + "╠" + "═".repeat(iw) + "╣" + RESET);

        // ── Welcome line ─────────────────────────────────────────
        boxRow(startRow++, col,
                boxColor + "║" + RESET + t + padC(welcome, iw) + RESET + boxColor + "║" + RESET);

        // ── Extra header (e.g. cafeteria clock) ──────────────────
        if (extraHeader != null) {
            for (String line : extraHeader) {
                boxRow(startRow++, col,
                        boxColor + "║ " + RESET + mu + padL(plain(line), iw - 1)
                        + RESET + boxColor + "║" + RESET);
            }
        }

        // ── Menu separator ───────────────────────────────────────
        boxRow(startRow++, col, boxColor + "╠" + "═".repeat(iw) + "╣" + RESET);

        // ── Menu items ───────────────────────────────────────────
        for (MenuItem item : items) {
            String numCol = item.number() == 0 ? ex : hi;
            String lblCol = item.number() == 0 ? mu : t;
            String numStr = "[" + item.number() + "]";
            String content = boxColor + "║ " + RESET
                    + numCol + numStr + RESET
                    + lblCol + " " + padL(item.label(), iw - numStr.length() - 2) + RESET
                    + boxColor + "║" + RESET;
            registerItem(startRow, item.number(), item.label(), themeColor, boxColor);
            boxRow(startRow++, col, content);
        }

        // ── Input separator ──────────────────────────────────────
        boxRow(startRow++, col, boxColor + "╠" + "═".repeat(iw) + "╣" + RESET);

        // ── Input field row ──────────────────────────────────────
        String inputLabel = "Your choice  : ";
        int fieldW = iw - inputLabel.length() - 2;     // 2 = outer padding
        boxRow(startRow, col,
                boxColor + "║ " + RESET
                + ConsoleColors.FG_WHITE + inputLabel + RESET
                + " ".repeat(Math.max(0, fieldW))
                + boxColor + " ║" + RESET);
        int inputRow = startRow++;

        // ── Bottom ───────────────────────────────────────────────
        boxRow(startRow++, col, boxColor + "╚" + "═".repeat(iw) + "╝" + RESET);

        // Position cursor inside the input field
        at(inputRow, col + 2 + inputLabel.length());
        System.out.flush();

        return startRow;   // row after box
    }

    private static void boxRow(int row, int col, String content) throws InterruptedException {
        at(row, col);
        System.out.print(content);
        System.out.flush();
        Thread.sleep(8);
    }

    // strip ANSI for length measurement
    private static String plain(String s) {
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    // ══════════════════════════════════════════════════════════════
    //  LOGOUT BOX
    // ══════════════════════════════════════════════════════════════
    public static void showLogout() throws InterruptedException {
        int iw = innerW();
        int col = boxCol();
        String ex = ConsoleColors.Accent.EXIT;
        String mu = ConsoleColors.Accent.MUTED;
        int mid = termH() / 2;

        boxRow(mid - 1, col, ex + "╔" + "═".repeat(iw) + "╗" + RESET);
        boxRow(mid, col, ex + "║" + RESET + mu + padC("Logging out  . . .", iw) + RESET + ex + "║" + RESET);
        boxRow(mid + 1, col, ex + "╚" + "═".repeat(iw) + "╝" + RESET);
        Thread.sleep(400);
    }

    // ══════════════════════════════════════════════════════════════
    //  FLASH FEEDBACK
    // ══════════════════════════════════════════════════════════════
    public static void flash(int row, String msg, String fgColor, String bgColor)
            throws InterruptedException {
        int col = boxCol();
        int iw = innerW();
        at(row, 1);
        System.out.print(bgColor + " ".repeat(termW()));
        at(row, col + 1);
        System.out.print(bgColor + fgColor + BOLD + padC(msg, iw) + RESET);
        System.out.flush();
        Thread.sleep(220);
    }

    // ══════════════════════════════════════════════════════════════
    //  PULSE THREAD
    // ══════════════════════════════════════════════════════════════
    private static volatile boolean pulseStopped = false;
    private static Thread pulseThread = null;

    public static void startPulse(int row, String text) {
        pulseStopped = false;
        int[] bright = {210, 175, 255};  // bright lavender
        int[] dark = {80, 55, 150};       // dim purple
        pulseThread = new Thread(() -> {
            int p = 0;
            while (!pulseStopped) {
                float t = (float) (p % 20) / 19f;
                if (p % 20 >= 10) {
                    t = 1f - t;
                }
                int col = centerCol(text.length());
                at(row, col);
                System.out.print(ConsoleColors.fgRGB(
                        lerp(dark[0], bright[0], t),
                        lerp(dark[1], bright[1], t),
                        lerp(dark[2], bright[2], t)
                ) + text + RESET);
                System.out.flush();
                try {
                    Thread.sleep(55);
                } catch (Exception e) {
                    break;
                }
                p++;
            }
            at(row, 1);
            System.out.print("\u001B[2K");
            System.out.flush();
        }, "pulse");
        pulseThread.setDaemon(true);
        pulseThread.start();
    }

    public static void stopPulse() {
        pulseStopped = true;
        if (pulseThread != null) {
            try {
                pulseThread.join(400);
            } catch (Exception ignored) {
            }
        }
    }

    private record Region(int row, int c1, int c2, int value) {

    }

    private record ItemData(int row, int number, String label, String theme, String box) {

    }

    private static final List<Region> REGIONS = new ArrayList<>();
    private static final List<ItemData> ITEM_DATA = new ArrayList<>();

    public static void clearRegions() {
        REGIONS.clear();
    }

    public static void clearItemData() {
        ITEM_DATA.clear();
    }

    private static void registerItem(int row, int num, String label, String theme, String box) {
        int col = boxCol();
        REGIONS.add(new Region(row, col, col + boxW() - 1, num));
        ITEM_DATA.add(new ItemData(row, num, label, theme, box));
    }

    private static int hitTest(int row, int col) {
        for (Region r : REGIONS) {
            if (r.row() == row && col >= r.c1() && col <= r.c2()) {
                return r.value();
            }
        }
        return -1;
    }

    private static int hitTestRow(int row) {
        for (Region r : REGIONS) {
            if (r.row() == row) {
                return row;
            }
        }
        return -1;
    }

    private static void renderHighlight(int row, boolean on) {
        for (ItemData d : ITEM_DATA) {
            if (d.row() != row) {
                continue;
            }
            String numStr = "[" + d.number() + "]";
            String numCol = d.number() == 0 ? ConsoleColors.Accent.EXIT : ConsoleColors.Accent.HIGHLIGHT;
            String lblCol = on
                    ? ConsoleColors.fgRGB(25, 15, 55)
                    : (d.number() == 0 ? ConsoleColors.Accent.MUTED : d.theme());
            String bgOn = on ? ConsoleColors.bgRGB(185, 165, 220) : "";
            int iw = innerW();
            at(row, boxCol());
            System.out.print(
                    d.box() + "║ " + RESET
                    + (on ? bgOn : "") + numCol + (on ? BOLD : "") + numStr + RESET
                    + (on ? bgOn : "") + lblCol + (on ? BOLD : "")
                    + " " + padL(d.label(), iw - numStr.length() - 2) + RESET
                    + d.box() + "║" + RESET
            );
            System.out.flush();
            return;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  RAW / COOKED MODE
    // ══════════════════════════════════════════════════════════════
    private static final boolean IS_WINDOWS
            = System.getProperty("os.name", "").toLowerCase().contains("win");

    public static void setRaw() {
        if (IS_WINDOWS) {
            return; // stty not available on Windows

        }
        try {
            new ProcessBuilder("sh", "-c", "stty raw -echo </dev/tty")
                    .inheritIO().start().waitFor();
        } catch (Exception ignored) {
        }
    }

    public static void setCooked() {
        if (IS_WINDOWS) {
            return; // stty not available on Windows

        }
        try {
            new ProcessBuilder("sh", "-c", "stty cooked echo </dev/tty")
                    .inheritIO().start().waitFor();
        } catch (Exception ignored) {
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  UNIFIED CHOICE READER  — keyboard digits + SGR mouse
    // ══════════════════════════════════════════════════════════════
    public static int readChoice() throws Exception {
        InputStream in = System.in;
        int lastHover = -1;

        while (true) {
            int b = in.read();
            if (b == -1) {
                continue;
            }

            // ESC sequence
            if (b == 27) {
                if (in.available() == 0) {
                    return -1;
                }
                int b2 = in.read();
                if (b2 != '[') {
                    continue;
                }
                int b3 = in.read();

                if (b3 == '<') {
                    // SGR mouse: ESC[<btn;col;rowM/m
                    StringBuilder sb = new StringBuilder();
                    int ch;
                    while (true) {
                        ch = in.read();
                        if (ch == 'M' || ch == 'm' || ch == -1) {
                            break;
                        }
                        sb.append((char) ch);
                    }
                    String[] parts = sb.toString().split(";");
                    if (parts.length < 3) {
                        continue;
                    }
                    int btn = Integer.parseInt(parts[0]);
                    int mcol = Integer.parseInt(parts[1]);
                    int mrow = Integer.parseInt(parts[2]);

                    if (ch == 'M' && btn == 0) {
                        // Left button click
                        int hit = hitTest(mrow, mcol);
                        if (hit >= 0) {
                            if (lastHover >= 0) {
                                renderHighlight(lastHover, false);
                            }
                            return hit;
                        }
                    } else if (ch == 'M' && (btn == 32 || btn == 35)) {
                        // Mouse move / hover
                        int hitRow = hitTestRow(mrow);
                        if (hitRow != lastHover) {
                            if (lastHover >= 0) {
                                renderHighlight(lastHover, false);
                            }
                            if (hitRow >= 0) {
                                renderHighlight(hitRow, true);
                            }
                            lastHover = hitRow;
                        }
                    }
                }
                continue;
            }

            // Enter / CR
            if (b == 13 || b == 10) {
                continue;
            }

            // Single digit — instant submit
            if (b >= '0' && b <= '9') {
                if (lastHover >= 0) {
                    renderHighlight(lastHover, false);
                }
                return b - '0';
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  CLEANUP  — always restore terminal state
    // ══════════════════════════════════════════════════════════════
    public static void cleanup() {
        stopPulse();
        System.out.print(MOUSE_OFF + SHOW_CUR + RESET);
        System.out.flush();
        setCooked();
    }

    // ══════════════════════════════════════════════════════════════
    //  STRING HELPERS
    // ══════════════════════════════════════════════════════════════
    /**
     * Center-pad s to exactly w characters.
     */
    public static String padC(String s, int w) {
        int p = Math.max(0, w - s.length());
        return " ".repeat(p / 2) + s + " ".repeat(p - p / 2);
    }

    /**
     * Left-pad s (pad trailing spaces) to exactly w characters.
     */
    public static String padL(String s, int w) {
        if (s.length() >= w) {
            return s.substring(0, w);
        }
        return s + " ".repeat(w - s.length());
    }

    // ══════════════════════════════════════════════════════════════
    //  CENTERED DASHBOARD HELPERS
    //  For consistent centered output across all dashboards
    // ══════════════════════════════════════════════════════════════
    private static final int DASH_W = 71;  // Dashboard box width
    private static final int DASH_IW = 69; // Inner width

    /**
     * Print a line centered in the terminal with background fill.
     */
    public static void printCentered(String text, String fg, String bg) {
        int col = centerCol(text.length());
        System.out.print("\u001B[" + col + "G" + fg + text + RESET);
        System.out.println();
    }

    /**
     * Print a dashboard box line, centered with filled background.
     */
    public static void printBoxLine(String line, String boxColor, String bgColor) {
        int col = centerCol(DASH_W);
        System.out.print("\u001B[" + col + "G" + boxColor + line + RESET);
        System.out.println();
    }

    /**
     * Draw a complete centered dashboard header with title and welcome message.
     * Returns the background color string for continued use.
     */
    public static String drawDashboardHeader(String title, String username, String boxColor, String textColor, String bgColor) {
        int col = boxCol();
        int iw = innerW();

        writeRow("\u001B[" + col + "G" + boxColor + bgColor + "╔" + "═".repeat(iw) + "╗");
        writeRow("\u001B[" + col + "G" + boxColor + bgColor + "║" + textColor + bgColor + padC(trimToWidth(title, iw), iw) + boxColor + bgColor + "║");
        writeRow("\u001B[" + col + "G" + boxColor + bgColor + "╠" + "═".repeat(iw) + "╣");
        String welcomeMsg = "Welcome, " + username;
        writeRow("\u001B[" + col + "G" + boxColor + bgColor + "║" + textColor + bgColor + padC(trimToWidth(welcomeMsg, iw), iw) + boxColor + bgColor + "║");
        writeRow("\u001B[" + col + "G" + boxColor + bgColor + "╠" + "═".repeat(iw) + "╣");

        return bgColor;
    }

    /**
     * Draw a menu item line.
     */
    public static void drawMenuItem(String item, String boxColor, String textColor, String bgColor) {
        int col = boxCol();
        int iw = innerW();
        writeRow("\u001B[" + col + "G" + boxColor + bgColor + "║ " + textColor + bgColor + padL(trimToWidth(item, iw - 2), iw - 2) + boxColor + bgColor + " ║");
    }

    /**
     * Draw bottom border.
     */
    public static void drawBoxBottom(String boxColor, String bgColor) {
        int col = boxCol();
        int iw = innerW();
        writeRow("\u001B[" + col + "G" + boxColor + bgColor + "╚" + "═".repeat(iw) + "╝");
    }

    /**
     * Draw a centered input prompt.
     */
    public static void drawInputPrompt(String prompt, String color, String bgColor) {
        int col = centerCol(prompt.length() + 10);
        System.out.print("\u001B[" + col + "G" + color + prompt + RESET);
    }

    public static void drawInputBox(int startRow, String label,
            String boxColor, String textColor,
            String panelBg, String inputBg)
            throws InterruptedException {

        int col = boxCol();
        int iw = innerW();
        String b = boxColor + panelBg;
        String r = RESET;

        int labelLen = label.length() + 3;          // "Label  : "
        int fieldW = Math.max(10, iw - labelLen - 1);

        String inputColor = ConsoleColors.Accent.INPUT;

        String[] rows = {
            b + "╔" + "═".repeat(iw) + "╗" + r,
            b + "║" + panelBg + " ".repeat(iw) + b + "║" + r,
            b + "║ " + r + inputColor + panelBg + label + " : " + r
            + inputBg + textColor + " ".repeat(fieldW) + r + b + "║" + r,
            b + "║" + panelBg + " ".repeat(iw) + b + "║" + r,
            b + "╚" + "═".repeat(iw) + "╝" + r,};

        for (int i = 0; i < rows.length; i++) {
            at(startRow + i, col);
            System.out.print(rows[i]);
            System.out.flush();
            Thread.sleep(8);
        }

        // Position cursor inside the input field (row +2, after label)
        at(startRow + 2, col + labelLen + 1);
        System.out.print(inputBg + textColor);
        System.out.flush();
    }

    /**
     * Draw centered logout box.
     */
    public static void drawLogoutBox(String boxColor, String textColor, String bgColor) {
        int col = boxCol();
        int iw = innerW();
        writeRow("\u001B[" + col + "G" + boxColor + bgColor + "╔" + "═".repeat(iw) + "╗");
        writeRow("\u001B[" + col + "G" + boxColor + bgColor + "║" + textColor + bgColor + padC("Logging Out....", iw) + boxColor + bgColor + "║");
        writeRow("\u001B[" + col + "G" + boxColor + bgColor + "╚" + "═".repeat(iw) + "╝");
    }

    /**
     * Set the terminal background color. Does NOT repaint or clear. Use
     * BackgroundFiller.applyXTheme() to clear + set color in one step.
     */
    public static void fillBackground(String bgColor) {
        System.out.print(bgColor);
        System.out.flush();
    }

    // ══════════════════════════════════════════════════════════════
    //  ACTIVE THEME CONTEXT
    //  Stored per-thread so any sub-view can use the current theme ═══
    // ══════════════════════════════════════════════════════════
    private static volatile String activeBoxColor = ConsoleColors.Accent.BOX;
    private static volatile String activeTextColor = ConsoleColors.ThemeText.SOFT_WHITE;
    private static volatile String activeBgColor = ConsoleColors.bgRGB(10, 7, 20);
    private static volatile String activePanelBgColor = ConsoleColors.bgRGB(16, 11, 30);

    /**
     * Set the active theme colors that sub-views will inherit.
     */
    public static void setActiveTheme(String boxColor, String textColor, String bgColor) {
        activeBoxColor = boxColor;
        activeTextColor = textColor;
        activeBgColor = bgColor;
        activePanelBgColor = bgColor;
    }

    public static void setActiveTheme(String boxColor, String textColor, String bgColor, String panelBgColor) {
        activeBoxColor = boxColor;
        activeTextColor = textColor;
        activeBgColor = bgColor;
        activePanelBgColor = panelBgColor;
    }

    public static String getActiveBoxColor() {
        return activeBoxColor;
    }

    public static String getActiveTextColor() {
        return activeTextColor;
    }

    public static String getActiveBgColor() {
        return activeBgColor;
    }

    public static String getActivePanelBgColor() {
        return activePanelBgColor;
    }

    private static void writeRow(String content) {
        System.out.print(content + RESET + "\n");
    }

    private static String trimToWidth(String text, int width) {
        if (text.length() <= width) {
            return text;
        }
        return text.substring(0, width);
    }

    // ══════════════════════════════════════════════════════════════
    //  THEMED PRINT HELPERS
    //  Use the active theme for centered, bg-filled output.
    //  Any sub-view can call these without knowing the theme.
    // ══════════════════════════════════════════════════════════════
    /**
     * Print a line of text centered, with theme bg fill.
     */
    public static void tPrint(String text) {
        int col = centerCol(text.length());
        writeRow("\u001B[" + col + "G" + activeTextColor + text);
    }

    public static void tPanelCenter(String text) {
        tPanelCenter(text, activeTextColor);
    }

    public static void tPanelCenter(String text, String fgColor) {
        int col = centerCol(text.length());
        writeRow("\u001B[" + col + "G" + fgColor + activeBgColor + text);
    }

    /**
     * Print a themed box top border.
     */
    public static void tBoxTop() {
        int col = centerCol(DASH_W);
        writeRow("\u001B[" + col + "G" + activeBoxColor + activeBgColor + "╔" + "═".repeat(DASH_IW) + "╗");
    }

    /**
     * Print a themed box title line.
     */
    public static void tBoxTitle(String title) {
        int col = centerCol(DASH_W);
        writeRow("\u001B[" + col + "G" + activeBoxColor + activeBgColor + "║"
                + BOLD + activeTextColor + activeBgColor + padC(trimToWidth(title, DASH_IW), DASH_IW)
                + activeBoxColor + activeBgColor + "║");
    }

    /**
     * Print a themed box separator.
     */
    public static void tBoxSep() {
        int col = centerCol(DASH_W);
        writeRow("\u001B[" + col + "G" + activeBoxColor + activeBgColor + "╠" + "═".repeat(DASH_IW) + "╣");
    }

    /**
     * Print a themed box content line (left-aligned inside box).
     */
    public static void tBoxLine(String text) {
        int col = centerCol(DASH_W);
        String display = trimToWidth(text, DASH_IW - 2);
        writeRow("\u001B[" + col + "G" + activeBoxColor + activeBgColor + "║ "
                + activeTextColor + activeBgColor + padL(display, DASH_IW - 2)
                + activeBoxColor + activeBgColor + " ║");
    }

    /**
     * Print a themed box content line with custom color.
     */
    public static void tBoxLine(String text, String fgColor) {
        int col = centerCol(DASH_W);
        String display = trimToWidth(text, DASH_IW - 2);
        writeRow("\u001B[" + col + "G" + activeBoxColor + activeBgColor + "║ "
                + fgColor + activeBgColor + padL(display, DASH_IW - 2)
                + activeBoxColor + activeBgColor + " ║");
    }

    /**
     * Print a themed box bottom border.
     */
    public static void tBoxBottom() {
        int col = centerCol(DASH_W);
        writeRow("\u001B[" + col + "G" + activeBoxColor + activeBgColor + "╚" + "═".repeat(DASH_IW) + "╝");
    }

    /**
     * Print an empty bg-filled line.
     */
    public static void tEmpty() {
        writeRow("");
    }

    /**
     * Print a themed input prompt, centered.
     */
    public static void tPrompt(String prompt) {
        int col = centerCol(prompt.length() + 10);
        System.out.print("\u001B[" + col + "G" + activeTextColor + prompt + RESET);
    }

    /**
     * Print a themed success message in a centered box.
     */
    public static void tSuccess(String msg) {
        String successColor = ConsoleColors.Accent.SUCCESS;
        tBoxTop();
        int col = centerCol(DASH_W);
        writeRow("\u001B[" + col + "G" + activeBoxColor + activeBgColor + "║"
                + successColor + activeBgColor + padC(trimToWidth(msg, DASH_IW), DASH_IW)
                + activeBoxColor + activeBgColor + "║");
        tBoxBottom();
    }

    /**
     * Print a themed error message in a centered box.
     */
    public static void tError(String msg) {
        String errorColor = ConsoleColors.Accent.ERROR;
        tBoxTop();
        int col = centerCol(DASH_W);
        writeRow("\u001B[" + col + "G" + activeBoxColor + activeBgColor + "║"
                + errorColor + activeBgColor + padC(trimToWidth(msg, DASH_IW), DASH_IW)
                + activeBoxColor + activeBgColor + "║");
        tBoxBottom();
    }

    /**
     * Print themed pause prompt, centered.
     */
    public static void tPause() {
        tEmpty();
        tPrompt("Press Enter to continue...");
        FastInput.readLine();
    }

    /**
     * Draw a complete sub-dashboard: title + menu items + input row inside box.
     * Items should include their [N] prefix. Positions the cursor inside the
     * input field after drawing.
     */
    public static void tSubDashboard(String title, String[] items) {
        tBoxTop();
        tBoxTitle(title);
        tBoxSep();
        for (String item : items) {
            if (item.startsWith("[0]")) {
                tBoxLine(item, ConsoleColors.Accent.EXIT);
            } else {
                tBoxLine(item);
            }
        }
        // Input separator + input row inside the box (matches drawDashboard style)
        tBoxSep();
        tInputRow();
    }

    /**
     * Draws the "Your choice" input row + bottom border and positions the
     * cursor inside the input field. Call this after all menu lines have been
     * drawn (no tBoxBottom needed after this).
     */
    public static void tInputRow() {
        int col = centerCol(DASH_W);
        String inputLabel = "Your choice  : ";
        int fieldW = DASH_IW - inputLabel.length() - 2;
        System.out.print(
                "\u001B[" + col + "G"
                + activeBoxColor + activePanelBgColor + "║ "
                + ConsoleColors.FG_WHITE + inputLabel + RESET + activePanelBgColor
                + " ".repeat(Math.max(0, fieldW))
                + activeBoxColor + activePanelBgColor + " ║" + RESET + "\n"
        );
        tBoxBottom();
        // Move cursor up 2 rows (input row is above the bottom border row)
        System.out.print(SHOW_CUR + "\u001B[2A\u001B[" + (col + 2 + inputLabel.length()) + "G");
        System.out.flush();
    }
}
