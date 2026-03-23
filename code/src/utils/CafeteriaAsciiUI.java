package utils;

import models.food.MealType;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class CafeteriaAsciiUI {

    private static volatile boolean barRunning = false;
    private static Thread barThread = null;

    private static volatile boolean tokenAnimRunning = false;
    private static Thread tokenAnimThread = null;

    private static final String SAVE_CUR = "\u001B7";
    private static final String RESTORE_CUR = "\u001B8";

    public static void startBarAnimation(int requestedStartRow, int itemCount, int extraHeaderCount, MealType slot) {
        stopBarAnimation();
        barRunning = true;
        barThread = new Thread(() -> {
            int tick = 0;
            int previousNowRow = -1;
            int previousBarRow = -1;

            while (barRunning) {
                try {
                    int dashboardTop = resolveDashboardTop(requestedStartRow, itemCount, extraHeaderCount);
                    int nowRow = dashboardTop + 4;
                    int barRow = dashboardTop + 5;
                    int liveCol = TerminalUI.boxCol();
                    int liveInnerWidth = TerminalUI.innerW();

                    System.out.print(SAVE_CUR);

                    if (previousNowRow > 0 && previousNowRow != nowRow) {
                        clearAnimatedRow(previousNowRow);
                    }
                    if (previousBarRow > 0 && previousBarRow != barRow) {
                        clearAnimatedRow(previousBarRow);
                    }

                    clearAnimatedRow(nowRow);
                    clearAnimatedRow(barRow);
                    paintNowLine(nowRow, liveCol, liveInnerWidth);
                    paintBar(barRow, liveCol, liveInnerWidth, slot, tick);

                    System.out.print(RESTORE_CUR);
                    System.out.flush();

                    previousNowRow = nowRow;
                    previousBarRow = barRow;
                    tick = (tick + 1) % 40;
                    Thread.sleep(120);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception ignored) {
                }
            }

            if (previousNowRow > 0 || previousBarRow > 0) {
                try {
                    System.out.print(SAVE_CUR);
                    if (previousNowRow > 0) {
                        clearAnimatedRow(previousNowRow);
                    }
                    if (previousBarRow > 0) {
                        clearAnimatedRow(previousBarRow);
                    }
                    System.out.print(RESTORE_CUR);
                    System.out.flush();
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

    public static void startTokenScreenAnimation(int dateRow) {
        stopTokenScreenAnimation();
        tokenAnimRunning = true;
        tokenAnimThread = new Thread(() -> {
            int tick = 0;
            int fixedDateRow = dateRow;

            while (tokenAnimRunning) {
                try {
                    MealType liveSlot = TimeManager.getCurrentMealSlot();
                    String dateLine = "Date: " + TimeManager.nowDate()
                            + " | Time: " + TimeManager.nowTime()
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    String statusLine = "Current Status: "
                            + (liveSlot == MealType.NONE ? "CLOSED" : "ACTIVE - " + liveSlot);

                    int col = TerminalUI.boxCol();
                    int contentWidth = Math.max(10, TerminalUI.innerW() - 2);

                    System.out.print(SAVE_CUR);
                    clearAnimatedRow(fixedDateRow);
                    clearAnimatedRow(fixedDateRow + 1);
                    clearAnimatedRow(fixedDateRow + 2);
                    paintDynamicTBoxTextLine(fixedDateRow, dateLine, TerminalUI.getActiveTextColor(), col, contentWidth);
                    paintDynamicTBoxTextLine(fixedDateRow + 1, statusLine, TerminalUI.getActiveTextColor(), col, contentWidth);
                    paintDynamicTBoxBarLine(fixedDateRow + 2, liveSlot, tick, col, contentWidth);
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

    private static int resolveDashboardTop(int requestedStartRow, int itemCount, int extraHeaderCount) {
        int totalRows = 8 + itemCount + extraHeaderCount;
        if (requestedStartRow <= 3) {
            return Math.max(2, TerminalUI.centerRow(totalRows) - 1);
        }
        return requestedStartRow;
    }

    private static void clearAnimatedRow(int termRow) {
        int width = Math.max(1, TerminalUI.termW());
        System.out.print("\u001B[" + termRow + ";1H"
                + TerminalUI.getActiveBgColor()
                + " ".repeat(width)
                + TerminalUI.RESET);
    }

    private static void paintDynamicTBoxTextLine(int termRow, String text, String textColor, int col, int contentWidth) {
        String boxColor = TerminalUI.getActiveBoxColor();
        String panelBg = TerminalUI.getActivePanelBgColor();
        String padded = TerminalUI.padL(text, contentWidth);

        StringBuilder sb = new StringBuilder(200);
        sb.append("\u001B[").append(termRow).append(';').append(col).append('H');
        sb.append(boxColor).append(panelBg).append("║ ");
        sb.append(textColor).append(panelBg).append(padded);
        sb.append(boxColor).append(panelBg).append(" ║").append(TerminalUI.RESET);
        System.out.print(sb);
    }

    private static void paintDynamicTBoxBarLine(int termRow, MealType slot, int tick, int col, int contentWidth) {
        String boxColor = TerminalUI.getActiveBoxColor();
        String panelBg = TerminalUI.getActivePanelBgColor();

        String content = buildAnimatedBar(slot, tick, contentWidth);
        int visibleLength = stripAnsi(content).length();
        int pad = Math.max(0, contentWidth - visibleLength);

        StringBuilder sb = new StringBuilder(220);
        sb.append("\u001B[").append(termRow).append(';').append(col).append('H');
        sb.append(boxColor).append(panelBg).append("║ ");
        sb.append(content);
        if (pad > 0) {
            sb.append(panelBg).append(" ".repeat(pad));
        }
        sb.append(boxColor).append(panelBg).append(" ║").append(TerminalUI.RESET);
        System.out.print(sb);
    }

    private static void paintBar(int termRow, int col, int iw, MealType slot, int tick) {
        String boxColor = TerminalUI.getActiveBoxColor();
        String panelBg = TerminalUI.getActivePanelBgColor();
        int contentWidth = Math.max(10, iw - 2);

        String content = buildAnimatedBar(slot, tick, contentWidth);
        int visibleLength = stripAnsi(content).length();
        int pad = Math.max(0, contentWidth - visibleLength);

        StringBuilder sb = new StringBuilder(220);
        sb.append("\u001B[").append(termRow).append(';').append(col).append('H');
        sb.append(boxColor).append(panelBg).append("║ ");
        sb.append(content);
        if (pad > 0) {
            sb.append(panelBg).append(" ".repeat(pad));
        }
        sb.append(boxColor).append(panelBg).append(" ║").append(TerminalUI.RESET);
        System.out.print(sb);
        System.out.flush();
    }

    private static void paintNowLine(int termRow, int col, int iw) {
        String boxColor = TerminalUI.getActiveBoxColor();
        String panelBg = TerminalUI.getActivePanelBgColor();
        String muted = ConsoleColors.Accent.MUTED;
        int contentWidth = Math.max(10, iw - 2);

        String nowText = "Now: " + TimeManager.nowDate() + " " + TimeManager.nowTime()
                + " | Slot: " + TimeManager.getCurrentMealSlot();
        String padded = TerminalUI.padL(nowText, contentWidth);

        StringBuilder sb = new StringBuilder(220);
        sb.append("\u001B[").append(termRow).append(';').append(col).append('H');
        sb.append(boxColor).append(panelBg).append("║ ");
        sb.append(muted).append(panelBg).append(padded).append(TerminalUI.RESET);
        sb.append(boxColor).append(panelBg).append(" ║").append(TerminalUI.RESET);
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

        int timeW = 8;
        int barW = Math.max(6, maxW - timeW - 1);
        int filled = (int) Math.round((usedSec * 1.0 / totalSec) * barW);
        filled = Math.max(0, Math.min(barW, filled));

        double angle = tick * Math.PI / 20.0;
        float pulse = (float) (0.5 + 0.5 * Math.sin(angle));

        int fr = 255, fg2 = (int) (150 + 70 * pulse), fb = 0;
        int pr = 255, pg = (int) (200 + 55 * pulse), pb = (int) (80 * pulse);

        String emptyCol = ConsoleColors.fgRGB(80, 65, 55);
        String filledCol = ConsoleColors.fgRGB(fr, fg2, fb);
        String headCol = ConsoleColors.fgRGB(pr, pg, pb) + TerminalUI.BOLD;
        String panelBg = TerminalUI.getActivePanelBgColor();

        StringBuilder bar = new StringBuilder();
        bar.append(panelBg);
        if (filled > 1) {
            bar.append(filledCol).append("█".repeat(filled - 1));
        }
        if (filled > 0) {
            bar.append(headCol).append("█").append("\u001B[22m").append(TerminalUI.RESET).append(panelBg);
        }
        int remaining = barW - filled;
        if (remaining > 0) {
            bar.append(emptyCol).append("░".repeat(remaining));
        }
        bar.append(TerminalUI.RESET).append(panelBg);

        long minLeft = leftSec / 60;
        long secLeft = leftSec % 60;
        String timeStr = String.format(" %3dm%02ds", minLeft, secLeft);
        bar.append(ConsoleColors.fgRGB(200, 175, 90)).append(timeStr).append(TerminalUI.RESET);

        return bar.toString();
    }

    private static String stripAnsi(String s) {
        return s == null ? "" : s.replaceAll("\u001B\\[[;\\d?]*[ -/]*[@-~]", "");
    }

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

        String bar = "█".repeat(filled) + "░".repeat(width - filled);

        long minLeft = leftSec / 60;
        long secLeft = leftSec % 60;

        return String.format("%s %s-%s [%s] %dm %ds left",
                slot, w.start, w.end, bar, minLeft, secLeft);
    }

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