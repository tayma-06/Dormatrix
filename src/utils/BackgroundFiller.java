package utils;

public final class BackgroundFiller {

    private BackgroundFiller() {
    }

    /**
     * Fills every cell of the terminal with the current background color. Uses
     * \e[2J then overwrites with spaces carrying the active bg attribute.
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
        System.out.flush();
    }

    public static void applyMainMenuTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(18, 12, 45));
        System.out.print(ConsoleColors.ThemeText.SOFT_WHITE);
        clearWithCurrentBackground();
    }

    public static void applyStudentTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(8, 18, 60));
        System.out.print(ConsoleColors.ThemeText.STUDENT_TEXT);
        clearWithCurrentBackground();
    }

    public static void applyAttendantTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(5, 45, 42));
        System.out.print(ConsoleColors.ThemeText.ATTENDANT_TEXT);
        clearWithCurrentBackground();
    }

    public static void applyMaintenanceTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(15, 20, 60));
        System.out.print(ConsoleColors.ThemeText.MAINTENANCE_TEXT);
        clearWithCurrentBackground();
    }

    public static void applyStoreInChargeTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(50, 28, 5));
        System.out.print(ConsoleColors.ThemeText.STORE_TEXT);
        clearWithCurrentBackground();
    }

    public static void applyHallOfficeTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(28, 12, 65));
        System.out.print(ConsoleColors.ThemeText.HALL_TEXT);
        clearWithCurrentBackground();
    }

    public static void applyAdminTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(55, 8, 22));
        System.out.print(ConsoleColors.ThemeText.ADMIN_TEXT);
        clearWithCurrentBackground();
    }

    public static void applyCafeteriaManagerTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.print(ConsoleColors.bgRGB(48, 38, 5));
        System.out.print(ConsoleColors.ThemeText.CAFETERIA_TEXT);
        clearWithCurrentBackground();
    }

    public static void resetTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.flush();
    }
}
