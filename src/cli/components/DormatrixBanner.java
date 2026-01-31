package cli.components;

import utils.ConsoleColors;

public class DormatrixBanner
{
    public void printBanner()
    {
        System.out.println(
                ConsoleColors.BRIGHT_PURPLE +
                        "██████   ██████  ██████  ███    ███  █████  ████████ ██████  ██ ██   ██\n" +
                        "██   ██ ██    ██ ██   ██ ████  ████ ██   ██    ██    ██   ██ ██  ██ ██\n" +
                        "██   ██ ██    ██ ██████  ██ ████ ██ ███████    ██    ██████  ██   ███ \n" +
                        "██   ██ ██    ██ ██   ██ ██  ██  ██ ██   ██    ██    ██   ██ ██  ██ ██ \n" +
                        "██████   ██████  ██   ██ ██      ██ ██   ██    ██    ██   ██ ██ ██   ██" +
                        ConsoleColors.RESET
        );
    }
}
