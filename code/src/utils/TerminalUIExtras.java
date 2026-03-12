package utils;

import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;

/**
 * Extension helper for TerminalUI.
 * All custom additions go here — TerminalUI itself is untouched.
 *
 * Usage in CLI classes:
 *   import static utils.TerminalUIExtras.*;
 */
public final class TerminalUIExtras {

    private TerminalUIExtras() {}

    // ── Use the same JLine terminal that MainDashboard already set up ─
    private static Terminal getSharedTerminal() {
        return TerminalUI.getJLineTerminal();
    }

    // ─────────────────────────────────────────────────────────────
    //  tArrowSelect
    //
    //  Draws the box ONCE, then highlights rows in-place on nav.
    //  Same visual style as readChoiceArrow() in TerminalUI.
    //
    //  Navigation:
    //    UP / DOWN arrow  navigate   (highlights the row)
    //    ENTER            confirm  -> returns 0-based index
    //    0-9              type number then Enter
    //    ESC / Q          cancel   -> returns -1
    // ─────────────────────────────────────────────────────────────

    public static int tArrowSelect(String title, String[] items) throws InterruptedException {
        // ── Fill background first ─────────────────────────────────
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);


        if (items == null || items.length == 0) {
            TerminalUI.tBoxTop();
            TerminalUI.tBoxTitle(title);
            TerminalUI.tBoxSep();
            TerminalUI.tBoxLine("(No items available)");
            TerminalUI.tBoxBottom();
            return -1;
        }

        // ── Draw the box once ─────────────────────────────────────
        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle(title);
        TerminalUI.tBoxSep();

        // Remember the terminal row where each item was drawn.
        // We get the current row by printing and tracking with ANSI CPR,
        // but the simpler approach: record System.out row via a counter
        // starting from the known cursor position after tBoxSep().
        // Instead, we use a two-pass approach: draw all items, then
        // re-highlight by re-printing at the saved row positions.

        int[] itemRows = new int[items.length];

        // Get current row after drawing the separator
        int startRow = getCursorRow();

        for (int i = 0; i < items.length; i++) {
            itemRows[i] = startRow + i;
            TerminalUI.tBoxLine("    " + items[i]);
        }

        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine(
                "  [Up/Down] Navigate    [Enter] Select    [ESC] Cancel",
//                ConsoleColors.Accent.MUTED);
//                ConsoleColors.fgRGB(255, 245, 100));
//                ConsoleColors.fgRGB(160, 150, 60));  // muted yellow
                ConsoleColors.fgRGB(120, 110, 40));


        // ── Input row ─────────────────────────────────────────────
        TerminalUI.tBoxSep();
        int inputRow = getCursorRow();
        drawInputRow("");
        TerminalUI.tBoxBottom();
        System.out.flush();


//        TerminalUI.tBoxBottom();
//        System.out.flush();

        // Highlight first item
        int selected = 0;
        renderItemHighlight(itemRows[selected], items[selected], true);
        StringBuilder inputBuffer = new StringBuilder();
        inputBuffer.append(selected + 1);                        // ← add this
        updateInputRow(inputRow, inputBuffer.toString());
        System.out.flush();

        Terminal term = getSharedTerminal();
        if (term == null) {
            return fallbackNumberInput(items);
        }

        inputBuffer = new StringBuilder();
        Attributes saved = term.enterRawMode();
        NonBlockingReader reader = term.reader();

