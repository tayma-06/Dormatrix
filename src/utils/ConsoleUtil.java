package utils;

public final class ConsoleUtil {
    private ConsoleUtil() {}
    public static void clearScreen() {
        try {
            new ProcessBuilder("cmd", "/c", "cls")
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
}
