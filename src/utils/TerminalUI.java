package utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TerminalUI — dynamic true-color terminal engine
 *
 * Fixes:
 * - fills the whole terminal using live size
 * - better Windows terminal height/width probing
 * - auto-centers dashboard blocks on resize
 * - consistent box spacing and right border alignment
 * - keeps all dashboard rows/backgrounds visually consistent
 */
public final class TerminalUI {

    // ─────────────────────────────────────────────────────────────
    // SHARED COLORS
    // ─────────────────────────────────────────────────────────────
    public static final String MUTED = "\u001B[38;2;110;85;170m";
    public static final String ACCENT = "\u001B[38;2;185;140;255m";
    public static final String ERROR = "\u001B[38;2;255;100;100m";
    public static final String HL_BG = "\u001B[48;2;60;40;100m";
    public static final String HL_FG = "\u001B[38;2;230;210;255m";
    public static final String ARROW = "\u001B[38;2;200;160;255m";
    public static final String SEARCH_FG = "\u001B[38;2;140;110;200m";

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String HIDE_CUR = "\u001B[?25l";
    public static final String SHOW_CUR = "\u001B[?25h";
    public static final String MOUSE_ON = "\u001B[?1000h\u001B[?1006h";
    public static final String MOUSE_OFF = "\u001B[?1006l\u001B[?1000l";

    private static volatile String activeBoxColor = ConsoleColors.Accent.BOX;
    private static volatile String activeTextColor = ConsoleColors.ThemeText.SOFT_WHITE;
    private static volatile String activeBgColor = ConsoleColors.bgRGB(10, 7, 20);
    private static volatile String activePanelBgColor = ConsoleColors.bgRGB(16, 11, 30);
    private static volatile String activeInputBgColor = ConsoleColors.bgRGB(24, 18, 42);

    private static org.jline.terminal.Terminal sharedJLineTerminal = null;

    private TerminalUI() {
    }

    // ─────────────────────────────────────────────────────────────
    // BASIC HELPERS
    // ─────────────────────────────────────────────────────────────
    public static String padC(String s, int w) {
        if (s == null) {
            s = "";
        }
        int p = Math.max(0, w - plain(s).length());
        return " ".repeat(p / 2) + s + " ".repeat(p - p / 2);
    }

    public static String padL(String s, int w) {
        if (s == null) {
            s = "";
        }
        if (plain(s).length() >= w) {
            return trimToDisplayWidth(s, w);
        }
        return s + " ".repeat(w - plain(s).length());
    }

