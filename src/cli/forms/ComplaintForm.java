package cli.forms;

import java.util.Scanner;
import models.enums.ComplaintCategory;

public class ComplaintForm {
    private final Scanner sc;

    public ComplaintForm(Scanner sc){ this.sc = sc; }

    public int readInt(){
        while(true){
            String line = sc.nextLine();
            try { return Integer.parseInt(line.trim()); }
            catch(Exception e){ System.out.print("Invalid input. Enter again: "); }
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
        System.out.println("\nSelect Complaint Category:");
        System.out.println("1. ELECTRICITY");
        System.out.println("2. PLUMBING");
        System.out.println("3. INTERNET");
        System.out.println("4. CLEANING");
        System.out.println("5. SECURITY");
        System.out.println("6. OTHER");
        System.out.print("Enter choice: ");
        int x = readInt();

        switch(x){
            case 1: return ComplaintCategory.ELECTRICITY;
            case 2: return ComplaintCategory.PLUMBING;
            case 3: return ComplaintCategory.INTERNET;
            case 4: return ComplaintCategory.CLEANING;
            case 5: return ComplaintCategory.SECURITY;
            case 6: return ComplaintCategory.OTHER;
            default:
                System.out.println("Invalid category.");
                return readCategory();
        }
    }
}