        try {
            while (true) {
                int c = reader.read();
                if (c == -1) continue;

                if (c == 27) {                              // ESC or arrow
                    int n1 = reader.read(100);
                    if (n1 == '[' || n1 == 'O') {
                        int n2 = reader.read(100);
                        if (n2 == 'A') {                    // UP
                            renderItemHighlight(itemRows[selected], items[selected], false);
                            selected = (selected - 1 + items.length) % items.length;
                            renderItemHighlight(itemRows[selected], items[selected], true);
                            inputBuffer.setLength(0);
                            inputBuffer.append(selected + 1);
                            updateInputRow(inputRow, inputBuffer.toString());
                            System.out.flush();
                            continue;
                        }
                        if (n2 == 'B') {                    // DOWN
                            renderItemHighlight(itemRows[selected], items[selected], false);
                            selected = (selected + 1) % items.length;
                            renderItemHighlight(itemRows[selected], items[selected], true);
                            inputBuffer.setLength(0);
                            inputBuffer.append(selected + 1);
                            updateInputRow(inputRow, inputBuffer.toString());
                            System.out.flush();
                            continue;
                        }
                    } else {
                        // bare ESC = cancel
                        renderItemHighlight(itemRows[selected], items[selected], false);
                        return -1;
                    }
                    continue;
                }

                if (c == 13 || c == 10) {                   // Enter
                    renderItemHighlight(itemRows[selected], items[selected], false);
                    if (inputBuffer.length() > 0) {
                        try {
                            int n = Integer.parseInt(inputBuffer.toString());
                            if (n == 0) return -1;
                            if (n >= 1 && n <= items.length) return n - 1;
                        } catch (NumberFormatException ignored) {
                        }
                        // invalid number — reset
                        inputBuffer.setLength(0);
                        updateInputRow(inputRow, "");
                        renderItemHighlight(itemRows[selected], items[selected], true);
                        continue;
                    }
                    return selected;
                }

                if (c == 3 || c == 'q' || c == 'Q') {      // Ctrl+C or Q
                    renderItemHighlight(itemRows[selected], items[selected], false);
                    return -1;
                }

                if (c == 127 || c == 8) {                   // Backspace
                    if (inputBuffer.length() > 0) {
                        inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                        updateInputRow(inputRow, inputBuffer.toString());
                        // Update highlight to match remaining number if valid
                        try {
                            int n = Integer.parseInt(inputBuffer.toString());
                            if (n >= 1 && n <= items.length) {
                                renderItemHighlight(itemRows[selected], items[selected], false);
                                selected = n - 1;
                                renderItemHighlight(itemRows[selected], items[selected], true);
                            }
                        } catch (NumberFormatException ignored) {
                        }
                        System.out.flush();
                    }
                    continue;
                }

                if (c >= '0' && c <= '9') {                 // digit
                    inputBuffer.append((char) c);
                    updateInputRow(inputRow, inputBuffer.toString());
                    // Move highlight to match typed number if valid
                    try {
                        int n = Integer.parseInt(inputBuffer.toString());
                        if (n >= 1 && n <= items.length) {
                            renderItemHighlight(itemRows[selected], items[selected], false);
                            selected = n - 1;
                            renderItemHighlight(itemRows[selected], items[selected], true);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                    System.out.flush();
                }
            }
        } catch (IOException e) {
            return fallbackNumberInput(items);
        } finally {
            term.setAttributes(saved);
            System.out.flush();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Draw / update the "Your choice no:" input row
    // ─────────────────────────────────────────────────────────────

    private static final String INPUT_LABEL = "  Your Choice: ";

    private static void drawInputRow(String value) {
        int col = TerminalUI.boxCol();
        int iw  = TerminalUI.innerW();
        String box  = TerminalUI.getActiveBoxColor();
        String bg   = TerminalUI.getActiveBgColor();
        String fg   = ConsoleColors.fgRGB(255, 245, 100);
        int fieldW  = iw - INPUT_LABEL.length() - 1;//-2

        System.out.print(
                "\u001B[" + col + "G"
                        + box + bg + "║"
                        + fg + INPUT_LABEL + TerminalUI.RESET + bg
                        + fg + TerminalUI.BOLD + value + TerminalUI.RESET + bg
                        + " ".repeat(Math.max(0, fieldW - value.length()))
                        + box + bg + " ║" + TerminalUI.RESET
        );
        System.out.println(); // separate println so it always ends on a clean new line
        System.out.flush();
    }

    private static void updateInputRow(int row, String value) {
        int col    = TerminalUI.boxCol();
        int iw     = TerminalUI.innerW();
        String box = TerminalUI.getActiveBoxColor();
        String bg  = TerminalUI.getActiveBgColor();
        String fg  = ConsoleColors.fgRGB(255, 245, 100);
        int fieldW = iw - INPUT_LABEL.length() - 1;  //-2

        TerminalUI.at(row, col);
        System.out.print(
                box + bg + "║"
                        + fg + INPUT_LABEL + TerminalUI.RESET + bg
                        + fg + TerminalUI.BOLD + value + TerminalUI.RESET + bg
                        + " ".repeat(Math.max(0, fieldW - value.length()))
                        + box + bg + " ║" + TerminalUI.RESET
        );
    }


    // ─────────────────────────────────────────────────────────────
    //  Re-render a single item row with highlight on/off
    //  Mirrors the style of renderHighlight() in TerminalUI.
    // ─────────────────────────────────────────────────────────────

    private static void renderItemHighlight(int row, String label, boolean on) {
        int col     = TerminalUI.boxCol();
        int iw      = TerminalUI.innerW();
        String box  = TerminalUI.getActiveBoxColor();
        String bg   = TerminalUI.getActiveBgColor();
        String text = TerminalUI.getActiveTextColor();

//        String rowBg    = on ? ConsoleColors.bgRGB(185, 165, 220) : bg;
//        String rowFg    = on ? ConsoleColors.fgRGB(25, 15, 55)    : text;

//        String rowBg = on ? ConsoleColors.bgRGB(80, 70, 20)    : bg;   // dark yellow bg
//        String rowFg = on ? ConsoleColors.fgRGB(255, 230, 100) : text; // bright yellow text

//        String rowBg = on ? ConsoleColors.bgRGB(120, 100, 0)   : bg;   // brighter yellow bg
//        String rowFg = on ? ConsoleColors.fgRGB(255, 245, 100) : text; // bright yellow text

        String rowBg = on ? ConsoleColors.bgRGB(160, 130, 0)   : bg; //more vivid yellow

//        String rowBg  = on ? ConsoleColors.bgRGB(0, 0, 0)       : bg;   // black bg
        String rowFg = on ? ConsoleColors.fgRGB(255, 255, 120) : text;

        String prefix   = on ? "  > " : "    ";
        String bold     = on ? TerminalUI.BOLD : "";

        String content  = prefix + label;
        int pad = Math.max(0, iw - 2 - content.length());

        TerminalUI.at(row, col);
        System.out.print(
                box + bg + "║ "
                        + rowBg + rowFg + bold + content + " ".repeat(pad) + TerminalUI.RESET
                        + box + bg + " ║" + TerminalUI.RESET
        );
    }

    // ─────────────────────────────────────────────────────────────
    //  Get current cursor row using ANSI DSR (ESC[6n)
    //  Returns a best-guess if the terminal doesn't respond.
    // ─────────────────────────────────────────────────────────────

    private static int getCursorRow() {
        try {
            System.out.print("\u001B[6n");
            System.out.flush();
            Terminal term = getSharedTerminal();
            if (term == null) return 10;

            NonBlockingReader reader = term.reader();
            // Response format: ESC [ row ; col R
            int b = reader.read(200);
            if (b != 27) return 10;
            if (reader.read(100) != '[') return 10;

            StringBuilder sb = new StringBuilder();
            int ch;
            while ((ch = reader.read(100)) != 'R' && ch != -1) {
                sb.append((char) ch);
            }
            String[] parts = sb.toString().split(";");
            if (parts.length >= 1) {
                return Integer.parseInt(parts[0].trim());
            }
        } catch (Exception ignored) {}
        return 10;
    }

    // ─────────────────────────────────────────────────────────────
    //  Fallback — plain number input if JLine unavailable
    // ─────────────────────────────────────────────────────────────

    private static int fallbackNumberInput(String[] items) {
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Type a number and press Enter. [0] to cancel.",
                ConsoleColors.Accent.MUTED);
        TerminalUI.tBoxSep();
        TerminalUI.tInputRow();

        while (true) {
            String input = FastInput.readLine().trim();
            if (input.isEmpty()) return -1;
            try {
                int n = Integer.parseInt(input);
                if (n == 0) return -1;
                if (n >= 1 && n <= items.length) return n - 1;
                TerminalUI.tError("Enter a number between 1 and " + items.length + ".");
                TerminalUI.tPrompt("Your choice no: ");
            } catch (NumberFormatException e) {
                TerminalUI.tError("Invalid input. Please enter a number.");
                TerminalUI.tPrompt("Your choice no: ");
            }
        }
    }
}
