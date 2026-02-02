package utils;

public final class ConsoleUtil {
    private static final String ANSI_CLEAR_SCREEN = "\u001b[H\u001b[2J";

    private ConsoleUtil() {}

    public static void clearScreen() {
        System.out.print(ANSI_CLEAR_SCREEN);
        System.out.flush();
    }
}
