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
            String s = br.readLine();
            return (s == null) ? "" : s;
        } catch (IOException e) {
            return "";
        }
    }

    public static String readNonEmptyLine() {
        while (true) {
            String line = readLine().trim();
            if (!line.isEmpty()) return line;
            System.out.print("Input cannot be empty. Try again: ");
        }
    }

    public static int readInt() {
        while (true) {
            String line = readLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Try again: ");
            }
        }
    }

    public static double readDouble() {
        while (true) {
            String line = readLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Try again: ");
            }
        }
    }

    public static boolean readBoolean() {
        while (true) {
            String s = readLine().trim().toLowerCase();

            if (s.equals("true") || s.equals("t") || s.equals("yes") || s.equals("y") || s.equals("1"))
                return true;

            if (s.equals("false") || s.equals("f") || s.equals("no") || s.equals("n") || s.equals("0"))
                return false;

            System.out.print("Invalid input. Enter true/false (or yes/no): ");
        }
    }
}
