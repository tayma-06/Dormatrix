package utils;

public final class ConsoleColors {

    private ConsoleColors() {}

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";

    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_PURPLE = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    public static final String PINK = "\u001B[38;5;206m";
    public static final String LIGHT_PINK = "\u001B[38;5;217m";
    public static final String HOT_PINK = "\u001B[38;5;198m";
    public static final String MAGENTA = "\u001B[38;5;201m";
    public static final String ORANGE = "\u001B[38;5;208m";
    public static final String GOLD = "\u001B[38;5;220m";
    public static final String SKY_BLUE = "\u001B[38;5;117m";
    public static final String LIME = "\u001B[38;5;118m";
    public static final String TEAL = "\u001B[38;5;30m";
    public static final String GREY = "\u001B[38;5;244m";
    public static final String BABY_PINK = "\u001B[38;2;244;194;194m";

    public static String color256(int i) {
        return "\u001B[38;5;" + i + "m";
    }

    public static String colorRGB(int r, int g, int b) {
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
    }

    public static String bg256(int i) {
        return "\u001B[48;5;" + i + "m";
    }
}