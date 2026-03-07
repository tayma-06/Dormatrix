package utils;

public final class BackgroundFiller {

    private BackgroundFiller() {}

    private static void clearWithCurrentBackground() {
        System.out.print("\u001B[2J\u001B[H");
        System.out.flush();
    }

    private static void apply(String bg, String fg) {
        System.out.print(ConsoleColors.RESET);
        System.out.print(bg);
        System.out.print(fg);
        clearWithCurrentBackground();
    }

    public static void applyMainMenuTheme() {
        apply(ConsoleColors.bgRGB(20, 14, 32),  ConsoleColors.ThemeText.SOFT_WHITE);
    }

    public static void applyStudentTheme() {
        apply(ConsoleColors.bgRGB(0, 4, 53),    ConsoleColors.ThemeText.STUDENT_TEXT);
    }

    public static void applyAttendantTheme() {
        apply(ConsoleColors.bgRGB(0, 40, 36),   ConsoleColors.ThemeText.ATTENDANT_TEXT);
    }

    public static void applyMaintenanceTheme() {
        apply(ConsoleColors.bgRGB(27, 31, 40),  ConsoleColors.ThemeText.MAINTENANCE_TEXT);
    }

    public static void applyStoreInChargeTheme() {
        apply(ConsoleColors.bgRGB(43, 20, 0),   ConsoleColors.ThemeText.STORE_TEXT);
    }

    public static void applyHallOfficeTheme() {
        apply(ConsoleColors.bgRGB(31, 24, 38),  ConsoleColors.ThemeText.HALL_TEXT);
    }

    public static void applyAdminTheme() {
        apply(ConsoleColors.bgRGB(61, 0, 8),    ConsoleColors.ThemeText.ADMIN_TEXT);
    }

    public static void applyCafeteriaManagerTheme() {
        apply(ConsoleColors.bgRGB(45, 25, 10),  ConsoleColors.ThemeText.CAFETERIA_TEXT);
    }

    public static void resetTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.flush();
    }
}