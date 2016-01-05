package nars.util.io;

import java.util.ArrayList;
import java.util.Objects;

public enum StringUtil {
    ;

    public static String[] splitInclusive(CharSequence input, char[] splitChars) {
        ArrayList<String> splited = new ArrayList<>();
        String readthusfar = "";
        
        for (int i = 0; i < input.length(); i++) {
            char readChar = input.charAt(i);
            
            if (containsChar(splitChars, readChar)) {
                if (!readthusfar.isEmpty()) {
                    splited.add(readthusfar);
                    readthusfar = "";
                }
                
                splited.add(String.valueOf(readChar));
                
                continue;
            }
            
            readthusfar += readChar;
        }
        
        if (!readthusfar.isEmpty()) {
            splited.add(readthusfar);
        }
        
        String[] resultAsArray = new String[splited.size()];
        resultAsArray = splited.toArray(resultAsArray);
        
        return resultAsArray;
    }
    
    // http://stackoverflow.com/questions/1128723/in-java-how-can-i-test-if-an-array-contains-a-certain-value
    public static <T> boolean contains(T[] array, T v) {
        for (T e : array)
            if (Objects.equals(v, e))
                return true;

        return false;
    }
    
    public static boolean containsChar(char[] array, char v) {
        for (char e : array)
            if (e == v)
                return true;

        return false;
    }
    
    public static boolean isNumeric(String s) {  
        return s.matches("\\d+");  
    }  
}