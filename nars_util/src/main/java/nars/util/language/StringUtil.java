package nars.util.language;

import java.util.ArrayList;

public class StringUtil {
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
    public static <T> boolean contains(final T[] array, final T v) {
        for (final T e : array)
            if (e == v || v != null && v.equals(e))
                return true;

        return false;
    }
    
    public static boolean containsChar(final char[] array, final char v) {
        for (final char e : array)
            if (e == v)
                return true;

        return false;
    }
    
    public static boolean isNumeric(String s) {  
        return s.matches("\\d+");  
    }  
}