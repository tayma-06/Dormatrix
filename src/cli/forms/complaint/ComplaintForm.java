package cli.forms.complaint;

import java.util.Scanner;
import models.enums.ComplaintCategory;

public class ComplaintForm {
    private final Scanner sc;

    public ComplaintForm(Scanner sc){ this.sc = sc; }

    public int readInt(){
        while(true){
            String line = sc.nextLine();
            try { return Integer.parseInt(line.trim()); }
            catch(Exception e){ System.out.println("Invalid input. Enter again: "); }
        }
    }

    public String readLine(String prompt){
        System.out.print(prompt);
        return sc.nextLine();
    }

    public String readNonEmpty(String prompt){
        while(true){
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if(!s.isEmpty()) return s;
            System.out.println("Input cannot be empty.");
        }
    }

    public ComplaintCategory readCategory(){
        System.out.println();
        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    SELECT COMPLAINT CATEGORY                        ║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
        System.out.println("║ [1] Electricity                                                     ║");
        System.out.println("║ [2] Plumbing                                                        ║");
        System.out.println("║ [3] Internet                                                        ║");
        System.out.println("║ [4] Cleaning                                                        ║");
        System.out.println("║ [5] Security                                                        ║");
        System.out.println("║ [6] Other                                                           ║");
        System.out.println("║ [0] Back                                                            ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");

        System.out.println();
        System.out.print("Enter choice: ");
        int x = readInt();

        switch(x){
            case 1: return ComplaintCategory.ELECTRICITY;
            case 2: return ComplaintCategory.PLUMBING;
            case 3: return ComplaintCategory.INTERNET;
            case 4: return ComplaintCategory.CLEANING;
            case 5: return ComplaintCategory.SECURITY;
            case 6: return ComplaintCategory.OTHER;
            case 0: return null;
            default:
                System.out.println("Invalid category.");
                return readCategory();
        }
    }
}
