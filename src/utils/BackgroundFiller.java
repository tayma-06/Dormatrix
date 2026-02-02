package utils;

public final class BackgroundFiller {

    private BackgroundFiller() {}

    private static void clearWithCurrentBackground() {
        System.out.print("\u001B[2J\u001B[H");
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

    public static void resetTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.flush();
    }
}
