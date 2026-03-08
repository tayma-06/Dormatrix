package utils;

public final class BackgroundFiller {

    private BackgroundFiller() {
    }

    /**
     * Paints every terminal cell with the given background color by explicitly
     * prepending the bgCode to every row. This is more reliable than relying on
     * the SGR state being intact when \e[2J fires — each row carries its own
     * explicit background attribute so no cell is left at the terminal default.
     */
    private static void fillBackground(String bgCode) {
        int w = TerminalUI.termW();
        int h = TerminalUI.termH();
        // Each row explicitly re-emits bgCode so Windows / WezTerm cannot
        // fall back to the terminal-default (white) background for any cell.
        String rowPaint = bgCode + " ".repeat(w);
        StringBuilder sb = new StringBuilder((bgCode.length() + w + 20) * h + 40);
        sb.append("\u001B[2J\u001B[H");           // fast erase first
        for (int r = 1; r <= h; r++) {
            sb.append("\u001B[").append(r).append(";1H").append(rowPaint);
        }
        sb.append("\u001B[H");
        System.out.print(sb);
        System.out.flush();
    }

    public static void applyMainMenuTheme() {
        String bg = ConsoleColors.bgRGB(18, 12, 45);
        System.out.print(ConsoleColors.RESET + bg + ConsoleColors.ThemeText.SOFT_WHITE);
        System.out.flush();
        fillBackground(bg);
    }

    public static void applyStudentTheme() {
        String bg = ConsoleColors.bgRGB(8, 18, 60);
        System.out.print(ConsoleColors.RESET + bg + ConsoleColors.ThemeText.STUDENT_TEXT);
        System.out.flush();
        fillBackground(bg);
    }

    public static void applyAttendantTheme() {
        String bg = ConsoleColors.bgRGB(5, 45, 42);
        System.out.print(ConsoleColors.RESET + bg + ConsoleColors.ThemeText.ATTENDANT_TEXT);
        System.out.flush();
        fillBackground(bg);
    }

    public static void applyMaintenanceTheme() {
        String bg = ConsoleColors.bgRGB(15, 20, 60);
        System.out.print(ConsoleColors.RESET + bg + ConsoleColors.ThemeText.MAINTENANCE_TEXT);
        System.out.flush();
        fillBackground(bg);
    }

    public static void applyStoreInChargeTheme() {
        String bg = ConsoleColors.bgRGB(50, 28, 5);
        System.out.print(ConsoleColors.RESET + bg + ConsoleColors.ThemeText.STORE_TEXT);
        System.out.flush();
        fillBackground(bg);
    }

    public static void applyHallOfficeTheme() {
        String bg = ConsoleColors.bgRGB(28, 12, 65);
        System.out.print(ConsoleColors.RESET + bg + ConsoleColors.ThemeText.HALL_TEXT);
        System.out.flush();
        fillBackground(bg);
    }

    public static void applyAdminTheme() {
        String bg = ConsoleColors.bgRGB(55, 8, 22);
        System.out.print(ConsoleColors.RESET + bg + ConsoleColors.ThemeText.ADMIN_TEXT);
        System.out.flush();
        fillBackground(bg);
    }

    public static void applyCafeteriaManagerTheme() {
        String bg = ConsoleColors.bgRGB(48, 38, 5);
        System.out.print(ConsoleColors.RESET + bg + ConsoleColors.ThemeText.CAFETERIA_TEXT);
        System.out.flush();
        fillBackground(bg);
    }

    public static void resetTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.flush();
    }
}
