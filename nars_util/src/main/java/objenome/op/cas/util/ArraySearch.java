package objenome.op.cas.util;

import java.lang.reflect.Array;
import java.util.Arrays;

public final class ArraySearch {
    
    public static void main(String[] args) {
        String[][] tmp1 = {{"help","yep"},
                           {"yep", "nope"},
                           {"good", "bad"}};
        System.out.println(Arrays.deepToString(tmp1));
        System.out.println("help index: " + index(tmp1, "help"));
        System.out.println("yep index: " + index(tmp1, "yep"));
        System.out.println("nope index: " + index(tmp1, "nope"));
        System.out.println("good index: " + index(tmp1, "good"));
        System.out.println("bad index: " + index(tmp1, "bad"));
        System.out.println("mustard index: " + index(tmp1, "mustard"));
    }
    
    public static boolean contains(Object[] array, Object searchFor) {
        for (Object o : array) {
            if (o.equals(searchFor)) return true;
        }
        return false;
    }
    
    public static int index(Object[][] array, Object searchFor) {
        for (int i = 0; i < Array.getLength(array); i++) {
            if (contains(array[i], searchFor)) return i;
        }
        return -1;
    }
    
}