    public static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() > max ? s.substring(0, max) : s;
    }

    public static String stripAnsi(String s) {
        return s == null ? "" : s.replaceAll("\u001B\\[[;\\d?]*[ -/]*[@-~]", "");
    }

    public static String topBorder(String label, int innerW, String accent) {
        int dashes = innerW - plain(label).length() - 3;
        return "╭─ " + accent + label + MUTED + " " + "─".repeat(Math.max(0, dashes)) + "╮";
    }

    public static String botBorder(int innerW) {
        return "╰" + "─".repeat(innerW) + "╯";
    }

    private static String plain(String s) {
        return stripAnsi(s);
    }

    private static String trimToDisplayWidth(String s, int width) {
        String p = plain(s);
        if (p.length() <= width) {
            return s;
        }
        return p.substring(0, width);
    }

    private static String trimToWidth(String text, int width) {
        if (text == null) {
            return "";
        }
        return text.length() <= width ? text : text.substring(0, width);
    }

    // ─────────────────────────────────────────────────────────────
    // LIVE TERMINAL SIZE
    // ─────────────────────────────────────────────────────────────
    private static volatile int cachedTermW = 120;
    private static volatile int cachedTermH = 30;
    private static volatile long lastTermProbeMs = 0L;
    private static final long TERM_PROBE_INTERVAL_MS = 250L;

    private static final boolean IS_WINDOWS =
            System.getProperty("os.name", "").toLowerCase().contains("win");

    private static final Pattern COLON_NUMBER = Pattern.compile(":\\s*(\\d+)");
    private static final Pattern MODE_CON_COLUMNS = Pattern.compile("(?i)columns?:\\s*(\\d+)");
    private static final Pattern MODE_CON_LINES = Pattern.compile("(?i)lines?:\\s*(\\d+)");

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

    private static void refreshTerminalSizeNow() {
        synchronized (TerminalUI.class) {
            int w = -1;
            int h = -1;

            // 1) JLine terminal if available
            try {
                if (sharedJLineTerminal != null) {
                    int jw = sharedJLineTerminal.getWidth();
                    int jh = sharedJLineTerminal.getHeight();
                    if (jw > 0) {
                        w = jw;
                    }
                    if (jh > 0) {
                        h = jh;
                    }
                }
            } catch (Exception ignored) {
            }

            // 2) Environment variables
            try {
                String ec = System.getenv("COLUMNS");
                String el = System.getenv("LINES");
                if (w <= 0 && ec != null) {
                    w = Integer.parseInt(ec.trim());
                }
                if (h <= 0 && el != null) {
                    h = Integer.parseInt(el.trim());
                }
            } catch (Exception ignored) {
            }

            // 3) Unix probes
            if (!IS_WINDOWS) {
                if (w <= 0 || h <= 0) {
                    String s = runProbe(new String[]{"sh", "-c", "stty size </dev/tty 2>/dev/null"}, 700);
                    if (s != null && s.matches("\\d+\\s+\\d+")) {
                        String[] parts = s.trim().split("\\s+");
                        try {
                            int sh = Integer.parseInt(parts[0]);
                            int sw = Integer.parseInt(parts[1]);
                            if (w <= 0) {
                                w = sw;
                            }
                            if (h <= 0) {
                                h = sh;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }

                if (w <= 0) {
                    String s = runProbe(new String[]{"sh", "-c", "tput cols 2>/dev/null"}, 700);
                    if (s != null) {
                        try {
                            w = Integer.parseInt(s.trim());
                        } catch (Exception ignored) {
                        }
                    }
                }
                if (h <= 0) {
                    String s = runProbe(new String[]{"sh", "-c", "tput lines 2>/dev/null"}, 700);
                    if (s != null) {
                        try {
                            h = Integer.parseInt(s.trim());
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            // 4) Windows mode con (width + height)
            if (IS_WINDOWS && (w <= 0 || h <= 0)) {
                String out = runProbe(new String[]{"cmd", "/c", "mode con"}, 1000);
                if (out != null) {
                    Matcher mw = MODE_CON_COLUMNS.matcher(out);
                    Matcher mh = MODE_CON_LINES.matcher(out);
                    if (w <= 0 && mw.find()) {
                        try {
                            w = Integer.parseInt(mw.group(1));
                        } catch (Exception ignored) {
                        }
                    }
                    if (h <= 0 && mh.find()) {
                        try {
                            h = Integer.parseInt(mh.group(1));
                        } catch (Exception ignored) {
                        }
                    }

                    // fallback more permissive scan
                    if (w <= 0 || h <= 0) {
                        for (String line : out.split("\\R")) {
                            String t = line.trim().toLowerCase();
                            Matcher m = COLON_NUMBER.matcher(t);
                            if (m.find()) {
                                int val;
                                try {
                                    val = Integer.parseInt(m.group(1));
                                } catch (Exception e) {
                                    continue;
                                }
                                if (t.contains("column") && w <= 0) {
                                    w = val;
                                } else if (t.contains("line") && h <= 0) {
                                    h = val;
                                }
                            }
                        }
                    }
                }
            }

            if (w > 20) {
                cachedTermW = w;
            }
            if (h > 10) {
                cachedTermH = h;
            }
            lastTermProbeMs = System.currentTimeMillis();
        }
    }

    private static void refreshTerminalSizeIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastTermProbeMs >= TERM_PROBE_INTERVAL_MS) {
            refreshTerminalSizeNow();
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

    public static int centerCol(int contentW) {
        int tw = termW();
        return Math.max(1, (tw - contentW) / 2 + 1);
    }

    public static int centerRow(int contentH) {
        int th = termH();
        return Math.max(1, (th - contentH) / 2 + 1);
    }

    /**
     * Dynamic dashboard width:
     * - small terminals shrink safely
     * - normal terminals use classic 71
     * - wide terminals can expand slightly
     */
    public static int boxW() {
        int tw = termW();
        if (tw <= 76) {
            return Math.max(40, tw - 4);
        }
        if (tw <= 110) {
            return Math.min(71, tw - 4);
        }
        return Math.min(88, tw - 6);
    }

    public static int boxCol() {
        return centerCol(boxW());
    }

    public static int innerW() {
        return boxW() - 2;
    }

    // ─────────────────────────────────────────────────────────────
    // CURSOR / CLEAR
    // ─────────────────────────────────────────────────────────────
    public static void at(int row, int col) {
        System.out.print("\u001B[" + row + ";" + col + "H");
    }

    public static void cls() {
        refreshTerminalSizeNow();
        System.out.print("\u001B[2J\u001B[H");
    }

    /**
     * Fills the current terminal with the active background.
     */
    public static void fillCanvas() {
        refreshTerminalSizeNow();
        int w = termW();
        int h = termH();
        StringBuilder sb = new StringBuilder(w * h + h * 20 + 40);
        sb.append("\u001B[2J\u001B[H");
        String row = activeBgColor + " ".repeat(w);
        for (int r = 1; r <= h; r++) {
            sb.append("\u001B[").append(r).append(";1H").append(row);
        }
        sb.append("\u001B[H").append(activeBgColor).append(activeTextColor);
        System.out.print(sb);
        System.out.flush();
    }

    // ─────────────────────────────────────────────────────────────
    // FULL-SCREEN GRADIENT PAINT
    // ─────────────────────────────────────────────────────────────
    public static void paintScreenGradient(int[] top, int[] mid, int[] bottom, String fgColor) {
        refreshTerminalSizeNow();
        int w = Math.max(1, termW());
        int h = Math.max(1, termH());

        StringBuilder sb = new StringBuilder(w * h + h * 48 + 128);
        sb.append("\u001B[2J\u001B[H");

        String spaces = " ".repeat(w);
        int split = Math.max(1, h / 2);

        for (int r = 1; r <= h; r++) {
            int[] c;
            if (r <= split) {
                double t = (split <= 1) ? 0.0 : (double) (r - 1) / (split - 1);
                c = lerpColor(top, mid, t);
            } else {
                double t = (h - split <= 1) ? 0.0 : (double) (r - split - 1) / (h - split - 1);
                c = lerpColor(mid, bottom, t);
            }
            sb.append("\u001B[").append(r).append(";1H")
                    .append(ConsoleColors.bgRGB(c[0], c[1], c[2]))
                    .append(fgColor)
                    .append(spaces);
        }

        sb.append("\u001B[H").append(fgColor);
        System.out.print(sb);
        System.out.flush();
    }

    public static void fillBackground(String bgColor) {
        int[] rgb = parseBgRGB(bgColor);
        if (rgb == null) {
            System.out.print(bgColor);
            System.out.flush();
            return;
        }

        int[] top = {Math.max(rgb[0] - 10, 0), Math.max(rgb[1] - 10, 0), Math.max(rgb[2] - 10, 0)};
        int[] mid = {Math.min(rgb[0] + 10, 255), Math.min(rgb[1] + 10, 255), Math.min(rgb[2] + 10, 255)};
        int[] bot = {Math.max(rgb[0] - 8, 0), Math.max(rgb[1] - 8, 0), Math.max(rgb[2] - 8, 0)};
        paintScreenGradient(top, mid, bot, activeTextColor);
    }

    private static int[] parseBgRGB(String esc) {
        if (esc == null) {
            return null;
        }
        String prefix = "\u001B[48;2;";
        if (!esc.startsWith(prefix)) {
            return null;
        }
        String body = esc.substring(prefix.length());
        if (body.endsWith("m")) {
            body = body.substring(0, body.length() - 1);
        }
        String[] parts = body.split(";");
        if (parts.length != 3) {
            return null;
        }
        try {
            return new int[]{
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
            };
        } catch (Exception e) {
            return null;
        }
    }

    private static int[] lerpColor(int[] a, int[] b, double t) {
        if (t < 0) {
            t = 0;
        }
        if (t > 1) {
            t = 1;
        }
        return new int[]{
                (int) (a[0] + (b[0] - a[0]) * t),
                (int) (a[1] + (b[1] - a[1]) * t),
                (int) (a[2] + (b[2] - a[2]) * t)
        };
    }

    // ─────────────────────────────────────────────────────────────
    // MATRIX RAIN
    // ─────────────────────────────────────────────────────────────
    private static final char[] MATRIX_GLYPHS =
            "DORMATRIX0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%&*<>=/+".toCharArray();
    private static final Random RAND = new Random();

    private static final int[] RAIN_BG = {20, 5, 20};
    private static final int[] RAIN_HEAD = {255, 240, 255};
    private static final int[] RAIN_BODY = {255, 50, 180};
    private static final int[] RAIN_MID = {200, 20, 130};
    private static final int[] RAIN_GHOST = {80, 5, 55};

    private static class RainColumn {
        int col, head, len, speed, tick;

        RainColumn(int c, int maxH, boolean stagger) {
            col = c;
            if (stagger) {
                head = RAND.nextInt(Math.max(1, maxH));
            } else {
                head = -RAND.nextInt(Math.max(1, maxH / 3 + 1));
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
                head = -RAND.nextInt(6);
                len = 6 + RAND.nextInt(12);
                speed = 1 + RAND.nextInt(2);
            }
        }
    }

    public static void matrixRain(int durationMs) throws InterruptedException {
        refreshTerminalSizeNow();
        int w = termW();
        int h = termH();

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

        String bgCode = ConsoleColors.bgRGB(RAIN_BG[0], RAIN_BG[1], RAIN_BG[2]);

        System.out.print(HIDE_CUR);
        System.out.print(bgCode);
        System.out.print("\u001B[2J\u001B[H");

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
                    if (dist == 0) {
                        frame.append("\u001B[22m");
                    }
                }

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

        System.out.print(SHOW_CUR);
        System.out.flush();
    }

    public static void quickMatrixRain() throws InterruptedException {
        matrixRain(2000);
    }

    // ─────────────────────────────────────────────────────────────
    // DORM RAIN INTRO
    // ─────────────────────────────────────────────────────────────
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
            "   ███████████████████████████████████████████████████████████   ",
    };

    private static final int[] DR_BG = {20, 5, 20};
    private static final int[] DR_HEAD = {255, 235, 255};
    private static final int[] DR_FALL_MID = {255, 60, 190};
    private static final int[] DR_SETTLE = {200, 20, 130};

    public static void dormRain(int durationMs) throws InterruptedException {
        refreshTerminalSizeNow();
        int w = termW();
        int h = termH();

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

        int artStartRow = Math.max(1, (h - artH) / 2);
        int artStartCol = Math.max(1, (w - artW) / 2 + 1);

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

        int[] head = new int[artW];
        int[] delay = new int[artW];
        boolean[] done = new boolean[artW];

        for (int c = 0; c < artW; c++) {
            head[c] = 0;
            delay[c] = c;
        }

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

                int r = head[c];
                if (r >= 1 && r <= h) {
                    frame.append("\u001B[").append(r).append(";").append(termCol).append("H");
                    frame.append(bgCode);
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

                int trail = r - 1;
                if (trail >= 1 && trail <= h) {
                    int artRow = trail - artStartRow;
                    boolean hasDot = artRow >= 0 && artRow < artH && art[artRow][c] != ' ';
                    frame.append("\u001B[").append(trail).append(";").append(termCol).append("H");
                    frame.append(bgCode);
                    if (hasDot) {
                        frame.append(ConsoleColors.fgRGB(DR_FALL_MID[0], DR_FALL_MID[1], DR_FALL_MID[2]));
                        frame.append(art[artRow][c]);
                    } else {
                        frame.append(' ');
                    }
                }

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

                if (head[c] > artStartRow + artH + 2) {
                    done[c] = true;
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
                break;
            }
            Thread.sleep(22);
        }

        dormArtPulse(artStartRow, artStartCol, art, artH, artW, bgCode);

        System.out.print(SHOW_CUR);
        System.out.flush();
    }

    public static void quickDormRain() throws InterruptedException {
        dormRain(2000);
    }

    private static void dormArtPulse(
            int artStartRow, int artStartCol,
            char[][] art, int artH, int artW,
            String bgCode) throws InterruptedException {

        int steps = 12;
        for (int step = 0; step <= steps; step++) {
            float t = step <= steps / 2
                    ? (float) step / (steps / 2)
                    : 1f - (float) (step - steps / 2) / (steps / 2);

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
        Thread.sleep(300);
    }

    // ─────────────────────────────────────────────────────────────
    // GOODBYE RAIN
    // ─────────────────────────────────────────────────────────────
    private static final String[] GOODBYE_BANNER = {
            "▒██████╗▒▒██████╗▒▒██████╗▒██████╗▒██████╗▒██╗▒▒▒██╗███████╗▒",
            "██╔════╝▒██╔═══██╗██╔═══██╗██╔══██╗██╔══██╗╚██╗▒██╔╝██╔════╝▒",
            "██║▒▒███╗██║▒▒▒██║██║▒▒▒██║██║▒▒██║██████╔╝▒╚████╔╝▒█████╗▒▒▒",
            "██║▒▒▒██║██║▒▒▒██║██║▒▒▒██║██║▒▒██║██╔══██╗▒▒╚██╔╝▒▒██╔══╝▒▒▒",
            "╚██████╔╝╚██████╔╝╚██████╔╝██████╔╝██████╔╝▒▒▒██║▒▒▒███████╗▒",
            "▒╚═════╝▒▒╚═════╝▒▒╚═════╝▒╚═════╝▒╚═════╝▒▒▒▒╚═╝▒▒▒╚══════╝▒"
    };

    private static final int[] GB_BG = {10, 0, 0};
    private static final int[] GB_HEAD = {255, 230, 200};
    private static final int[] GB_MID = {255, 60, 20};
    private static final int[] GB_SETTLE = {180, 10, 10};

    private static final String[] GLITCH_NAMES = {
            "DORMATRIX KHADIZA SULTANA",
            "AYMAN BINTE ALTAF NONDINY",
            "SAYMA TASNIM",
            "PROCHETA SILVIE"
    };

    public static void goodbyeRain() throws InterruptedException {
        refreshTerminalSizeNow();
        int w = termW();
        int h = termH();
        java.util.Random rng = new java.util.Random();

        int artH = GOODBYE_BANNER.length;
        int artW = 0;
        for (String row : GOODBYE_BANNER) {
            artW = Math.max(artW, row.length());
        }

        char[][] art = new char[artH][artW];
        for (int r = 0; r < artH; r++) {
            String line = GOODBYE_BANNER[r];
            for (int c = 0; c < artW; c++) {
                art[r][c] = c < line.length() ? line.charAt(c) : ' ';
            }
        }

        int artStartRow = Math.max(1, (h - artH) / 2 - 1);
        int artStartCol = Math.max(1, (w - artW) / 2 + 1);

        String bgCode = ConsoleColors.bgRGB(GB_BG[0], GB_BG[1], GB_BG[2]);
        System.out.print(HIDE_CUR + bgCode + "\u001B[2J\u001B[H");
        StringBuilder init = new StringBuilder();
        for (int r = 1; r <= h + 8; r++) {
            init.append("\u001B[").append(r).append(";1H").append(bgCode).append("\u001B[2K");
        }
        System.out.print(init);
        System.out.flush();

        int[] head = new int[artW];
        int[] delay = new int[artW];
        boolean[] done = new boolean[artW];
        for (int c = 0; c < artW; c++) {
            head[c] = 0;
            delay[c] = c / 2;
        }

        long end = System.currentTimeMillis() + 2600;
        while (System.currentTimeMillis() < end) {
            StringBuilder frame = new StringBuilder(artW * h * 28);
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

                int tc = artStartCol + c;
                int r = head[c];

                if (r >= artStartRow && r < artStartRow + artH) {
                    int ar = r - artStartRow;
                    char ch = art[ar][c];
                    if (ch != ' ') {
                        frame.append("\u001B[").append(r).append(";").append(tc).append("H")
                                .append(bgCode)
                                .append(ConsoleColors.fgRGB(GB_HEAD[0], GB_HEAD[1], GB_HEAD[2]))
                                .append(BOLD).append(ch).append("\u001B[22m");
                    }
                }

                int trail = r - 1;
                if (trail >= 1 && trail <= h) {
                    int ar = trail - artStartRow;
                    boolean hasDot = ar >= 0 && ar < artH && art[ar][c] != ' ';
                    frame.append("\u001B[").append(trail).append(";").append(tc).append("H")
                            .append(bgCode);
                    if (hasDot) {
                        frame.append(ConsoleColors.fgRGB(GB_MID[0], GB_MID[1], GB_MID[2])).append(art[ar][c]);
                    } else {
                        frame.append(' ');
                    }
                }

                int settle = r - 2;
                if (settle >= 1 && settle <= h) {
                    int ar = settle - artStartRow;
                    boolean hasDot = ar >= 0 && ar < artH && art[ar][c] != ' ';
                    frame.append("\u001B[").append(settle).append(";").append(tc).append("H")
                            .append(bgCode);
                    if (hasDot) {
                        frame.append(ConsoleColors.fgRGB(GB_SETTLE[0], GB_SETTLE[1], GB_SETTLE[2])).append(art[ar][c]);
                    } else {
                        frame.append(' ');
                    }
                }

                head[c]++;
                if (head[c] > artStartRow + artH + 2) {
                    done[c] = true;
                    for (int ar = 0; ar < artH; ar++) {
                        char ch = art[ar][c];
                        if (ch != ' ') {
                            frame.append("\u001B[").append(artStartRow + ar).append(";").append(tc).append("H")
                                    .append(bgCode)
                                    .append(ConsoleColors.fgRGB(GB_SETTLE[0], GB_SETTLE[1], GB_SETTLE[2]))
                                    .append(ch);
                        }
                    }
                    int below = artStartRow + artH;
                    if (below <= h) {
                        frame.append("\u001B[").append(below).append(";").append(tc).append("H")
                                .append(bgCode).append(' ');
                    }
                }
            }

            System.out.print(frame);
            System.out.flush();
            if (!anyActive) {
                break;
            }
            Thread.sleep(18);
        }

        String exitMsg = "Exiting Dormatrix...";
        int exitRow = artStartRow + artH + 2;
        int exitCol = Math.max(1, (w - exitMsg.length()) / 2 + 1);
        String exitFg = ConsoleColors.fgRGB(200, 30, 30);
        StringBuilder msgBuf = new StringBuilder();
        for (int i = 0; i < exitMsg.length(); i++) {
            msgBuf.append("\u001B[").append(exitRow).append(";").append(exitCol + i).append("H")
                    .append(bgCode).append(exitFg).append(exitMsg.charAt(i));
            System.out.print(msgBuf);
            msgBuf.setLength(0);
            System.out.flush();
            Thread.sleep(70);
        }
        Thread.sleep(2000);

        for (int step = 0; step <= 10; step++) {
            float t = step <= 5 ? step / 5f : 1f - (step - 5) / 5f;
            int fr = lerp(GB_SETTLE[0], 255, t);
            int fg = lerp(GB_SETTLE[1], 80, t);
            int fb = lerp(GB_SETTLE[2], 40, t);
            StringBuilder frame = new StringBuilder();
            for (int r = 0; r < artH; r++) {
                for (int c = 0; c < artW; c++) {
                    char ch = art[r][c];
                    if (ch == ' ') {
                        continue;
                    }
                    frame.append("\u001B[").append(artStartRow + r).append(";").append(artStartCol + c).append("H")
                            .append(bgCode).append(ConsoleColors.fgRGB(fr, fg, fb)).append(ch);
                }
            }
            System.out.print(frame);
            System.out.flush();
            Thread.sleep(35);
        }

        for (int glitchPass = 0; glitchPass < GLITCH_NAMES.length; glitchPass++) {
            String name = GLITCH_NAMES[glitchPass];
            int nameLen = name.length();
            int namePos = 0;

            StringBuilder gFrame = new StringBuilder();
            for (int r = 0; r < artH; r++) {
                for (int c = 0; c < artW; c++) {
                    char ch = art[r][c];
                    if (ch == ' ') {
                        continue;
                    }
                    char render;
                    if (rng.nextDouble() < 0.45) {
                        char nc = name.charAt(namePos % nameLen);
                        namePos++;
                        if (nc == ' ') {
                            nc = name.charAt(namePos % nameLen);
                            namePos++;
                        }
                        render = nc;
                    } else {
                        render = ch;
                    }
                    int[] gc = (rng.nextInt(3) == 0)
                            ? new int[]{255, 220, 180}
                            : (rng.nextInt(2) == 0 ? new int[]{255, 40, 10} : GB_SETTLE);

                    gFrame.append("\u001B[").append(artStartRow + r).append(";").append(artStartCol + c).append("H")
                            .append(bgCode).append(ConsoleColors.fgRGB(gc[0], gc[1], gc[2])).append(render);
                }
            }
            System.out.print(gFrame);
            System.out.flush();
            Thread.sleep(80);

            StringBuilder restore = new StringBuilder();
            for (int r = 0; r < artH; r++) {
                for (int c = 0; c < artW; c++) {
                    char ch = art[r][c];
                    if (ch == ' ') {
                        continue;
                    }
                    restore.append("\u001B[").append(artStartRow + r).append(";").append(artStartCol + c).append("H")
                            .append(bgCode).append(ConsoleColors.fgRGB(GB_SETTLE[0], GB_SETTLE[1], GB_SETTLE[2])).append(ch);
                }
            }
            System.out.print(restore);
            System.out.flush();
            Thread.sleep(60);
        }

        for (int step = 0; step <= 12; step++) {
            float t = step / 12f;
            int fr = lerp(GB_SETTLE[0], 0, t);
            int fg = lerp(GB_SETTLE[1], 0, t);
            int fb = lerp(GB_SETTLE[2], 0, t);
            String fadeBg = ConsoleColors.bgRGB(
                    lerp(GB_BG[0], 0, t),
                    lerp(GB_BG[1], 0, t),
                    lerp(GB_BG[2], 0, t)
            );
            StringBuilder frame = new StringBuilder();
            for (int r = 0; r < artH; r++) {
                for (int c = 0; c < artW; c++) {
                    char ch = art[r][c];
                    if (ch == ' ') {
                        continue;
                    }
                    frame.append("\u001B[").append(artStartRow + r).append(";").append(artStartCol + c).append("H")
                            .append(fadeBg).append(ConsoleColors.fgRGB(fr, fg, fb)).append(ch);
                }
            }
            System.out.print(frame);
            System.out.flush();
            Thread.sleep(40);
        }
        Thread.sleep(120);
        System.out.print(SHOW_CUR);
        System.out.flush();
    }

    // ─────────────────────────────────────────────────────────────
    // COLOR INTERPOLATION / BANNER
    // ─────────────────────────────────────────────────────────────
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

    private static final String[] BANNER_LINES = {
            "██████╗▒▒██████╗▒██████╗▒███╗▒▒▒███╗▒█████╗▒████████╗██████╗▒██╗██╗▒▒██╗",
            "██╔══██╗██╔═══██╗██╔══██╗████╗▒████║██╔══██╗╚══██╔══╝██╔══██╗██║╚██╗██╔╝",
            "██║▒▒██║██║▒▒▒██║██████╔╝██╔████╔██║███████║▒▒▒██║▒▒▒██████╔╝██║▒╚███╔╝▒",
            "██║▒▒██║██║▒▒▒██║██╔══██╗██║╚██╔╝██║██╔══██║▒▒▒██║▒▒▒██╔══██╗██║▒██╔██╗▒",
            "██████╔╝╚██████╔╝██║▒▒██║██║▒╚═╝▒██║██║▒▒██║▒▒▒██║▒▒▒██║▒▒██║██║██╔╝▒██╗",
            "╚═════╝▒▒╚═════╝▒╚═╝▒▒╚═╝╚═╝▒▒▒▒▒╚═╝╚═╝▒▒╚═╝▒▒▒╚═╝▒▒▒╚═╝▒▒╚═╝╚═╝╚═╝▒▒╚═╝"
    };
    private static final int BANNER_W = 73;
    private static final int[] GRAD_A = {120, 50, 220};
    private static final int[] GRAD_B = {30, 180, 190};

    public static int drawBanner(int startRow) throws InterruptedException {
        int col = centerCol(BANNER_W);
        for (String line : BANNER_LINES) {
            at(startRow, col);
            System.out.print(activeBgColor + BOLD + gradient(line, GRAD_A, GRAD_B) + "\u001B[22m");
            System.out.flush();
            Thread.sleep(52);
            startRow++;
        }
        return startRow;
    }

    public static void typewrite(int row, String text, String colorCode, long msPerChar)
            throws InterruptedException {
        at(row, centerCol(text.length()));
        System.out.print(activeBgColor);
        for (char c : text.toCharArray()) {
            System.out.print(colorCode + activeBgColor + c);
            System.out.flush();
            Thread.sleep(msPerChar);
        }
        System.out.print(activeTextColor + activeBgColor);
        System.out.flush();
    }

    public static void divider(int row, String colorCode) {
        int w = Math.min(BANNER_W, termW() - 4);
        at(row, centerCol(w));
        System.out.print(activeBgColor + colorCode + "─".repeat(w));
        System.out.flush();
    }

    // ─────────────────────────────────────────────────────────────
    // DASHBOARD DRAWING
    // ─────────────────────────────────────────────────────────────
    public record MenuItem(int number, String label) {
    }

    private static final int DASH_W_FALLBACK = 71;
    private static final int DASH_IW_FALLBACK = 69;

    private record Region(int row, int c1, int c2, int value) {
    }

    private record ItemData(int row, int number, String label, String theme, String box) {
    }

    private static final List<Region> REGIONS = new ArrayList<>();
    private static final List<ItemData> ITEM_DATA = new ArrayList<>();
    private static int inputFieldRow = -1;
    private static int inputFieldCol = -1;
    private static int notifyRow = -1;

    public static void clearRegions() {
        REGIONS.clear();
    }

    public static void clearItemData() {
        ITEM_DATA.clear();
        inputFieldRow = -1;
        inputFieldCol = -1;
        notifyRow = -1;
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

    private static int autoDashboardTop(int itemsCount, int extraHeaderCount) {
        int contentH = 8 + itemsCount + Math.max(0, extraHeaderCount);
        return Math.max(2, centerRow(contentH) - 1);
    }

    private static String boxBorder(String left, String mid, String right, String boxColor, String bg, int iw) {
        return boxColor + bg + left + mid.repeat(Math.max(0, iw)) + right + RESET;
    }

    private static String boxContentLine(String text, String fg, String boxColor, String bg, int iw) {
        return boxColor + bg + "║"
                + fg + bg + padC(trimToWidth(text, iw), iw)
                + boxColor + bg + "║" + RESET;
    }

    private static String boxContentLeft(String text, String fg, String boxColor, String bg, int iw) {
        return boxColor + bg + "║ "
                + fg + bg + padL(trimToWidth(text, iw - 2), iw - 2)
                + boxColor + bg + " ║" + RESET;
    }

    private static String buildMenuLine(int number, String label, boolean selected,
                                        String themeColor, String boxColor, int iw) {
        String panel = activePanelBgColor;
        String numStr = "[" + number + "]";
        String numCol = number == 0 ? ConsoleColors.Accent.EXIT : ConsoleColors.Accent.HIGHLIGHT;
        String labelColor = number == 0 ? ConsoleColors.Accent.MUTED : themeColor;

        String rowBg = selected ? ConsoleColors.bgRGB(185, 165, 220) : panel;
        String rowFg = selected ? ConsoleColors.fgRGB(25, 15, 55) : labelColor;
        String rowNumFg = selected ? ConsoleColors.fgRGB(80, 55, 0) : numCol;

        int leftContentW = iw - 2;
        String content = numStr + " " + label;
        String padded = padL(trimToWidth(content, leftContentW), leftContentW);

        StringBuilder sb = new StringBuilder();
        sb.append(boxColor).append(panel).append("║ ");
        sb.append(rowBg).append(rowNumFg);
        sb.append(numStr);
        sb.append(rowBg).append(rowFg);
        sb.append(" ").append(padL(trimToWidth(label, leftContentW - numStr.length() - 1),
                leftContentW - numStr.length() - 1));
        sb.append(boxColor).append(panel).append(" ║").append(RESET);
        return sb.toString();
    }

    private static String buildInputRow(String label, String currentText, String boxColor, String panelBg,
                                        String inputBg, int iw) {
        String safeText = currentText == null ? "" : currentText;
        int leftWidth = label.length();
        int fieldW = Math.max(1, iw - leftWidth - 2);

        if (safeText.length() > fieldW) {
            safeText = safeText.substring(0, fieldW);
        }

        return boxColor + panelBg + "║ "
                + ConsoleColors.FG_WHITE + panelBg + label
                + inputBg + ConsoleColors.FG_WHITE + safeText
                + " ".repeat(Math.max(0, fieldW - safeText.length()))
                + boxColor + panelBg + " ║" + RESET;
    }

    public static int drawDashboard(
            String title,
            String welcome,
            MenuItem[] items,
            String themeColor,
            String boxColor,
            String[] extraHeader,
            int startRow) throws InterruptedException {

        refreshTerminalSizeNow();
        clearRegions();
        clearItemData();

        int extraCount = extraHeader == null ? 0 : extraHeader.length;
        if (startRow <= 3) {
            startRow = autoDashboardTop(items.length, extraCount);
        }

        int col = boxCol();
        int iw = innerW();
        String panel = activePanelBgColor;
        String inputBg = activeInputBgColor;
        String muted = ConsoleColors.Accent.MUTED;

        boxRow(startRow++, col, boxBorder("╔", "═", "╗", boxColor, panel, iw));
        boxRow(startRow++, col, boxContentLine(title, BOLD + themeColor, boxColor, panel, iw));
        boxRow(startRow++, col, boxBorder("╠", "═", "╣", boxColor, panel, iw));
        boxRow(startRow++, col, boxContentLine(welcome, themeColor, boxColor, panel, iw));

        if (extraHeader != null) {
            for (String line : extraHeader) {
                boxRow(startRow++, col, boxContentLeft(plain(line), muted, boxColor, panel, iw));
            }
        }

        boxRow(startRow++, col, boxBorder("╠", "═", "╣", boxColor, panel, iw));

        for (MenuItem item : items) {
            registerItem(startRow, item.number(), item.label(), themeColor, boxColor);
            boxRow(startRow++, col, buildMenuLine(item.number(), item.label(), false, themeColor, boxColor, iw));
        }

        boxRow(startRow++, col, boxBorder("╠", "═", "╣", boxColor, panel, iw));

        String inputLabel = "Your choice  : ";
        inputFieldRow = startRow;
        inputFieldCol = col + 2 + inputLabel.length();

        boxRow(startRow++, col, buildInputRow(inputLabel, "", boxColor, panel, inputBg, iw));
        boxRow(startRow++, col, boxBorder("╚", "═", "╝", boxColor, panel, iw));

        notifyRow = startRow;
        System.out.flush();
        return startRow;
    }

    private static void boxRow(int row, int col, String content) throws InterruptedException {
        at(row, col);
        System.out.print(content);
        System.out.flush();
        Thread.sleep(8);
    }

    private static void updateInputField(String text) {
        if (inputFieldRow < 0) {
            return;
        }
        clearNotify();

        String safe = text == null ? "" : text;
        int fieldW = Math.max(1, innerW() - "Your choice  : ".length() - 2);
        if (safe.length() > fieldW) {
            safe = safe.substring(0, fieldW);
        }

        at(inputFieldRow, inputFieldCol);
        System.out.print(activeInputBgColor + ConsoleColors.FG_WHITE + ConsoleColors.BOLD
                + safe
                + " ".repeat(Math.max(0, fieldW - safe.length()))
                + RESET);

        at(inputFieldRow, inputFieldCol + safe.length());
        System.out.flush();
    }

    private static void updateInputFieldError(String text) {
        if (inputFieldRow >= 0) {
            int fieldW = Math.max(1, innerW() - "Your choice  : ".length() - 2);
            at(inputFieldRow, inputFieldCol);
            System.out.print(activeInputBgColor + " ".repeat(fieldW) + RESET);
            System.out.flush();
        }
        paintNotifyError(text);
        if (inputFieldRow >= 0) {
            at(inputFieldRow, inputFieldCol);
        }
    }

    private static void paintNotifyError(String text) {
        if (notifyRow < 0) {
            return;
        }
        int col = boxCol();
        int iw = innerW();
        String err = ConsoleColors.Accent.ERROR;
        String box = activeBoxColor;
        String panel = activePanelBgColor;
        String line = padC(trimToWidth(text, iw), iw);

        at(notifyRow, col);
        System.out.print(box + panel + "╔" + "═".repeat(iw) + "╗" + RESET);

        at(notifyRow + 1, col);
        System.out.print(box + panel + "║" + err + panel + BOLD + line + box + panel + "║" + RESET);

        at(notifyRow + 2, col);
        System.out.print(box + panel + "╚" + "═".repeat(iw) + "╝" + RESET);

        System.out.flush();
    }

    private static void clearNotify() {
        if (notifyRow < 0) {
            return;
        }
        int col = boxCol();
        int bw = boxW();
        String panel = activePanelBgColor;
        for (int r = notifyRow; r <= notifyRow + 2; r++) {
            at(r, col);
            System.out.print(panel + " ".repeat(bw) + RESET);
        }
        System.out.flush();
    }

    private static void renderHighlight(int row, boolean on) {
        for (ItemData d : ITEM_DATA) {
            if (d.row() != row) {
                continue;
            }
            at(row, boxCol());
            System.out.print(buildMenuLine(d.number(), d.label(), on, d.theme(), d.box(), innerW()));
            System.out.flush();
            return;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // LOGOUT / FLASH / PULSE
    // ─────────────────────────────────────────────────────────────
    public static void showLogout() throws InterruptedException {
        refreshTerminalSizeNow();
        int iw = innerW();
        int col = boxCol();
        int mid = centerRow(3);
        String ex = ConsoleColors.Accent.EXIT;
        String mu = ConsoleColors.Accent.MUTED;
        String panel = activePanelBgColor;

        boxRow(mid - 1, col, boxBorder("╔", "═", "╗", ex, panel, iw));
        boxRow(mid, col, boxContentLine("Logging out  . . .", mu, ex, panel, iw));
        boxRow(mid + 1, col, boxBorder("╚", "═", "╝", ex, panel, iw));
        Thread.sleep(400);
    }

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

    private static volatile boolean pulseStopped = false;
    private static Thread pulseThread = null;

    public static void startPulse(int row, String text) {
        pulseStopped = false;
        int[] bright = {210, 175, 255};
        int[] dark = {80, 55, 150};

        pulseThread = new Thread(() -> {
            int p = 0;
            while (!pulseStopped) {
                float t = (float) (p % 20) / 19f;
                if (p % 20 >= 10) {
                    t = 1f - t;
                }
                int col = centerCol(text.length());
                at(row, col);
                System.out.print(activeBgColor + ConsoleColors.fgRGB(
                        lerp(dark[0], bright[0], t),
                        lerp(dark[1], bright[1], t),
                        lerp(dark[2], bright[2], t)
                ) + text);
                System.out.flush();
                try {
                    Thread.sleep(55);
                } catch (Exception e) {
                    break;
                }
                p++;
            }
            at(row, 1);
            System.out.print(activeBgColor + " ".repeat(termW()));
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

    // ─────────────────────────────────────────────────────────────
    // RAW / COOKED MODE
    // ─────────────────────────────────────────────────────────────
    public static void setRaw() {
        if (IS_WINDOWS) {
            return;
        }
        try {
            new ProcessBuilder("sh", "-c", "stty raw -echo </dev/tty")
                    .inheritIO().start().waitFor();
        } catch (Exception ignored) {
        }
    }

    public static void setCooked() {
        if (IS_WINDOWS) {
            return;
        }
        try {
            new ProcessBuilder("sh", "-c", "stty cooked echo </dev/tty")
                    .inheritIO().start().waitFor();
        } catch (Exception ignored) {
        }
    }

    // ─────────────────────────────────────────────────────────────
    // MOUSE / KEYBOARD INPUT
    // ─────────────────────────────────────────────────────────────
    public static int readChoice() throws Exception {
        InputStream in = System.in;
        int lastHover = -1;

        while (true) {
            int b = in.read();
            if (b == -1) {
                continue;
            }

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
                        int hit = hitTest(mrow, mcol);
                        if (hit >= 0) {
                            if (lastHover >= 0) {
                                renderHighlight(lastHover, false);
                            }
                            return hit;
                        }
                    } else if (ch == 'M' && (btn == 32 || btn == 35)) {
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

            if (b == 13 || b == 10) {
                continue;
            }

            if (b >= '0' && b <= '9') {
                if (lastHover >= 0) {
                    renderHighlight(lastHover, false);
                }
                return b - '0';
            }
        }
    }

    public static int readChoiceArrow() throws Exception {
        if (ITEM_DATA.isEmpty()) {
            return -1;
        }

        int selected = 0;
        StringBuilder inputBuffer = new StringBuilder();
        System.out.print(HIDE_CUR);
        renderHighlight(ITEM_DATA.get(selected).row(), true);
        updateInputField(String.valueOf(ITEM_DATA.get(selected).number()));
        System.out.print(SHOW_CUR);
        System.out.flush();

        if (sharedJLineTerminal != null) {
            org.jline.terminal.Attributes saved = sharedJLineTerminal.enterRawMode();
            org.jline.utils.NonBlockingReader reader = sharedJLineTerminal.reader();
            try {
                while (true) {
                    int c = reader.read();
                    if (c == -1) {
                        continue;
                    }

                    if (c == 27) {
                        int n1 = reader.read(100);
                        if (n1 == '[' || n1 == 'O') {
                            int n2 = reader.read(100);
                            switch (n2) {
                                case 'A' -> {
                                    renderHighlight(ITEM_DATA.get(selected).row(), false);
                                    selected = (selected - 1 + ITEM_DATA.size()) % ITEM_DATA.size();
                                    renderHighlight(ITEM_DATA.get(selected).row(), true);
                                    inputBuffer.setLength(0);
                                    updateInputField(String.valueOf(ITEM_DATA.get(selected).number()));
                                    System.out.flush();
                                }
                                case 'B' -> {
                                    renderHighlight(ITEM_DATA.get(selected).row(), false);
                                    selected = (selected + 1) % ITEM_DATA.size();
                                    renderHighlight(ITEM_DATA.get(selected).row(), true);
                                    inputBuffer.setLength(0);
                                    updateInputField(String.valueOf(ITEM_DATA.get(selected).number()));
                                    System.out.flush();
                                }
                            }
                        }
                        continue;
                    }

                    if (c == 13 || c == 10) {
                        if (inputBuffer.length() > 0) {
                            try {
                                int typed = Integer.parseInt(inputBuffer.toString());
                                for (ItemData d : ITEM_DATA) {
                                    if (d.number() == typed) {
                                        renderHighlight(ITEM_DATA.get(selected).row(), false);
                                        return typed;
                                    }
                                }
                            } catch (NumberFormatException ignored) {
                            }
                            inputBuffer.setLength(0);
                            updateInputFieldError("Invalid choice input");
                        } else {
                            renderHighlight(ITEM_DATA.get(selected).row(), false);
                            return ITEM_DATA.get(selected).number();
                        }
                    }

                    if (c == 3) {
                        renderHighlight(ITEM_DATA.get(selected).row(), false);
                        return 0;
                    }

                    if (c == 127 || c == 8) {
                        if (inputBuffer.length() > 0) {
                            inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                            updateInputField(inputBuffer.length() > 0
                                    ? inputBuffer.toString()
                                    : String.valueOf(ITEM_DATA.get(selected).number()));
                            System.out.flush();
                        }
                        continue;
                    }

                    if (c >= '0' && c <= '9') {
                        inputBuffer.append((char) c);
                        updateInputField(inputBuffer.toString());
                        System.out.flush();
                    }
                }
            } finally {
                sharedJLineTerminal.setAttributes(saved);
                System.out.print(SHOW_CUR);
                System.out.flush();
            }
        }

        setRaw();
        InputStream in = System.in;
        try {
            while (true) {
                int b = in.read();
                if (b == -1) {
                    continue;
                }
                if (b == 27) {
                    if (in.available() == 0) {
                        continue;
                    }
                    int b2 = in.read();
                    if (b2 != '[') {
                        continue;
                    }
                    int b3 = in.read();
                    switch (b3) {
                        case 'A' -> {
                            renderHighlight(ITEM_DATA.get(selected).row(), false);
                            selected = (selected - 1 + ITEM_DATA.size()) % ITEM_DATA.size();
                            renderHighlight(ITEM_DATA.get(selected).row(), true);
                            inputBuffer.setLength(0);
                            updateInputField(String.valueOf(ITEM_DATA.get(selected).number()));
                            System.out.flush();
                        }
                        case 'B' -> {
                            renderHighlight(ITEM_DATA.get(selected).row(), false);
                            selected = (selected + 1) % ITEM_DATA.size();
                            renderHighlight(ITEM_DATA.get(selected).row(), true);
                            inputBuffer.setLength(0);
                            updateInputField(String.valueOf(ITEM_DATA.get(selected).number()));
                            System.out.flush();
                        }
                    }
                    continue;
                }

                if (b == 13 || b == 10) {
                    if (inputBuffer.length() > 0) {
                        try {
                            int typed = Integer.parseInt(inputBuffer.toString());
                            for (ItemData d : ITEM_DATA) {
                                if (d.number() == typed) {
                                    renderHighlight(ITEM_DATA.get(selected).row(), false);
                                    return typed;
                                }
                            }
                        } catch (NumberFormatException ignored) {
                        }
                        inputBuffer.setLength(0);
                        updateInputFieldError("Invalid choice input");
                    } else {
                        renderHighlight(ITEM_DATA.get(selected).row(), false);
                        return ITEM_DATA.get(selected).number();
                    }
                }

                if (b == 127 || b == 8) {
                    if (inputBuffer.length() > 0) {
                        inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                        updateInputField(inputBuffer.length() > 0
                                ? inputBuffer.toString()
                                : String.valueOf(ITEM_DATA.get(selected).number()));
                        System.out.flush();
                    }
                    continue;
                }

                if (b >= '0' && b <= '9') {
                    inputBuffer.append((char) b);
                    updateInputField(inputBuffer.toString());
                    System.out.flush();
                }
            }
        } finally {
            setCooked();
            System.out.print(SHOW_CUR);
            System.out.flush();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CLEANUP
    // ─────────────────────────────────────────────────────────────
    public static void cleanup() {
        stopPulse();
        System.out.print(MOUSE_OFF + SHOW_CUR + RESET);
        System.out.flush();
        setCooked();
    }

    // ─────────────────────────────────────────────────────────────
    // CENTERED / THEMED HELPERS
    // ─────────────────────────────────────────────────────────────
    private static final int DASH_W = DASH_W_FALLBACK;
    private static final int DASH_IW = DASH_IW_FALLBACK;

    public static void printCentered(String text, String fg, String bg) {
        int col = centerCol(plain(text).length());
        System.out.print("\u001B[" + col + "G" + bg + fg + text + RESET);
        System.out.println();
    }

    public static void printBoxLine(String line, String boxColor, String bgColor) {
        int col = boxCol();
        System.out.print("\u001B[" + col + "G" + boxColor + bgColor + line + RESET);
        System.out.println();
    }

    public static String drawDashboardHeader(String title, String username,
                                             String boxColor, String textColor, String bgColor) {
        int col = boxCol();
        int iw = innerW();

        writeRowAt(col, boxColor + bgColor + "╔" + "═".repeat(iw) + "╗");
        writeRowAt(col, boxColor + bgColor + "║" + textColor + bgColor + padC(trimToWidth(title, iw), iw) + boxColor + bgColor + "║");
        writeRowAt(col, boxColor + bgColor + "╠" + "═".repeat(iw) + "╣");
        String welcomeMsg = "Welcome, " + username;
        writeRowAt(col, boxColor + bgColor + "║" + textColor + bgColor + padC(trimToWidth(welcomeMsg, iw), iw) + boxColor + bgColor + "║");
        writeRowAt(col, boxColor + bgColor + "╠" + "═".repeat(iw) + "╣");

        return bgColor;
    }

    public static void drawMenuItem(String item, String boxColor, String textColor, String bgColor) {
        int col = boxCol();
        int iw = innerW();
        writeRowAt(col, boxColor + bgColor + "║ " + textColor + bgColor + padL(trimToWidth(item, iw - 2), iw - 2) + boxColor + bgColor + " ║");
    }

    public static void drawBoxBottom(String boxColor, String bgColor) {
        int col = boxCol();
        int iw = innerW();
        writeRowAt(col, boxColor + bgColor + "╚" + "═".repeat(iw) + "╝");
    }

    public static void drawInputPrompt(String prompt, String color, String bgColor) {
        int col = centerCol(prompt.length());
        System.out.print("\u001B[" + col + "G" + bgColor + color + prompt + RESET);
    }

    public static void drawInputBox(int startRow, String label,
                                    String boxColor, String textColor,
                                    String panelBg, String inputBg)
            throws InterruptedException {

        if (startRow <= 3) {
            startRow = Math.max(2, centerRow(5) - 1);
        }

        int col = boxCol();
        int iw = innerW();
        String b = boxColor + panelBg;
        String r = RESET;

        int labelLen = label.length() + 3;
        int fieldW = Math.max(10, iw - labelLen - 2);

        String inputColor = ConsoleColors.Accent.INPUT;

        String[] rows = {
                b + "╔" + "═".repeat(iw) + "╗" + r,
                b + "║" + panelBg + " ".repeat(iw) + b + "║" + r,
                b + "║ " + inputColor + panelBg + label + " : "
                        + inputBg + textColor + " ".repeat(fieldW)
                        + b + panelBg + " ║" + r,
                b + "║" + panelBg + " ".repeat(iw) + b + "║" + r,
                b + "╚" + "═".repeat(iw) + "╝" + r,
        };

        for (int i = 0; i < rows.length; i++) {
            at(startRow + i, col);
            System.out.print(rows[i]);
            System.out.flush();
            Thread.sleep(8);
        }

        at(startRow + 2, col + label.length() + 6);
        System.out.print(inputBg + textColor);
        System.out.flush();
    }

    public static void drawLogoutBox(String boxColor, String textColor, String bgColor) {
        int col = boxCol();
        int iw = innerW();
        writeRowAt(col, boxColor + bgColor + "╔" + "═".repeat(iw) + "╗");
        writeRowAt(col, boxColor + bgColor + "║" + textColor + bgColor + padC("Logging Out....", iw) + boxColor + bgColor + "║");
        writeRowAt(col, boxColor + bgColor + "╚" + "═".repeat(iw) + "╝");
    }

    // ─────────────────────────────────────────────────────────────
    // ACTIVE THEME
    // ─────────────────────────────────────────────────────────────
    public static void setActiveTheme(String boxColor, String textColor, String bgColor) {
        activeBoxColor = boxColor;
        activeTextColor = textColor;
        activeBgColor = bgColor;
        activePanelBgColor = bgColor;
        activeInputBgColor = bgColor;
    }

    public static void setActiveTheme(String boxColor, String textColor,
                                      String bgColor, String panelBgColor) {
        activeBoxColor = boxColor;
        activeTextColor = textColor;
        activeBgColor = bgColor;
        activePanelBgColor = panelBgColor;
        activeInputBgColor = panelBgColor;
    }

    public static void setActiveTheme(String boxColor, String textColor,
                                      String bgColor, String panelBgColor, String inputBgColor) {
        activeBoxColor = boxColor;
        activeTextColor = textColor;
        activeBgColor = bgColor;
        activePanelBgColor = panelBgColor;
        activeInputBgColor = inputBgColor;
    }

    public static String getActiveInputBgColor() {
        return activeInputBgColor;
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

    private static void writeRowAt(int col, String content) {
        System.out.print("\u001B[" + col + "G" + content + RESET + "\n");
    }

    public static void tPrint(String text) {
        int col = centerCol(text.length());
        writeRowAt(col, activeBgColor + activeTextColor + text);
    }

    public static void tPanelCenter(String text) {
        tPanelCenter(text, activeTextColor);
    }

    public static void tPanelCenter(String text, String fgColor) {
        int col = centerCol(text.length());
        writeRowAt(col, fgColor + activeBgColor + text);
    }

    public static void tBoxTop() {
        int col = boxCol();
        writeRowAt(col, activeBoxColor + activePanelBgColor + "╔" + "═".repeat(innerW()) + "╗");
    }

    public static void tBoxTitle(String title) {
        int col = boxCol();
        int iw = innerW();
        writeRowAt(col, activeBoxColor + activePanelBgColor + "║"
                + BOLD + activeTextColor + activePanelBgColor + padC(trimToWidth(title, iw), iw)
                + activeBoxColor + activePanelBgColor + "║");
    }

    public static void tBoxSep() {
        int col = boxCol();
        writeRowAt(col, activeBoxColor + activePanelBgColor + "╠" + "═".repeat(innerW()) + "╣");
    }

    public static void tBoxLine(String text) {
        int col = boxCol();
        int iw = innerW();
        String display = trimToWidth(text, iw - 2);
        writeRowAt(col, activeBoxColor + activePanelBgColor + "║ "
                + activeTextColor + activePanelBgColor + padL(display, iw - 2)
                + activeBoxColor + activePanelBgColor + " ║");
    }

    public static void tBoxLine(String text, String fgColor) {
        int col = boxCol();
        int iw = innerW();
        String display = trimToWidth(text, iw - 2);
        writeRowAt(col, activeBoxColor + activePanelBgColor + "║ "
                + fgColor + activePanelBgColor + padL(display, iw - 2)
                + activeBoxColor + activePanelBgColor + " ║");
    }

    public static void tBoxBottom() {
        int col = boxCol();
        writeRowAt(col, activeBoxColor + activePanelBgColor + "╚" + "═".repeat(innerW()) + "╝");
    }

    public static void tEmpty() {
        writeRow(activeBgColor);
    }

    public static void tPrompt(String prompt) {
        int col = centerCol(prompt.length());
        System.out.print("\u001B[" + col + "G" + activeBgColor + activeTextColor + prompt + RESET);
    }

    public static void tSuccess(String msg) {
        tBoxTop();
        int col = boxCol();
        writeRowAt(col, activeBoxColor + activePanelBgColor + "║"
                + activeTextColor + activePanelBgColor + padC(trimToWidth(msg, innerW()), innerW())
                + activeBoxColor + activePanelBgColor + "║");
        tBoxBottom();
    }

    public static void tError(String msg) {
        String errorColor = ConsoleColors.Accent.ERROR;
        tBoxTop();
        int col = boxCol();
        writeRowAt(col, activeBoxColor + activePanelBgColor + "║"
                + errorColor + activePanelBgColor + padC(trimToWidth(msg, innerW()), innerW())
                + activeBoxColor + activePanelBgColor + "║");
        tBoxBottom();
    }

    public static void tPause() {
        tEmpty();
        tPrompt("Press Enter to continue...");
        FastInput.readLine();
    }

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
        tBoxSep();
        tInputRow();
    }

    public static void tInputRow() {
        int col = boxCol();
        int iw = innerW();
        String inputLabel = "Your choice  : ";
        int fieldW = Math.max(1, iw - inputLabel.length() - 2);
        System.out.print(
                "\u001B[" + col + "G"
                        + activeBoxColor + activePanelBgColor + "║ "
                        + ConsoleColors.FG_WHITE + activePanelBgColor + inputLabel
                        + activeInputBgColor + ConsoleColors.FG_WHITE
                        + " ".repeat(fieldW)
                        + activeBoxColor + activePanelBgColor + " ║"
                        + RESET + "\n"
        );
        tBoxBottom();
        System.out.print(SHOW_CUR + "\u001B[2A\u001B[" + (col + 2 + inputLabel.length()) + "G");
        System.out.flush();
    }

    public static void tInteractiveDashboard(String title, String[] items, int selectedIndex) {
        tBoxTop();
        tBoxTitle(title);
        tBoxSep();

        for (int i = 0; i < items.length; i++) {
            boolean isSelected = (i == selectedIndex);
            boolean isExit = items[i].toLowerCase().contains("exit");
            tInteractiveBoxLine(items[i], isSelected, isExit);
        }

        tBoxBottom();
    }

    private static void tInteractiveBoxLine(String text, boolean isSelected, boolean isExit) {
        int col = boxCol();
        int iw = innerW();
        String b = activeBoxColor + activePanelBgColor;
        String r = RESET;

        String prefix = isSelected ? "  > " : "    ";
        String rowBg = isSelected ? ConsoleColors.bgRGB(60, 60, 80) : activePanelBgColor;
        String textColor = isExit
                ? ConsoleColors.Accent.EXIT
                : (isSelected ? ConsoleColors.FG_WHITE + ConsoleColors.BOLD : ConsoleColors.FG_WHITE);

        String content = prefix + text;
        int padLen = Math.max(0, iw - 2 - content.length());

        System.out.print(
                "\u001B[" + col + "G"
                        + b + "║ "
                        + rowBg + textColor + content + " ".repeat(padLen)
                        + b + " ║" + r + "\n"
        );
    }

    // ─────────────────────────────────────────────────────────────
    // JLINE BRIDGE
    // ─────────────────────────────────────────────────────────────
    public static void setJLineTerminal(org.jline.terminal.Terminal t) {
        sharedJLineTerminal = t;
        refreshTerminalSizeNow();
    }

    public static void highlightItem(int number, boolean on) {
        for (ItemData d : ITEM_DATA) {
            if (d.number() == number) {
                renderHighlight(d.row(), on);
                return;
            }
        }
    }
}