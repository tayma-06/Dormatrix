package controllers.authentication;

import libraries.collections.MyString;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ConfigLoader {

    private static final String CONFIG_PATH = "config" + File.separator + "admin.config";

    public static MyString getAdminUsername() {
        return readConfigValue("ADMIN_USERNAME");
    }

    public static MyString getAdminPassword() {
        return readConfigValue("ADMIN_PASSWORD");
    }

    public static MyString getAdminName() {
        return readConfigValue("ADMIN_NAME");
    }

    public static MyString getAdminPhone() {
        return readConfigValue("ADMIN_PHONE");
    }

    public static MyString getAdminEmail() {
        return readConfigValue("ADMIN_EMAIL");
    }

    private static MyString readConfigValue(String keyToFind) {
        File configFile = new File(CONFIG_PATH);
        if (!configFile.exists()) {
            System.err.println("CRITICAL ERROR: Config file not found at " + configFile.getAbsolutePath());
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    if (key.equals(keyToFind)) {
                        return new MyString(value);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading config: " + e.getMessage());
        }

        System.err.println("Warning: Key '" + keyToFind + "' not found in config file.");
        return null;
    }
}
