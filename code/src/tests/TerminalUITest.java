package tests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import utils.TerminalUI;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class TerminalUITest {

    @Test
    public void testPadC_evenWidth() {
        String result = TerminalUI.padC("Hi", 6);
        assertEquals("  Hi  ", result);
    }

    @Test
    public void testPadC_oddWidth() {
        String result = TerminalUI.padC("Hi", 5);
        assertEquals(" Hi  ", result);
    }

    @Test
    public void testPadL_normal() {
        String result = TerminalUI.padL("Test", 6);
        assertEquals("Test  ", result);
    }

    @Test
    public void testPadL_null() {
        String result = TerminalUI.padL(null, 4);
        assertEquals("    ", result);
    }

    @Test
    public void testPadL_truncate() {
        String result = TerminalUI.padL("HelloWorld", 5);
        assertEquals("Hello", result);
    }

    @Test
    public void testTruncate_shortString() {
        String result = TerminalUI.truncate("Hello", 10);
        assertEquals("Hello", result);
    }

    @Test
    public void testTruncate_longString() {
        String result = TerminalUI.truncate("HelloWorld", 5);
        assertEquals("Hello", result);
    }

    @Test
    public void testTruncate_null() {
        String result = TerminalUI.truncate(null, 5);
        assertEquals("", result);
    }

    @Test
    public void testStripAnsi() {
        String colored = "\u001B[31mHello\u001B[0m";
        String result = TerminalUI.stripAnsi(colored);
        assertEquals("Hello", result);
    }

    @Test
    public void testBotBorder() {
        String result = TerminalUI.botBorder(4);
        assertEquals("╰────╯", result);
    }

    @Test
    public void testTopBorder_containsLabel() {
        String result = TerminalUI.topBorder("MENU", 20, "\u001B[35m");
        String plain = TerminalUI.stripAnsi(result);

        assertTrue(plain.contains("MENU"));
        assertTrue(plain.startsWith("╭─ "));
        assertTrue(plain.endsWith("╮"));
    }

    @Test
    public void testLerp_start() {
        assertEquals(10, TerminalUI.lerp(10, 50, 0f));
    }

    @Test
    public void testLerp_middle() {
        assertEquals(30, TerminalUI.lerp(10, 50, 0.5f));
    }

    @Test
    public void testLerp_end() {
        assertEquals(50, TerminalUI.lerp(10, 50, 1f));
    }

    @Test
    public void testGradient_keepsText() {
        String result = TerminalUI.gradient("DORM", new int[]{10, 20, 30}, new int[]{40, 50, 60});
        String plain = TerminalUI.stripAnsi(result);
        assertEquals("DORM", plain);
    }

    @Test
    public void testGradient_containsAnsi() {
        String result = TerminalUI.gradient("AB", new int[]{10, 20, 30}, new int[]{40, 50, 60});
        assertTrue(result.contains("\u001B[38;2;"));
    }

    @Test
    public void testSetActiveTheme_threeArgs() {
        String box = "\u001B[38;2;1;2;3m";
        String text = "\u001B[38;2;4;5;6m";
        String bg = "\u001B[48;2;7;8;9m";

        TerminalUI.setActiveTheme(box, text, bg);

        assertEquals(box, TerminalUI.getActiveBoxColor());
        assertEquals(text, TerminalUI.getActiveTextColor());
        assertEquals(bg, TerminalUI.getActiveBgColor());
        assertEquals(bg, TerminalUI.getActivePanelBgColor());
        assertEquals(bg, TerminalUI.getActiveInputBgColor());
    }

    @Test
    public void testSetActiveTheme_fiveArgs() {
        String box = "\u001B[38;2;11;12;13m";
        String text = "\u001B[38;2;14;15;16m";
        String bg = "\u001B[48;2;17;18;19m";
        String panel = "\u001B[48;2;21;22;23m";
        String input = "\u001B[48;2;31;32;33m";

        TerminalUI.setActiveTheme(box, text, bg, panel, input);

        assertEquals(box, TerminalUI.getActiveBoxColor());
        assertEquals(text, TerminalUI.getActiveTextColor());
        assertEquals(bg, TerminalUI.getActiveBgColor());
        assertEquals(panel, TerminalUI.getActivePanelBgColor());
        assertEquals(input, TerminalUI.getActiveInputBgColor());
    }

    @Test
    public void testDrawInputPrompt_printsText() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(out));

        try {
            TerminalUI.drawInputPrompt("Press Enter", "\u001B[37m", "\u001B[40m");
        } finally {
            System.setOut(old);
        }

        String printed = out.toString();
        assertTrue(printed.contains("Press Enter"));
    }

    @Test
    public void testFillBackground_printsSomething() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(out));

        try {
            TerminalUI.fillBackground("\u001B[48;2;20;10;30m");
        } finally {
            System.setOut(old);
        }

        String printed = out.toString();
        assertFalse(printed.isEmpty());
    }

    @Test
    public void testPaintScreenGradient_printsEscapeCodes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(out));

        try {
            TerminalUI.paintScreenGradient(
                    new int[]{10, 10, 10},
                    new int[]{20, 20, 20},
                    new int[]{30, 30, 30},
                    "\u001B[37m"
            );
        } finally {
            System.setOut(old);
        }

        String printed = out.toString();
        assertTrue(printed.contains("\u001B[2J\u001B[H"));
    }
}