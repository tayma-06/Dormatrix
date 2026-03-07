package cli.components;

import utils.ConsoleColors;
import utils.TerminalUI;

/**
 * DormatrixBanner — renders the block-letter DORMATRIX logo.
 *
 * • Violet → cyan true-color gradient
 * • Centered dynamically using live terminal width
 * • printBannerOnTheme()  — no trailing RESET (background stays active)
 * • printBanner()         — adds RESET after (standalone use)
 * • printBannerAnimated() — line-by-line animated reveal (52ms per line)
 */
public class DormatrixBanner {

    private static final String[] LINES = {
            "██████╗  ██████╗ ██████╗ ███╗   ███╗ █████╗ ████████╗██████╗ ██╗██╗  ██╗",
            "██╔══██╗██╔═══██╗██╔══██╗████╗ ████║██╔══██╗╚══██╔══╝██╔══██╗██║╚██╗██╔╝",
            "██║  ██║██║   ██║██████╔╝██╔████╔██║███████║   ██║   ██████╔╝██║ ╚███╔╝ ",
            "██║  ██║██║   ██║██╔══██╗██║╚██╔╝██║██╔══██║   ██║   ██╔══██╗██║ ██╔██╗ ",
            "██████╔╝╚██████╔╝██║  ██║██║ ╚═╝ ██║██║  ██║   ██║   ██║  ██║██║██╔╝ ██╗",
            "╚═════╝  ╚═════╝ ╚═╝  ╚═╝╚═╝     ╚═╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚═╝╚═╝  ╚═╝"
    };

    private static final int   W      = 73;
    private static final int[] GRAD_A = {140,  80, 255};   // violet
    private static final int[] GRAD_B = { 60, 210, 230};   // cyan

    // ── Public API ────────────────────────────────────────────────

    /** Instant draw, adds RESET at end (use in standalone contexts). */
    public void printBanner() {
        draw(true, false);
    }

    /** Instant draw, no trailing RESET (background color stays intact). */
    public void printBannerOnTheme() {
        draw(false, false);
    }

    /**
     * Animated line-by-line reveal (52ms per line), no trailing RESET.
     * Call from inside an InterruptedException-declaring method.
     */
    public void printBannerAnimated() throws InterruptedException {
        int col = TerminalUI.centerCol(W);
        System.out.println();
        for (String line : LINES) {
            // move to correct column then print gradient line
            System.out.print("\r" + " ".repeat(col - 1) + applyGradient(line));
            System.out.println();
            System.out.flush();
            Thread.sleep(52);
        }
    }

    // ── Internals ────────────────────────────────────────────────

    private void draw(boolean reset, boolean animate) {
        int col = TerminalUI.centerCol(W);
        String indent = " ".repeat(Math.max(0, col - 1));
        System.out.println();
        for (String line : LINES) {
            System.out.print(indent + applyGradient(line) + (reset ? TerminalUI.RESET : ""));
            System.out.println();
        }
        System.out.flush();
    }

    private static String applyGradient(String text) {
        StringBuilder sb = new StringBuilder(TerminalUI.BOLD);
        int n = text.length();
        for (int i = 0; i < n; i++) {
            float t  = n < 2 ? 0f : (float) i / (n - 1);
            int r = TerminalUI.lerp(GRAD_A[0], GRAD_B[0], t);
            int g = TerminalUI.lerp(GRAD_A[1], GRAD_B[1], t);
            int b = TerminalUI.lerp(GRAD_A[2], GRAD_B[2], t);
            sb.append(ConsoleColors.fgRGB(r, g, b)).append(text.charAt(i));
        }
        return sb.toString();
    }
}