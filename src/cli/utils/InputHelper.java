package cli.utils;

import libraries.collections.MyString;
import java.io.Console;
import java.util.Scanner;

public class InputHelper {
    public static MyString readPassword(Scanner scanner) {
        Console console = System.console();
        if (console != null) {
            char[] passwordArray = console.readPassword();
            return new MyString(new String(passwordArray));
        } else {
            System.out.print(" [IDE detected: Password will be visible] ");
            return new MyString(scanner.nextLine().trim());
        }
    }
}