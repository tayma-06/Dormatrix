package utils;

import java.io.IOException;

public final class ConsoleUtil {

    private static final String ESC = "\u001b[";
    private static final String CLEAR_SCREEN = ESC + "2J";
    private static final String CURSOR_HOME = ESC + "H";
    private static final String RESET = ESC + "0m";
    private static final String SHOW_CURSOR = ESC + "?25h";
    private static final String HIDE_CURSOR = ESC + "?25l";

    private ConsoleUtil() {
    }

    public static void clearScreen() {
        clearTerminal();
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

    public static void pause() {
        System.out.println();
        System.out.print("Press Enter to continue...");
        FastInput.readLine();
    }

    private static void clearTerminal() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to clear terminal.");
        }
    }
}
