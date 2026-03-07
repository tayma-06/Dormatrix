package cli.forms.complaint;

import java.util.Scanner;
import models.enums.ComplaintCategory;
import utils.TerminalUI;

public class ComplaintForm {
    private final Scanner sc;

    public ComplaintForm(Scanner sc){ this.sc = sc; }

    public int readInt(){
        while(true){
            String line = sc.nextLine();
            try { return Integer.parseInt(line.trim()); }
            catch(Exception e){ TerminalUI.tPrompt("Invalid input. Enter again: "); }
        }
    }

    public String readLine(String prompt){
        TerminalUI.tPrompt(prompt);
        return sc.nextLine();
    }

    public String readNonEmpty(String prompt){
        while(true){
            TerminalUI.tPrompt(prompt);
            String s = sc.nextLine().trim();
            if(!s.isEmpty()) return s;
            TerminalUI.tError("Input cannot be empty.");
        }
    }

    public ComplaintCategory readCategory(){
        TerminalUI.tEmpty();
        TerminalUI.tSubDashboard("SELECT COMPLAINT CATEGORY", new String[]{
            "[1] Electricity",
            "[2] Plumbing",
            "[3] Internet",
            "[4] Cleaning",
            "[0] Back"
        });
        int x = readInt();

        switch(x){
            case 1: return ComplaintCategory.ELECTRICITY;
            case 2: return ComplaintCategory.PLUMBING;
            case 3: return ComplaintCategory.INTERNET;
            case 4: return ComplaintCategory.CLEANING;
//            case 5: return ComplaintCategory.SECURITY;
//            case 6: return ComplaintCategory.OTHER;
            case 0: return null;
            default:
                TerminalUI.tError("Invalid category.");
                return readCategory();
        }
    }
}
