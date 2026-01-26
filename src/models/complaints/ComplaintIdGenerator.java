package models.complaints;

import libraries.collections.MyString;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ComplaintIdGenerator {
    private static long counter = 1;
    private ComplaintIdGenerator(){}
    public static void initFromFile(String complaintsFilePath){
        long max = 0;

        File f = new File(complaintsFilePath);
        if (!f.exists()){
            counter = 1;
            return;
        }

        try (FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr)){
            String line;
            while((line = br.readLine()) != null)
            {
                MyString msLine = new MyString(line);

                if (isBlank(msLine)) continue;
                MyString id = firstField(msLine);

                long num = extractNumberFromComplaintId(id);
                if (num > max) max = num;
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }

        counter = max + 1;
    }

    public static String complaintId(){
        return "C-" +(counter++);
    }

    private static boolean isBlank(MyString s){
        if (s == null) return true;
        for (int i = 0; i < s.length(); i++){
            if (s.charAt(i) > ' ') return false;
        }
        return true;
    }

    private static MyString firstField(MyString line){
        int start = 0;
        while (start < line.length() && line.charAt(start) <= ' ') start++;

        int i = start;
        while (i < line.length() && line.charAt(i) != '|') i++;
        return line.substring(0, i);
    }

    private static long extractNumberFromComplaintId(MyString id){
        if (id == null) return 0;
        if (id.length() < 3) return 0; // as minimum is "C-0"
        if (id.charAt(0) != 'C' || id.charAt(1) != '-') return 0;

        long num = 0;
        for (int i = 2; i < id.length(); i++){
            char ch = id.charAt(i);
            if (ch < '0' || ch > '9') return 0;
            num = num * 10 + (ch - '0');
        }
        return num;
    }
}
