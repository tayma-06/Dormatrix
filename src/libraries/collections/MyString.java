package libraries.collections;

public class MyString {
    private final String str;

    public MyString(String str) {
        this.str = str != null ? str : "";
    }
    
    public MyString(char[] chars) {
        if (chars == null)
            this.str = "";
        else {
            int length = 0;
            while (length < chars.length && chars[length] != '\0') {
                length++;
            }
            char[] newchars = new char[length];
            for (int i = 0; i < length; i++) {
                newchars[i] = chars[i];
            }
            this.str = new String(newchars);
        }
    }

    public int length()
    {
        return toCharArrayInternal().length;
    }

    public String getValue() {
        return this.str;
    }

    public char[] toCharArray() {
        char[] chars = toCharArrayInternal();
        char[] newchars = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            newchars[i] = chars[i];
        }
        return newchars;
    }

    private char[] toCharArrayInternal()
    {
        int length = 0;
        try
        {
            while(true)
            {
                str.charAt(length);
                length++;
            }
        }
        catch(StringIndexOutOfBoundsException e){

        }
        char[] newchars = new char[length];
        for (int i = 0; i < length; i++) {
            newchars[i] = str.charAt(i);
        }
        return newchars;
    }

    public char charAt(int index)
    {
        try {
            char[] chars = toCharArrayInternal();
            return chars[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new StringIndexOutOfBoundsException();
        }
    }

    public boolean equals(MyString other)
    {
        if(this == other)
        {
            return true;
        }
        if(other == null)
        {
            return false;
        }
        char[] thisChars = toCharArrayInternal();
        char[] otherChars = other.toCharArrayInternal();
        if(thisChars.length != otherChars.length)
        {
            return false;
        }
        for (int i = 0; i < thisChars.length; i++)
        {
            if(thisChars[i] != otherChars[i])
            {
                return false;
            }
        }
        return true;
    }
    public int compareTo(MyString other)
    {
        if(other == null)
        {
            return 1;
        }
        char[] thisChars = toCharArrayInternal();
        char[] otherChars = other.toCharArrayInternal();
        int length1 = thisChars.length;
        int length2 = otherChars.length;
        int minLength = length1 < length2 ? length1 : length2;
        for(int i = 0; i < minLength; i++)
        {
            if(thisChars[i] != otherChars[i])
            {
                return thisChars[i] - otherChars[i];
            }
        }
        return length1 - length2;
    }
    public MyString substring(int begin, int end)
    {
        char[] chars = toCharArrayInternal();
        if(begin < 0)
        {
            begin = 0;
        }
        if(end > chars.length)
        {
            end = chars.length;
        }
        if(begin > end)
        {
            return new MyString("");
        }
        char[] newchars = new char[end - begin];
        for(int i = begin; i < end; i++)
        {
            newchars[i - begin] = chars[i];
        }
        return new MyString(newchars);
    }
    public int indexOf(char ch)
    {
        char[] chars = toCharArrayInternal();
        for(int i = 0; i < chars.length; i++)
        {
            if(chars[i] == ch)
            {
                return i;
            }
        }
        return -1;
    }
    public MyString[] split(char delimiter)
    {
        char[] chars = toCharArrayInternal();
        if(chars.length == 0) {
            return new MyString[]{new MyString("")};
        }
        int delimiterCount = 0;
        for(int i = 0; i < chars.length; i++) {
            if(chars[i] == delimiter) {
                delimiterCount++;
            }
        }
        MyString[] result = new MyString[delimiterCount+1];
        int start=0;
        int resultIndex=0;
        for(int i = 0; i <= chars.length; i++) {
            if(i == chars.length || chars[i] == delimiter) {
                int length = i - start;
                char[] subChars = new char[length];
                for(int j = 0; j < length; j++) {
                    subChars[j] = chars[start+j];
                }
                result[resultIndex++] = new MyString(subChars);
                start=i+1;
            }
        }
        return result;
    }
    public MyString[] split()
    {
        return split(' ');
    }
    private boolean isWhitespace(char ch)
    {
        return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r' || ch == '\f';
    }
    public boolean isEmpty()
    {
        return length() == 0;
    }
    public MyString concat(MyString other)
    {
        if(other == null)
        {
            return this;
        }
        char[] thisChars = toCharArrayInternal();
        char[] otherChars = other.toCharArrayInternal();
        char[] newChars = new char[thisChars.length + otherChars.length];
        for(int i = 0; i < thisChars.length; i++)
        {
            newChars[i] = thisChars[i];
        }
        for(int i = 0; i < otherChars.length; i++)
        {
            newChars[i + thisChars.length] = otherChars[i];
        }
        return new MyString(newChars);
    }
    public MyString toLowerCase()
    {
        char[] chars = toCharArrayInternal();
        char[] newChars = new char[chars.length];
        boolean changed = false;
        for(int i = 0; i < chars.length; i++)
        {
            char ch = chars[i];
            if(ch >= 'A' && ch <= 'Z')
            {
                newChars[i] = (char)(ch + 32);
                changed = true;
            }
            else
            {
                newChars[i] = ch;
            }
        }
        return changed ? new MyString(newChars) : this;
    }
    public MyString toUpperCase()
    {
        char[] chars = toCharArrayInternal();
        char[] newChars = new char[chars.length];
        boolean changed = false;
        for(int i = 0; i < chars.length; i++)
        {
            char ch = chars[i];
            if(ch >= 'a' && ch <= 'z')
            {
                newChars[i] = (char)(ch - 32);
                changed = true;
            }
            else
            {
                newChars[i] = ch;
            }
        }
        return changed ? new MyString(newChars) : this;
    }
    public MyString trim()
    {
        char[] chars = toCharArrayInternal();
        if (chars.length == 0) {
            return this;
        }
        int start = 0;
        while (start < chars.length && isWhitespace(chars[start])) {
            start++;
        }
        int end = chars.length - 1;
        while (end >= start && isWhitespace(chars[end])) {
            end--;
        }
        if (start > end) {
            return new MyString("");
        }
        return substring(start, end + 1);
    }
    public String toString()
    {
        return str;
    }
}
