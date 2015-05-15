package nars.util.language;

import java.io.IOException;
import java.util.Map;

/**
 * JSON utilities
 */
public class JSON {

    static com.fasterxml.jackson.jr.ob.JSON def =
            com.fasterxml.jackson.jr.ob.JSON.std.
                    with(com.fasterxml.jackson.jr.ob.JSON.Feature.WRITE_NULL_PROPERTIES);



    public static Map<String,Object> toMap(String json) {
        try {
            return def.mapFrom(json);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object toObj(String json) {
        try {
            return def.anyFrom(json);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String stringFrom(Object obj) {
        try {
            return def.asString(obj);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String,Object> mapFrom(Object obj) {
        try {
            return def.mapFrom(obj);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
