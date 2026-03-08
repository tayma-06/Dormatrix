package utils;

public final class ConsoleColors {

    private ConsoleColors() {
    }
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String BLINK = "\u001B[5m";
    public static final String FG_BLACK = fgRGB(18, 18, 18);
    public static final String FG_RED = fgRGB(220, 60, 60);
    public static final String FG_GREEN = fgRGB(80, 200, 120);
    public static final String FG_YELLOW = fgRGB(255, 210, 80);
    public static final String FG_BLUE = fgRGB(80, 140, 240);
    public static final String FG_PURPLE = fgRGB(180, 100, 240);
    public static final String FG_CYAN = fgRGB(80, 220, 210);
    public static final String FG_WHITE = fgRGB(220, 215, 240);
    public static final String FG_BRIGHT_BLACK = fgRGB(80, 80, 80);
    public static final String FG_BRIGHT_RED = fgRGB(255, 100, 100);
    public static final String FG_BRIGHT_GREEN = fgRGB(120, 240, 160);
    public static final String FG_BRIGHT_YELLOW = fgRGB(255, 230, 120);
    public static final String FG_BRIGHT_BLUE = fgRGB(120, 180, 255);
    public static final String FG_BRIGHT_PURPLE = fgRGB(210, 150, 255);
    public static final String FG_BRIGHT_CYAN = fgRGB(130, 240, 240);
    public static final String FG_BRIGHT_WHITE = fgRGB(255, 255, 255);

    public static String fgRGB(int r, int g, int b) {
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
    }

    public static String bgRGB(int r, int g, int b) {
        return "\u001B[48;2;" + r + ";" + g + ";" + b + "m";
    }

    public static String fg256(int index) {
        return "\u001B[38;5;" + index + "m";
    }

    public static String bg256(int index) {
        return "\u001B[48;5;" + index + "m";
    }

    public static final class ThemeText {

        private ThemeText() {
        }

        public static final String SOFT_WHITE = fgRGB(220, 200, 255);  // bright lavender (main menu)
        public static final String STUDENT_TEXT = fgRGB(120, 200, 255);  // electric azure
        public static final String ATTENDANT_TEXT = fgRGB(80, 245, 225);  // vivid cyan
        public static final String MAINTENANCE_TEXT = fgRGB(110, 255, 150);  // neon green
        public static final String STORE_TEXT = fgRGB(255, 190, 90);  // bright orange
        public static final String HALL_TEXT = fgRGB(255, 150, 210);  // hot pink
        public static final String ADMIN_TEXT = fgRGB(255, 130, 130);  // bright red
        public static final String CAFETERIA_TEXT = fgRGB(255, 240, 90);  // vivid yellow
    }

    public static final class Accent {

        private Accent() {
        }

        public static final String BOX = fgRGB(170, 120, 255);  // bright purple (box borders, main menu default)
        public static final String EXIT = fgRGB(240, 100, 135);  // bright rose
        public static final String INPUT = fgRGB(80, 215, 205);  // bright teal
        public static final String BANNER = fgRGB(80, 215, 205);  // bright teal
        public static final String SUCCESS = fgRGB(80, 220, 140);  // bright green
        public static final String WARNING = fgRGB(245, 190, 50);  // bright amber
        public static final String ERROR = fgRGB(255, 90, 90);  // bright red
        public static final String MUTED = fgRGB(165, 150, 200);  // light lavender-grey
        public static final String HIGHLIGHT = fgRGB(255, 215, 75);  // bright gold
    }
}
