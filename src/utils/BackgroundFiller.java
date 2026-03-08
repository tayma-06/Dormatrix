package utils;

public final class BackgroundFiller {

    private BackgroundFiller() {
    }

    // ── linear interpolation between two RGB stops ────────────────────────────
    private static int[] lerp(int[] a, int[] b, double t) {
        return new int[]{
            (int) Math.round(a[0] + (b[0] - a[0]) * t),
            (int) Math.round(a[1] + (b[1] - a[1]) * t),
            (int) Math.round(a[2] + (b[2] - a[2]) * t)
        };
    }

    /**
     * Paints a three-stop vertical gradient (top → mid → bottom).
     *
     * The midpoint lands near the vertical centre of the screen, giving every
     * theme a hollow / depth-of-field feel — edges are deep/dark, the centre
     * breathes with a richer colour — exactly like a Hollow-Knight-style game
     * backdrop. Each row carries its own explicit bgCode so no cell reverts to
     * the terminal default.
     *
     * @param top RGB at row 1
     * @param mid RGB at the vertical centre (the "glow pocket")
     * @param bottom RGB at the last row
     * @param fg foreground escape to activate after the fill
     */
    private static void fillGradient(int[] top, int[] mid, int[] bottom, String fg) {
        int h = TerminalUI.termH();
        // Paint extra rows beyond the reported height so that any
        // under-reported terminal height still gets covered.
        int rows = h + 8;

        // \u001B[2K  — Erase Entire Line — fills the FULL visible line width
        // using the current background colour, so we never need to know the
        // exact terminal width. This handles wide WezTerm / Windows Terminal
        // windows where "mode con" under-reports columns.
        StringBuilder sb = new StringBuilder(32 * rows + 64);
        sb.append("\u001B[2J\u001B[H");   // fast erase first

        for (int r = 1; r <= rows; r++) {
            // clamp t to [0,1] — extra rows get the bottom colour
            double t = Math.min(1.0, (double) (r - 1) / Math.max(h - 1, 1));
            int[] color;
            if (t <= 0.5) {
                color = lerp(top, mid, t * 2.0);
            } else {
                color = lerp(mid, bottom, (t - 0.5) * 2.0);
            }
            String bg = ConsoleColors.bgRGB(color[0], color[1], color[2]);
            // Move to start of row, set bg, then erase whole line with it.
            sb.append("\u001B[").append(r).append(";1H")
                    .append(bg).append("\u001B[2K");
        }
        sb.append("\u001B[H");  // park cursor at top-left

        System.out.print(sb);
        // Re-emit the top-row background as the "current" background so that
        // any immediately following text sits on a consistent dark surface.
        System.out.print(ConsoleColors.bgRGB(top[0], top[1], top[2]) + fg);
        System.out.flush();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  THEME IMPLEMENTATIONS  — each palette is three stops:
    //    top    = deep, shadowed edge (the "void rim")
    //    mid    = richer, slightly lifted hue (the "hollow glow")
    //    bottom = deep counterpart, may shift hue for depth
    // ══════════════════════════════════════════════════════════════════════════
    /**
     * Main menu — vivid cosmic: deep purple void with a bright indigo glow.
     */
    public static void applyMainMenuTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{8, 3, 30}, // top    – dark purple
                new int[]{30, 12, 80}, // mid    – bright indigo glow
                new int[]{5, 2, 18}, // bottom – deep void
                ConsoleColors.ThemeText.SOFT_WHITE
        );
    }

    /**
     * Student — electric blue: deep navy void with a vivid cobalt glow.
     */
    public static void applyStudentTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{2, 6, 40}, // top    – deep navy
                new int[]{10, 30, 100}, // mid    – electric blue glow
                new int[]{1, 3, 25}, // bottom – midnight
                ConsoleColors.ThemeText.STUDENT_TEXT
        );
    }

    /**
     * Attendant — vivid cyan: dark teal void with a bright cyan glow.
     */
    public static void applyAttendantTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{2, 22, 22}, // top    – dark teal
                new int[]{6, 65, 58}, // mid    – bright cyan glow
                new int[]{1, 14, 16}, // bottom – deep ocean
                ConsoleColors.ThemeText.ATTENDANT_TEXT
        );
    }

    /**
     * Maintenance — neon green: dark forest void with a vivid green glow.
     */
    public static void applyMaintenanceTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{2, 25, 8}, // top    – dark forest
                new int[]{10, 60, 25}, // mid    – neon green glow
                new int[]{1, 15, 5}, // bottom – deep green void
                ConsoleColors.ThemeText.MAINTENANCE_TEXT
        );
    }

    /**
     * Store-in-charge — bright orange: dark amber void with a vivid orange
     * glow.
     */
    public static void applyStoreInChargeTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{38, 14, 2}, // top    – dark amber
                new int[]{85, 35, 8}, // mid    – bright orange glow
                new int[]{22, 7, 1}, // bottom – deep ember
                ConsoleColors.ThemeText.STORE_TEXT
        );
    }

    /**
     * Hall office — hot pink: dark magenta void with a vivid pink glow.
     */
    public static void applyHallOfficeTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{30, 4, 25}, // top    – dark magenta
                new int[]{70, 12, 55}, // mid    – hot pink glow
                new int[]{18, 2, 15}, // bottom – deep magenta
                ConsoleColors.ThemeText.HALL_TEXT
        );
    }

    /**
     * Admin — vivid red: deep crimson void with a bright scarlet glow.
     */
    public static void applyAdminTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{42, 5, 12}, // top    – dark crimson
                new int[]{95, 14, 28}, // mid    – bright red glow
                new int[]{25, 2, 8}, // bottom – deep blood
                ConsoleColors.ThemeText.ADMIN_TEXT
        );
    }

    /**
     * Cafeteria manager — vivid yellow: dark gold void with a bright yellow
     * glow.
     */
    public static void applyCafeteriaManagerTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{38, 30, 2}, // top    – dark gold
                new int[]{82, 65, 8}, // mid    – bright yellow glow
                new int[]{22, 16, 1}, // bottom – deep amber
                ConsoleColors.ThemeText.CAFETERIA_TEXT
        );
    }

    public static void resetTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.flush();
    }
}
