package utils;

import models.food.MealType;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class CafeteriaAsciiUI {

    // ─────────────────────────────────────────────────────────────
    //  Live animated progress bar — Spotify-style pulsing playhead
    // ─────────────────────────────────────────────────────────────
    private static volatile boolean barRunning = false;
    private static Thread barThread = null;

    /**
     * Starts the live pulsing bar animation on a daemon thread.
     * Also repaints the nowLine row in real time.
     *
     * @param barRow     1-based terminal row where the bar line lives
     * @param nowRow     1-based terminal row where the date/time/slot line lives
     * @param boxLeftCol 1-based column of the box left border ║
     * @param iw         inner width of the box (boxW - 2)
     * @param slot       current meal slot
     */
    public static void startBarAnimation(int barRow, int nowRow, int boxLeftCol, int iw, MealType slot) {
        stopBarAnimation();
        barRunning = true;
        barThread = new Thread(() -> {
            int tick = 0;
            while (barRunning) {
                try {
                    System.out.print(SAVE_CUR);
                    paintNowLine(nowRow, boxLeftCol, iw);
                    paintBar(barRow, boxLeftCol, iw, slot, tick);
                    System.out.print(RESTORE_CUR);
                    System.out.flush();
                    tick = (tick + 1) % 40;
                    Thread.sleep(120);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception ignored) {
                }
            }
        }, "cafeteria-bar");
        barThread.setDaemon(true);
        barThread.start();
    }

    public static void stopBarAnimation() {
        barRunning = false;
        if (barThread != null) {
            try {
                barThread.join(400);
            } catch (Exception ignored) {
            }
            barThread = null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Token-screen live animation (date/time, status, progress bar)
    //  Uses save/restore cursor so typed input stays in place.
    // ─────────────────────────────────────────────────────────────
    private static final String SAVE_CUR    = "\u001B7";
    private static final String RESTORE_CUR = "\u001B8";
    private static final int DASH_W  = 71;  // mirrors TerminalUI.DASH_W
    private static final int DASH_CW = 67;  // DASH_IW - 2

    private static volatile boolean tokenAnimRunning = false;
    private static Thread tokenAnimThread = null;

    /**
     * Starts live animation for the token-purchase screen.
     * Repaints dateTimeLine, statusLine, and progress bar every 120 ms
     * while the user is at the input prompt.
     *
     * @param dateRow 1-based terminal row of the "Date: ..." line
     */
    public static void startTokenScreenAnimation(int dateRow) {
        stopTokenScreenAnimation();
        tokenAnimRunning = true;
        tokenAnimThread = new Thread(() -> {
            int tick = 0;
            while (tokenAnimRunning) {
                try {
                    MealType liveSlot = TimeManager.getCurrentMealSlot();
                    String dateLine = "Date: " + TimeManager.nowDate()
                            + " | Time: " + TimeManager.nowTime()
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    String statusLine = "Current Status: "
                            + (liveSlot == MealType.NONE ? "CLOSED" : "ACTIVE - " + liveSlot);

                    System.out.print(SAVE_CUR);
                    paintTBoxTextLine(dateRow,     dateLine,   TerminalUI.getActiveTextColor());
                    paintTBoxTextLine(dateRow + 1, statusLine, TerminalUI.getActiveTextColor());
                    paintTBoxBarLine (dateRow + 2, liveSlot, tick);
                    System.out.print(RESTORE_CUR);
                    System.out.flush();

                    tick = (tick + 1) % 40;
                    Thread.sleep(120);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception ignored) {
                }
            }
        }, "token-anim");
        tokenAnimThread.setDaemon(true);
        tokenAnimThread.start();
    }

    public static void stopTokenScreenAnimation() {
        tokenAnimRunning = false;
        if (tokenAnimThread != null) {
            try {
                tokenAnimThread.join(400);
            } catch (Exception ignored) {
            }
            tokenAnimThread = null;
        }
    }

    private static void paintTBoxTextLine(int termRow, String text, String textColor) {
        String boxColor = TerminalUI.getActiveBoxColor();
        String bgColor  = TerminalUI.getActiveBgColor();
        int col         = TerminalUI.centerCol(DASH_W);
        String padded   = TerminalUI.padL(text, DASH_CW);

        StringBuilder sb = new StringBuilder(200);
        sb.append("\u001B[").append(termRow).append(";").append(col).append("H");
        sb.append(bgColor).append(boxColor).append("\u2551 ");
        sb.append(textColor).append(bgColor).append(padded);
        sb.append(boxColor).append(bgColor).append(" \u2551").append(TerminalUI.RESET);
        System.out.print(sb);
    }

    private static void paintTBoxBarLine(int termRow, MealType slot, int tick) {
        String boxColor = TerminalUI.getActiveBoxColor();
        String bgColor  = TerminalUI.getActiveBgColor();
        int col         = TerminalUI.centerCol(DASH_W);

        String content = buildAnimatedBar(slot, tick, DASH_CW);
        int visLen = stripAnsi(content).length();
        int pad    = Math.max(0, DASH_CW - visLen);

        StringBuilder sb = new StringBuilder(200);
        sb.append("\u001B[").append(termRow).append(";").append(col).append("H");
        sb.append(bgColor).append(boxColor).append("\u2551 ").append(TerminalUI.RESET).append(bgColor);
        sb.append(content);
        if (pad > 0) sb.append(" ".repeat(pad));
        sb.append(bgColor).append(boxColor).append(" \u2551").append(TerminalUI.RESET);
        System.out.print(sb);
    }



    private static void paintBar(int termRow, int col, int iw, MealType slot, int tick) {
        String boxColor = TerminalUI.getActiveBoxColor();
        String bgColor = TerminalUI.getActiveBgColor();
        int contentW = iw - 1;  // visible chars after "║ "

        String content = buildAnimatedBar(slot, tick, contentW);
        int visLen = stripAnsi(content).length();
        int pad = Math.max(0, contentW - visLen);

        StringBuilder sb = new StringBuilder(200);
        sb.append("\u001B[").append(termRow).append(";").append(col).append("H");
        sb.append(bgColor).append(boxColor).append("║ ").append(TerminalUI.RESET).append(bgColor);
        sb.append(content);
        if (pad > 0) {
            sb.append(" ".repeat(pad));
        }
        sb.append(boxColor).append("║").append(TerminalUI.RESET);
        System.out.print(sb);
        System.out.flush();
    }

    private static void paintNowLine(int termRow, int col, int iw) {
        String boxColor = TerminalUI.getActiveBoxColor();
        String bgColor  = TerminalUI.getActiveBgColor();
        String mu       = ConsoleColors.Accent.MUTED;
        int contentW    = iw - 1;  // visible chars after "║ "

        String nowText = "Now: " + TimeManager.nowDate() + " " + TimeManager.nowTime()
                + " | Slot: " + TimeManager.getCurrentMealSlot()
                + " | Ramadan: " + TimeManager.isRamadanMode();
        String padded = TerminalUI.padL(nowText, contentW);

        StringBuilder sb = new StringBuilder(200);
        sb.append("\u001B[").append(termRow).append(";").append(col).append("H");
        sb.append(bgColor).append(boxColor).append("\u2551 ").append(TerminalUI.RESET).append(bgColor);
        sb.append(mu).append(padded).append(TerminalUI.RESET);
        sb.append(boxColor).append("\u2551").append(TerminalUI.RESET);
        System.out.print(sb);
        System.out.flush();
    }

    private static String buildAnimatedBar(MealType slot, int tick, int maxW) {
        LocalTime now = TimeManager.nowTime();
        DayOfWeek day = TimeManager.nowDay();
        TimeWindow w = getWindow(slot, day, TimeManager.isRamadanMode());

        if (w == null || now.isBefore(w.start) || now.isAfter(w.end)) {
            String dim = ConsoleColors.fgRGB(90, 80, 70);
            return dim + "--- closed ---" + TerminalUI.RESET;
        }

        long totalSec = Duration.between(w.start, w.end).getSeconds();
        long usedSec = Duration.between(w.start, now).getSeconds();
        long leftSec = Duration.between(now, w.end).getSeconds();

        // Reserve 8 chars for " 42m59s", 1 space gap, rest for bar
        int timeW = 8;
        int barW = Math.max(6, maxW - timeW - 1);
        int filled = (int) Math.round((usedSec * 1.0 / totalSec) * barW);
        filled = Math.max(0, Math.min(barW, filled));

        // Smooth pulse: 0 → 1 → 0 over 40 ticks
        double angle = tick * Math.PI / 20.0;
        float pulse = (float) (0.5 + 0.5 * Math.sin(angle));

        // Filled body: warm amber pulsing
        int fr = 255, fg2 = (int) (150 + 70 * pulse), fb = 0;
        // Playhead (last filled block): pulses towards near-white
        int pr = 255, pg = (int) (200 + 55 * pulse), pb = (int) (80 * pulse);

        String emptyCol  = ConsoleColors.fgRGB(80, 65, 55);
        String filledCol = ConsoleColors.fgRGB(fr, fg2, fb);
        String headCol   = ConsoleColors.fgRGB(pr, pg, pb) + TerminalUI.BOLD;
        String bgColor   = TerminalUI.getActiveBgColor();

        StringBuilder bar = new StringBuilder();
        bar.append(bgColor);
        // Draw filled blocks; last one gets the bright "playhead" color
        if (filled > 1) {
            bar.append(filledCol).append("\u2588".repeat(filled - 1));
        }
        if (filled > 0) {
            bar.append(headCol).append("\u2588").append("\u001B[22m").append(TerminalUI.RESET).append(bgColor);
        }
        // Empty blocks
        int remaining = barW - filled;
        if (remaining > 0) {
            bar.append(emptyCol).append("\u2591".repeat(remaining));
        }
        bar.append(TerminalUI.RESET).append(bgColor);

        // Time remaining (right side)
        long minLeft = leftSec / 60;
        long secLeft = leftSec % 60;
        String timeStr = String.format(" %3dm%02ds", minLeft, secLeft);
        bar.append(ConsoleColors.fgRGB(200, 175, 90)).append(timeStr).append(TerminalUI.RESET);

        return bar.toString();
    }

    private static String stripAnsi(String s) {
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    // ─────────────────────────────────────────────────────────────
    //  Static render — used for the initial extraHeader snapshot
    // ─────────────────────────────────────────────────────────────
    public static String renderSlotProgress(MealType slot) {
        LocalTime now = TimeManager.nowTime();
        DayOfWeek day = TimeManager.nowDay();

        TimeWindow w = getWindow(slot, day, TimeManager.isRamadanMode());

        if (w == null) {
            return "Status Bar: [ CLOSED ]";
        }

        if (now.isBefore(w.start) || now.isAfter(w.end)) {
            return "Status Bar: [ Not in " + slot + " window ]";
        }

        long totalSec = Duration.between(w.start, w.end).getSeconds();
        long usedSec = Duration.between(w.start, now).getSeconds();
        long leftSec = Duration.between(now, w.end).getSeconds();

        int width = 28;
        int filled = (int) Math.round((usedSec * 1.0 / totalSec) * width);
        filled = Math.max(0, Math.min(width, filled));

        String bar = "\u2588".repeat(filled) + "\u2591".repeat(width - filled);

        long minLeft = leftSec / 60;
        long secLeft = leftSec % 60;

        return String.format("%s %s-%s [%s] %dm %ds left",
                slot, w.start, w.end, bar, minLeft, secLeft);
    }

    // ─────────────────────────────────────────────────────────────
    //  Time window helpers
    // ─────────────────────────────────────────────────────────────
    private static TimeWindow getWindow(MealType slot, DayOfWeek day, boolean ramadan) {
        if (slot == MealType.NONE) {
            return null;
        }

        if (ramadan) {
            return switch (slot) {
                case SUHOOR ->
                    new TimeWindow(LocalTime.parse("03:00"), LocalTime.parse("04:30"));
                case IFTAR ->
                    new TimeWindow(LocalTime.parse("18:00"), LocalTime.parse("19:15"));
                case DINNER ->
                    new TimeWindow(LocalTime.parse("19:30"), LocalTime.parse("21:30"));
                default ->
                    null;
            };
        }

        if (slot == MealType.BREAKFAST) {
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                return new TimeWindow(LocalTime.parse("07:00"), LocalTime.parse("10:00"));
            }
            return new TimeWindow(LocalTime.parse("07:00"), LocalTime.parse("09:30"));
        }

        return switch (slot) {
            case LUNCH ->
                new TimeWindow(LocalTime.parse("12:00"), LocalTime.parse("14:00"));
            case DINNER ->
                new TimeWindow(LocalTime.parse("19:00"), LocalTime.parse("22:00"));
            default ->
                null;
        };
    }

    private static class TimeWindow {

        LocalTime start;
        LocalTime end;

        TimeWindow(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }
    }
}
