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
        TerminalUI.tPause();
    }

    public static String[] wrapText(String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return new String[]{""};
        }

        if (text.length() <= maxWidth) {
            return new String[]{text};
        }

        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxWidth) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                if (word.length() > maxWidth) {
                    while (word.length() > maxWidth) {
                        lines.add(word.substring(0, maxWidth));
                        word = word.substring(maxWidth);
                    }
                    if (!word.isEmpty()) {
                        currentLine.append(word);
                    }
                } else {
                    currentLine.append(word);
                }
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines.toArray(new String[0]);
    }

    public static void printWrappedInBox(String text, int width) {
        String[] lines = wrapText(text, width);
        for (String line : lines) {
            System.out.printf("║ %-" + width + "s ║%n", line);
        }
    }

    private static void clearTerminal() {
        try {
            System.out.print("\u001B[3J\u001B[2J\u001B[H");
            System.out.flush();
        } catch (Exception e) {
            System.out.println("Failed to clear terminal.");
        }
    }
}
