package nars.util.data.rope;

import java.lang.reflect.Field;

/**
 * Direct access to String private fields
 */
public   enum StringHack {
    ;

    public static final Field sbval;
    public static final Field val;

    //Add reflection for String value access
    static {
        Field sv = null, sbv = null;
        try {
            sv = String.class.getDeclaredField("value");
            //o = String.class.getDeclaredField("offset");
            sbv = StringBuilder.class.getSuperclass().getDeclaredField("value");

            sv.setAccessible(true);
            sbv.setAccessible(true);
            //o.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        val = sv;
        sbval = sbv;
    }

    public static char[] chars(String s) {

        try {
            return (char[])val.get(s);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return s.toCharArray();
    }
}
