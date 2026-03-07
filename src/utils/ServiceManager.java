package utils;

import controllers.food.TokenPurchaseService;

public class ServiceManager {

    private static TokenPurchaseService tokenService;
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) {
            return;
        }

        tokenService = TokenPurchaseService.getInstance();
        tokenService.start();

        initialized = true;
    }

    public static void shutdown() {
        if (tokenService != null) {
            tokenService.shutdown();
        }
    }
}
