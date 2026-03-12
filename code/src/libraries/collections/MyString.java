package libraries.collections;

public class MyString {
    private final char[] data;
    private final int size;
    public MyString(String str) {
        if (str == null) {
            this.data = new char[0];
            this.size = 0;
        } else {
            this.size = str.length();
            this.data = new char[size];
            for (int i = 0; i < size; i++) {
                this.data[i] = str.charAt(i);
            }
        }
    }
    public MyString(char[] chars) {
        if (chars == null) {
            this.data = new char[0];
            this.size = 0;
        } else {
            int len = 0;
            while (len < chars.length && chars[len] != '\0') len++;
            this.size = len;
            this.data = new char[size];
            for (int i = 0; i < size; i++) this.data[i] = chars[i];
        }
    }
    public int length() {
        return size;
    }
    public char charAt(int index) {
        if (index < 0 || index >= size) throw new StringIndexOutOfBoundsException(index);
        return data[index];
    }
    public String getValue() {
        return new String(data);
    }
    public char[] toCharArray() {
        char[] copy = new char[size];
        for (int i = 0; i < size; i++) copy[i] = data[i];
        return copy;
    }
    public boolean equals(MyString other) {
        if (this == other) return true;
        if (other == null || size != other.size) return false;
        for (int i = 0; i < size; i++) {
            if (data[i] != other.data[i]) return false;
        }
        return true;
    }
    public int compareTo(MyString other) {
        if (other == null) return 1;
        int min = size < other.size ? size : other.size;
        for (int i = 0; i < min; i++) {
            if (data[i] != other.data[i]) return data[i] - other.data[i];
        }
        return size - other.size;
    }
    public MyString substring(int begin, int end) {
        if (begin < 0) begin = 0;
        if (end > size) end = size;
        if (begin >= end) return new MyString("");
        char[] sub = new char[end - begin];
        for (int i = begin; i < end; i++) sub[i - begin] = data[i];
        return new MyString(sub);
    }
    public int indexOf(char ch) {
        for (int i = 0; i < size; i++) {
            if (data[i] == ch) return i;
        }
        return -1;
    }
    public MyString[] split(char delimiter) {
        if (size == 0) return new MyString[]{new MyString("")};
        int count = 1;
        for (int i = 0; i < size; i++) if (data[i] == delimiter) count++;
        MyString[] result = new MyString[count];
        int start = 0, idx = 0;
        for (int i = 0; i <= size; i++) {
            if (i == size || data[i] == delimiter) {
                result[idx++] = substring(start, i);
                start = i + 1;
            }
        }
        return result;
    }
    public MyString[] split() {
        return split(' ');
    }
    public boolean isEmpty() {
        return size == 0;
    }
    public MyString concat(MyString other) {
        if (other == null || other.size == 0) return this;
        char[] res = new char[size + other.size];
        for (int i = 0; i < size; i++) res[i] = data[i];
        for (int i = 0; i < other.size; i++) res[size + i] = other.data[i];
        return new MyString(res);
    }
    public MyString toLowerCase() {
        char[] res = new char[size];
        boolean mod = false;
        for (int i = 0; i < size; i++) {
            res[i] = (data[i] >= 'A' && data[i] <= 'Z') ? (char)(data[i] + 32) : data[i];
            if (res[i] != data[i]) mod = true;
        }
        return mod ? new MyString(res) : this;
    }
    public MyString toUpperCase() {
        char[] res = new char[size];
        boolean mod = false;
        for (int i = 0; i < size; i++) {
            res[i] = (data[i] >= 'a' && data[i] <= 'z') ? (char)(data[i] - 32) : data[i];
            if (res[i] != data[i]) mod = true;
        }
        return mod ? new MyString(res) : this;
    }
    public MyString trim() {
        int s = 0, e = size - 1;
        while (s < size && data[s] <= ' ') s++;
        while (e > s && data[e] <= ' ') e--;
        return (s == 0 && e == size - 1) ? this : substring(s, e + 1);
    }
    public static MyString intToHex(int num) {
        char[] hexDigits = {
                '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };
        char[] buffer = new char[8];
        for (int i = 7; i >= 0; i--) {
            int nibble = num & 0xF;
            buffer[i] = hexDigits[nibble];
            num = num >>> 4;
        }

        return new MyString(buffer);
    }
    public String toString() {
        return new String(data);
    }

    public boolean contains(MyString sub){
        if (sub == null) return false;
        if (sub.size == 0) return true;
        if (sub.size > this.size) return false;

        for (int i = 0; i <= this.size - sub.size; i++){
            boolean ok = true;
            for (int j = 0; j < sub.size; j++){
                if (this.data[i+j] != sub.data[j]){
                    ok = false;
                    break;
                }
            }
            if (ok) return true;
        }
        return false;
    }

    public boolean containsAny(MyString ... keys){
        if (keys == null) return false;

        for (int i = 0; i < keys.length; i++){
            MyString k = keys[i];
            if (k == null || k.size == 0) continue;
            if (this.contains(k)) return true;
        }
        return false;
    }

    public static MyString join(char delimiter, MyString ... parts){
        if (parts == null || parts.length == 0) return new MyString("");

        // compute total size needed
        int totalLength = 0;
        for (int i = 0; i < parts.length; i++){
            if (parts[i] != null) totalLength += parts[i].size;
        }
        // add delimiter count between elements (n - 1 delimiters)
        totalLength += parts.length - 1;
        // edge case
        if (totalLength <= 0) return new MyString("");

        // build final char array
        char[] result = new char[totalLength];
        int position = 0;

        for (int i = 0; i < parts.length; i++){
            if (i > 0) result[position++] = delimiter;

            MyString p = parts[i];
            if (p == null || p.size == 0) continue;

            for (int j = 0; j < p.size; j++){
                result[position++] = p.data[j];
            }
        }

        return new MyString(result);
    }

    public MyString replace(char oldCh, char newCh) {
        if (size == 0) return this;

        char[] res = new char[size];
        boolean mod = false;

        for (int i = 0; i < size; i++) {
            char c = data[i];
            if (c == oldCh) {
                res[i] = newCh;
                mod = true;
            } else {
                res[i] = c;
            }
        }

        return mod ? new MyString(res) : this;
    }
}