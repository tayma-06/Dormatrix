package libraries.file;

import libraries.collections.MyArrayList;
import libraries.collections.MyString;

public class TextFile {
    private static final char SEP = '|';

    private TextFile() {}

    // Serializer
    public static String join(String ... parts){
        if (parts == null || parts.length == 0) return "";

        // convert String[] to MyString[]
        MyString[] msParts = new MyString[parts.length];
        for (int i = 0; i < parts.length; i++){
            msParts[i] = (parts[i] == null) ? new MyString("") : new MyString(parts[i]);

        }

        // join using MyString, then convert back to String
        MyString joined = MyString.join(SEP, msParts);
        return joined.toString();
    }

    // Deserializer
    public static MyArrayList<String> split(String line){
        MyArrayList<String> out = new MyArrayList<>();

        if (line == null)
        {
            out.add("");
            return out;
        }

        MyString msLine = new MyString(line);
        MyString[] parts = msLine.split(SEP);

        for (int i = 0; i < parts.length; i++)
        {
            out.add(parts[i].toString());
        }

        return out;
    }

}
