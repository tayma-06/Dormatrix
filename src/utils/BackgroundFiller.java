package utils;

public final class BackgroundFiller {

    private BackgroundFiller() {
    }

    /**
     * Fills every row of the terminal with a vertical gradient between three
     * color stops (top → mid → bottom). Each row gets its own bg-RGB value.
     */
    private static void fillGradient(int[] top, int[] mid, int[] bot, String fgColor) {
        int w = TerminalUI.termW();
        int h = TerminalUI.termH();
        if (h < 1) {
            h = 30;
        }

        StringBuilder sb = new StringBuilder(w * h + h * 40 + 60);
        sb.append("\u001B[2J");   // erase display
        sb.append("\u001B[H");    // cursor home
        sb.append(fgColor);

        String rowSpaces = " ".repeat(w);
        int half = Math.max(h / 2, 1);

        for (int r = 1; r <= h; r++) {
            int[] color;
            if (r <= half) {
                // top → mid
                color = lerp(top, mid, (double) (r - 1) / (half - 1 == 0 ? 1 : half - 1));
            } else {
                // mid → bot
                color = lerp(mid, bot, (double) (r - half - 1) / Math.max(h - half - 1, 1));
            }
            sb.append("\u001B[").append(r).append(";1H");
            sb.append(ConsoleColors.bgRGB(color[0], color[1], color[2]));
            sb.append(rowSpaces);
        }
        sb.append("\u001B[H");    // cursor home again
        System.out.print(sb);
        System.out.flush();
    }

    private static int[] lerp(int[] a, int[] b, double t) {
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

    // ── Role themes ──
    public static void applyMainMenuTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{15, 10, 40}, // top: deep purple
                new int[]{30, 22, 60}, // mid: lighter purple
                new int[]{12, 8, 28}, // bot: dark purple
                ConsoleColors.ThemeText.SOFT_WHITE
        );
    }

    public static void applyStudentTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{0, 2, 40}, // top: very dark blue
                new int[]{10, 18, 72}, // mid: brighter navy
                new int[]{0, 4, 35}, // bot: dark blue
                ConsoleColors.ThemeText.STUDENT_TEXT
        );
    }

    public static void applyAttendantTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{0, 28, 26}, // top: dark teal
                new int[]{8, 55, 50}, // mid: brighter teal
                new int[]{0, 22, 20}, // bot: deep teal
                ConsoleColors.ThemeText.ATTENDANT_TEXT
        );
    }

    public static void applyMaintenanceTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{18, 22, 32}, // top: dark steel
                new int[]{35, 42, 55}, // mid: lighter steel
                new int[]{16, 18, 28}, // bot: deep steel
                ConsoleColors.ThemeText.MAINTENANCE_TEXT
        );
    }

    public static void applyStoreInChargeTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{35, 14, 0}, // top: dark amber
                new int[]{58, 30, 6}, // mid: brighter amber
                new int[]{28, 10, 0}, // bot: deep amber
                ConsoleColors.ThemeText.STORE_TEXT
        );
    }

    public static void applyHallOfficeTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{22, 16, 30}, // top: dark plum
                new int[]{42, 32, 52}, // mid: brighter plum
                new int[]{18, 12, 24}, // bot: deep plum
                ConsoleColors.ThemeText.HALL_TEXT
        );
    }

    public static void applyAdminTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{48, 0, 6}, // top: dark crimson
                new int[]{78, 8, 18}, // mid: brighter crimson
                new int[]{38, 0, 4}, // bot: deep crimson
                ConsoleColors.ThemeText.ADMIN_TEXT
        );
    }

    public static void applyCafeteriaManagerTheme() {
        System.out.print(ConsoleColors.RESET);
        fillGradient(
                new int[]{36, 18, 6}, // top: dark brown
                new int[]{60, 34, 14}, // mid: brighter brown
                new int[]{30, 14, 4}, // bot: deep brown
                ConsoleColors.ThemeText.CAFETERIA_TEXT
        );
    }

    public static void resetTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.flush();
    }
}
