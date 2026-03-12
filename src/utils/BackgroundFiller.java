package utils;

public final class BackgroundFiller {

    private BackgroundFiller() {
    }

    public record Theme(
            String box,
            String text,
            int[] topBg,
            int[] midBg,
            int[] bottomBg,
            String canvasBg,
            String panelBg,
            String inputBg
    ) {
    }

    public static final Theme MAIN = new Theme(
            ConsoleColors.Accent.BOX,
            ConsoleColors.ThemeText.SOFT_WHITE,
            new int[]{12, 8, 22},
            new int[]{22, 14, 38},
            new int[]{10, 7, 18},
            ConsoleColors.bgRGB(20, 14, 32),
            ConsoleColors.bgRGB(16, 11, 30),
            ConsoleColors.bgRGB(24, 18, 42)
    );

    public static final Theme STUDENT = new Theme(
            ConsoleColors.fgRGB(60, 140, 255),
            ConsoleColors.ThemeText.STUDENT_TEXT,
            new int[]{0, 3, 32},
            new int[]{0, 8, 56},
            new int[]{0, 3, 26},
            ConsoleColors.bgRGB(0, 6, 45),
            ConsoleColors.bgRGB(8, 14, 62),
            ConsoleColors.bgRGB(14, 22, 78)
    );

    public static final Theme ATTENDANT = new Theme(
            ConsoleColors.fgRGB(40, 220, 210),
            ConsoleColors.ThemeText.ATTENDANT_TEXT,
            new int[]{0, 18, 16},
            new int[]{0, 34, 30},
            new int[]{0, 14, 12},
            ConsoleColors.bgRGB(0, 28, 26),
            ConsoleColors.bgRGB(0, 40, 36),
            ConsoleColors.bgRGB(0, 52, 46)
    );

    public static final Theme MAINTENANCE = new Theme(
            ConsoleColors.fgRGB(170, 190, 210),
            ConsoleColors.ThemeText.MAINTENANCE_TEXT,
            new int[]{18, 20, 26},
            new int[]{30, 34, 44},
            new int[]{16, 18, 24},
            ConsoleColors.bgRGB(27, 31, 40),
            ConsoleColors.bgRGB(34, 39, 50),
            ConsoleColors.bgRGB(44, 50, 64)
    );

    public static final Theme STORE = new Theme(
            ConsoleColors.fgRGB(255, 150, 40),
            ConsoleColors.ThemeText.STORE_TEXT,
            new int[]{26, 10, 0},
            new int[]{46, 18, 0},
            new int[]{20, 8, 0},
            ConsoleColors.bgRGB(40, 16, 0),
            ConsoleColors.bgRGB(50, 22, 0),
            ConsoleColors.bgRGB(64, 30, 0)
    );

    public static final Theme HALL = new Theme(
            ConsoleColors.fgRGB(255, 80, 190),
            ConsoleColors.ThemeText.HALL_TEXT,
            new int[]{20, 0, 15},
            new int[]{40, 0, 30},
            new int[]{16, 0, 12},
            ConsoleColors.bgRGB(35, 0, 25),
            ConsoleColors.bgRGB(46, 0, 34),
            ConsoleColors.bgRGB(58, 0, 44)
    );

    public static final Theme ADMIN = new Theme(
            ConsoleColors.fgRGB(255, 60, 60),
            ConsoleColors.ThemeText.ADMIN_TEXT,
            new int[]{24, 0, 3},
            new int[]{54, 0, 8},
            new int[]{18, 0, 2},
            ConsoleColors.bgRGB(48, 0, 5),
            ConsoleColors.bgRGB(61, 0, 8),
            ConsoleColors.bgRGB(76, 0, 12)
    );

    public static final Theme CAFETERIA = new Theme(
            ConsoleColors.fgRGB(255, 210, 30),
            ConsoleColors.ThemeText.CAFETERIA_TEXT,
            new int[]{22, 14, 0},
            new int[]{42, 28, 0},
            new int[]{18, 12, 0},
            ConsoleColors.bgRGB(35, 28, 0),
            ConsoleColors.bgRGB(48, 36, 0),
            ConsoleColors.bgRGB(62, 48, 0)
    );

    public static void applyTheme(Theme theme) {
        System.out.print(ConsoleColors.RESET);
        TerminalUI.paintScreenGradient(
                theme.topBg(),
                theme.midBg(),
                theme.bottomBg(),
                theme.text()
        );
        System.out.flush();
    }

    public static void applyMainMenuTheme() {
        applyTheme(MAIN);
    }

    public static void applyStudentTheme() {
        applyTheme(STUDENT);
    }

    public static void applyAttendantTheme() {
        applyTheme(ATTENDANT);
    }

    public static void applyMaintenanceTheme() {
        applyTheme(MAINTENANCE);
    }

    public static void applyStoreInChargeTheme() {
        applyTheme(STORE);
    }

    public static void applyHallOfficeTheme() {
        applyTheme(HALL);
    }

    public static void applyAdminTheme() {
        applyTheme(ADMIN);
    }

    public static void applyCafeteriaManagerTheme() {
        applyTheme(CAFETERIA);
    }

    public static void resetTheme() {
        System.out.print(ConsoleColors.RESET);
        System.out.flush();
    }
}