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
public   enum WrapperMapper {
    ;

    private static final Map<String, Object> MAPPING = Collections.unmodifiableMap(createMapping());

    private static Map<String, Object> createMapping() {
        Map<String, Object> map = new HashMap<>();
        map.put(Double.TYPE.getName(), 0d);
        map.put(Float.TYPE.getName(), 0f);
        map.put(Long.TYPE.getName(), 0L);
        map.put(Integer.TYPE.getName(), 0);
        map.put(Short.TYPE.getName(), (short) 0);
        map.put(Character.TYPE.getName(), (char) 0);
        map.put(Byte.TYPE.getName(), (byte) 0);
        map.put(Boolean.TYPE.getName(), Boolean.FALSE);
        return map;
    }

    public static Object getNullObject(String primitiveTypeName) {
        return MAPPING.get(primitiveTypeName);
    }
}