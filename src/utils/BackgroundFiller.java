package utils;

public final class BackgroundFiller {

    private static final int DEFAULT_ROWS = 40;
    private static final int DEFAULT_COLS = 140;

    private BackgroundFiller() {}

    private static void fillWithCurrentBackground() {
        System.out.print("\u001B[H");
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < DEFAULT_COLS; i++) {
            line.append(' ');
        }
        for (int r = 0; r < DEFAULT_ROWS; r++) {
            System.out.print(line);
            System.out.print('\n');
        }
        System.out.print("\u001B[H");
    }

    public static void applyMainMenuTheme() {
        System.out.print(ConsoleColors.bgRGB(20, 14, 32));
        System.out.print(ConsoleColors.ThemeText.SOFT_WHITE);
        fillWithCurrentBackground();
    }

    public static void applyStudentTheme() {
        System.out.print(ConsoleColors.bgRGB(0, 4, 53));
        System.out.print(ConsoleColors.ThemeText.STUDENT_TEXT);
        fillWithCurrentBackground();
    }

    public static void applyAttendantTheme() {
        System.out.print(ConsoleColors.bgRGB(0, 40, 36));
        System.out.print(ConsoleColors.ThemeText.ATTENDANT_TEXT);
        fillWithCurrentBackground();
    }

    public static void applyMaintenanceTheme() {
        System.out.print(ConsoleColors.bgRGB(27, 31, 40));
        System.out.print(ConsoleColors.ThemeText.MAINTENANCE_TEXT);
        fillWithCurrentBackground();
    }

    public static void applyStoreInChargeTheme() {
        System.out.print(ConsoleColors.bgRGB(43, 20, 0));
        System.out.print(ConsoleColors.ThemeText.STORE_TEXT);
        fillWithCurrentBackground();
    }

    public static void applyHallOfficeTheme() {
        System.out.print(ConsoleColors.bgRGB(31, 24, 38));
        System.out.print(ConsoleColors.ThemeText.HALL_TEXT);
        fillWithCurrentBackground();
    }

    public static void applyAdminTheme() {
        System.out.print(ConsoleColors.bgRGB(61, 0, 8));
        System.out.print(ConsoleColors.ThemeText.ADMIN_TEXT);
        fillWithCurrentBackground();
    }

    public static void resetTheme() {
        System.out.print(ConsoleColors.RESET);
    }
}
