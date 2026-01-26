package libraries.hashing;

import libraries.collections.MyString;

public class HashFunction {
    private static MyString xorHash(MyString input) {
        if (input == null || input.isEmpty()) {
            return new MyString("0");
        }
        char[] chars = input.toCharArray();
        int length = input.length();
        int hash1 = 5381;
        int hash2 = 0;
        int hash3 = length;
        for (int i = 0; i < length; i++) {
            char c = chars[i];
            hash1 = ((hash1 << 5) + hash1) ^ c; // fancy way to write hash1 = (hash1 * 33) ^ c
            hash2 = (hash2 ^ c) * 31;
            hash3 = hash3 ^ (c << (i % 16));
        }
        int combinedHash = hash1 ^ hash2 ^ hash3;
        int positiveHash = combinedHash & 0x7FFFFFFF;
        return MyString.intToHex(positiveHash);
    }

    public static MyString hashPassword(MyString password) {
        return xorHash(password);
    }
}