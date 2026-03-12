package libraries.hashing;

import libraries.collections.MyString;

public class hashFunctionTest {
    public static void main(String[] args) {
        MyString myString = new MyString("A");
        System.out.println(HashFunction.hashPassword(myString));
    }
}
