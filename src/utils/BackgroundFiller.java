package utils;

public final class BackgroundFiller {

    private BackgroundFiller() {
    }

    /**
     * Fills every cell of the terminal with the current background color.
     * Uses \e[2J then overwrites with spaces carrying the active bg attribute.
     */
    private static void clearWithCurrentBackground() {
        int w = TerminalUI.termW();
        int h = TerminalUI.termH();
        // First clear using standard escape, then overwrite each row
        // with spaces that carry the current SGR background attribute.
        StringBuilder sb = new StringBuilder(w * h + h * 20 + 40);
        sb.append("\u001B[2J");                    // erase display
        sb.append("\u001B[H");                     // cursor home
        String rowSpaces = " ".repeat(w);
        for (int r = 1; r <= h; r++) {
            sb.append("\u001B[").append(r).append(";1H").append(rowSpaces);
        }
        sb.append("\u001B[H");                     // cursor home again
        System.out.print(sb);
        // Re-emit the top-row background as the "current" background so that
        // any immediately following text sits on a consistent dark surface.
        System.out.print(ConsoleColors.bgRGB(top[0], top[1], top[2]) + fg);
        System.out.flush();
    }

    public static void applyMainMenuTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(20, 14, 32));
        System.out.print(ConsoleColors.ThemeText.SOFT_WHITE);
        clearWithCurrentBackground();
    }

    public static void applyStudentTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(0, 4, 53));
        System.out.print(ConsoleColors.ThemeText.STUDENT_TEXT);
        clearWithCurrentBackground();
    }

    public static void applyAttendantTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(0, 40, 36));
        System.out.print(ConsoleColors.ThemeText.ATTENDANT_TEXT);
        clearWithCurrentBackground();
    }

    public static void applyMaintenanceTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(27, 31, 40));
        System.out.print(ConsoleColors.ThemeText.MAINTENANCE_TEXT);
        clearWithCurrentBackground();
    }

    public static void applyStoreInChargeTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(43, 20, 0));
        System.out.print(ConsoleColors.ThemeText.STORE_TEXT);
        clearWithCurrentBackground();
    }

    public static void applyHallOfficeTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(31, 24, 38));
        System.out.print(ConsoleColors.ThemeText.HALL_TEXT);
        clearWithCurrentBackground();
    }

    public static void applyAdminTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(61, 0, 8));
        System.out.print(ConsoleColors.ThemeText.ADMIN_TEXT);
        clearWithCurrentBackground();
    }

    public static void applyCafeteriaManagerTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(45, 25, 10));
        System.out.print(ConsoleColors.ThemeText.CAFETERIA_TEXT);
        clearWithCurrentBackground();
    }

    public static void resetTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.flush();
    }
}
