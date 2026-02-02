package utils;

public final class ConsoleUtil {

    private static final String ESC = "\u001b[";

    private static final String CLEAR_SCREEN = ESC + "2J";
    private static final String CURSOR_HOME = ESC + "H";
    private static final String RESET = ESC + "0m";
    private static final String SHOW_CURSOR = ESC + "?25h";
    private static final String HIDE_CURSOR = ESC + "?25l";

    private ConsoleUtil() {}
    
    public static void clearScreen() {
        System.out.print(CURSOR_HOME + CLEAR_SCREEN);
        System.out.flush();
    }

    public static void clearAndReset() {
        System.out.print(RESET + CURSOR_HOME + CLEAR_SCREEN + SHOW_CURSOR);
        System.out.flush();
    }

    public static void resetTerminal() {
        System.out.print(RESET + SHOW_CURSOR);
        System.out.flush();
    }

    public static void hideCursor() {
        System.out.print(HIDE_CURSOR);
        System.out.flush();
    }

    public static void showCursor() {
        System.out.print(SHOW_CURSOR);
        System.out.flush();
    }
}
