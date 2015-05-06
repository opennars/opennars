package objenome.util.bean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class maps primitive types to their default values.
 * 
 * @author Joachim Baumann
 * @author Peter Fichtner
 */
public final class WrapperMapper {

    private static final Map<String, Object> MAPPING = Collections.unmodifiableMap(createMapping());

    /** All methods are static */
    private WrapperMapper() {
        throw new IllegalStateException();
    }

    private static Map<String, Object> createMapping() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(Double.TYPE.getName(), Double.valueOf(0));
        map.put(Float.TYPE.getName(), Float.valueOf(0));
        map.put(Long.TYPE.getName(), Long.valueOf(0));
        map.put(Integer.TYPE.getName(), Integer.valueOf(0));
        map.put(Short.TYPE.getName(), Short.valueOf((short) 0));
        map.put(Character.TYPE.getName(), Character.valueOf((char) 0));
        map.put(Byte.TYPE.getName(), Byte.valueOf((byte) 0));
        map.put(Boolean.TYPE.getName(), Boolean.FALSE);
        return map;
    }

    public static Object getNullObject(final Class<?> primitiveType) {
        return getNullObject(primitiveType.getName());
    }

    public static Object getNullObject(final String primitiveTypeName) {
        return MAPPING.get(primitiveTypeName);
    }
}