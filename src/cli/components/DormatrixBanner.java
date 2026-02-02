package cli.components;

import utils.ConsoleColors;

public class DormatrixBanner {

    public void printBanner() {
        printBanner(true);
    }

    public void printBannerOnTheme() {
        printBanner(false);
    }

    private void printBanner(boolean reset) {
        String r = reset ? ConsoleColors.RESET : "";

        System.out.println(
                ConsoleColors.Accent.BANNER +
                        "██████   ██████  ██████  ███    ███  █████  ████████ ██████  ██ ██   ██\n" +
                        "██   ██ ██    ██ ██   ██ ████  ████ ██   ██    ██    ██   ██ ██  ██ ██\n" +
                        "██   ██ ██    ██ ██████  ██ ████ ██ ███████    ██    ██████  ██   ███ \n" +
                        "██   ██ ██    ██ ██   ██ ██  ██  ██ ██   ██    ██    ██   ██ ██  ██ ██ \n" +
                        "██████   ██████  ██   ██ ██      ██ ██   ██    ██    ██   ██ ██ ██   ██" +
                        r
        );
    }
}
