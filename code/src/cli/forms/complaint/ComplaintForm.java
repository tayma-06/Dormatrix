package cli.forms.complaint;

import java.util.Scanner;
import models.enums.ComplaintCategory;
import utils.ConsoleUtil;
import utils.TerminalUI;

import static utils.TerminalUIExtras.tArrowSelect;

public class ComplaintForm {

    private final Scanner sc;

    public ComplaintForm(Scanner sc) {
        this.sc = sc;
    }

    public int readInt() {
        while (true) {
            String line = sc.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (Exception e) {
                TerminalUI.tError("Invalid input. Please enter a number.");
                TerminalUI.tPause();
                TerminalUI.tPrompt("Enter a number: ");
            }
        }
    }

    public String readLine(String prompt) {
        TerminalUI.tPrompt(prompt);
        return sc.nextLine();
    }

    public String readNonEmpty(String prompt) {
        while (true) {
            TerminalUI.tPrompt(prompt);
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) {
                return s;
            }
            TerminalUI.tError("Input cannot be empty.");
            TerminalUI.tPause();
        }
    }

    public ComplaintCategory readCategory() {
        ConsoleUtil.clearScreen();
        TerminalUI.fillBackground(TerminalUI.getActiveBgColor());
        TerminalUI.at(2, 1);

        TerminalUI.tBoxTop();
        TerminalUI.tBoxTitle("SELECT COMPLAINT CATEGORY");
        TerminalUI.tBoxSep();
        TerminalUI.tBoxLine("Choose a complaint category using the arrow keys.");
        TerminalUI.tBoxBottom();

        int choice;
        try {
            choice = tArrowSelect("COMPLAINT CATEGORY", new String[]{
                    "Electricity",
                    "Plumbing",
                    "Internet",
                    "Cleaning",
                    "Back"
            }, false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }

        return switch (choice) {
            case 0 -> ComplaintCategory.ELECTRICITY;
            case 1 -> ComplaintCategory.PLUMBING;
            case 2 -> ComplaintCategory.INTERNET;
            case 3 -> ComplaintCategory.CLEANING;
            default -> null;
        };
    }
}