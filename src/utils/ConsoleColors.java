package utils;

public final class ConsoleColors {

    private ConsoleColors() {}

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";

    public static final String FG_BLACK = "\u001B[30m";
    public static final String FG_RED = "\u001B[31m";
    public static final String FG_GREEN = "\u001B[32m";
    public static final String FG_YELLOW = "\u001B[33m";
    public static final String FG_BLUE = "\u001B[34m";
    public static final String FG_PURPLE = "\u001B[35m";
    public static final String FG_CYAN = "\u001B[36m";
    public static final String FG_WHITE = "\u001B[37m";

    public static final String FG_BRIGHT_BLACK = "\u001B[90m";
    public static final String FG_BRIGHT_RED = "\u001B[91m";
    public static final String FG_BRIGHT_GREEN = "\u001B[92m";
    public static final String FG_BRIGHT_YELLOW = "\u001B[93m";
    public static final String FG_BRIGHT_BLUE = "\u001B[94m";
    public static final String FG_BRIGHT_PURPLE = "\u001B[95m";
    public static final String FG_BRIGHT_CYAN = "\u001B[96m";
    public static final String FG_BRIGHT_WHITE = "\u001B[97m";

    public static String fg256(int index) {
        return "\u001B[38;5;" + index + "m";
    }

    public static String bg256(int index) {
        return "\u001B[48;5;" + index + "m";
    }

    public static String fgRGB(int r, int g, int b) {
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
    }

    public static String bgRGB(int r, int g, int b) {
        return "\u001B[48;2;" + r + ";" + g + ";" + b + "m";
    }

    public static final class ThemeText {
        public static final String SOFT_WHITE = fgRGB(236, 232, 245);
        public static final String STUDENT_TEXT = fgRGB(232, 241, 255);
        public static final String ATTENDANT_TEXT = fgRGB(232, 255, 244);
        public static final String MAINTENANCE_TEXT = fgRGB(233, 237, 247);
        public static final String STORE_TEXT = fgRGB(255, 239, 226);
        public static final String HALL_TEXT = fgRGB(238, 230, 248);
        public static final String ADMIN_TEXT = fgRGB(255, 232, 242);
        public static final String CAFETERIA_TEXT = fgRGB(255, 243, 230);

        private ThemeText() {}
    }

    public static final class Accent {
        public static final String BOX = fgRGB(206, 180, 214);
        public static final String EXIT = fgRGB(194, 140, 170);
        public static final String INPUT = fgRGB(150, 210, 220);
        public static final String BANNER = fgRGB(150, 210, 220);

        private Accent() {}
    }
}