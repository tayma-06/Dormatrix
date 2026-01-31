package utils;

import libraries.collections.MyString;
import java.io.Console;

public class InputHelper {

    public static MyString readPassword() {
        Console console = System.console();
        if (console != null) {
            char[] passwordArray = console.readPassword();
            if (passwordArray == null) {
                return new MyString("");
            }
            return new MyString(new String(passwordArray).trim());
        }
        return new MyString(FastInput.readNonEmptyLine());
    }
}
