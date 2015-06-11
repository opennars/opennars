package nars.util.language;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.Map;

/**
 * JSON utilities
 */
public class JSON {

    static com.fasterxml.jackson.jr.ob.JSON def =
            com.fasterxml.jackson.jr.ob.JSON.std.
                    with(com.fasterxml.jackson.jr.ob.JSON.Feature.WRITE_NULL_PROPERTIES);

    static com.fasterxml.jackson.jr.ob.JSON unRef =
            com.fasterxml.jackson.jr.ob.JSON.std.
                    without(com.fasterxml.jackson.jr.ob.JSON.Feature.FORCE_REFLECTION_ACCESS);




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
    public static String stringFromUnreflected(Object obj) {
        try {
            return unRef.asString(obj);
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
