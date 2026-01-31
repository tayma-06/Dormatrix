package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class FastInput {
    private static final BufferedReader br =
            new BufferedReader(new InputStreamReader(System.in));
    private FastInput() {}
    public static String readLine() {
        try {
            return br.readLine();
        } catch (IOException e) {
            return "";
        }
    }
    public static int readInt() {
        while (true) {
            try {
                String line = readLine();
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Try again: ");
            }
        }
    }
    public static String readNonEmptyLine() {
        while (true) {
            String line = readLine();
            if (line != null && !line.trim().isEmpty()) {
                return line.trim();
            }
            System.out.print("Input cannot be empty. Try again: ");
        }
    }
}
