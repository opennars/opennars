package za.co.knonchalant.builder;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import za.co.knonchalant.builder.converters.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Various types available for conversion to GUIs
 */
public enum EType {
    INTEGER(Integer.class, IntegerConverter.class, true),
    INT(int.class, IntegerConverter.class, true),
    DOUBLERANGE(Interval.class, IntervalConverter.class, true),
    STRING(String.class, StandardTextFieldConverter.class, true),
    PATH(String.class, PathFieldConverter.class, false),
    COLLECTION(String.class, CollectionConverter.class, false);

    private static final Map<Class<?>, EType> values = new HashMap<>();
    static {
        for (EType type : EType.values()) {
            if (type.isDefaultForType()) {
                values.put(type.getConvertedClass(), type);
            }
        }
    }

    private final Class<?> stringClass;
    private final Class<? extends IValueFieldConverter> converterClass;
    private final boolean defaultForType;


    EType(Class<?> stringClass, Class<? extends IValueFieldConverter> fieldConverter, boolean defaultForType) {
        this.stringClass = stringClass;
        converterClass = fieldConverter;
        this.defaultForType = defaultForType;
    }

    public static EType getConverterForClass(Class<? extends Object> aClass) {
        return values.get(aClass);
    }

    public boolean isDefaultForType() {
        return defaultForType;
    }

    public Class<?> getConvertedClass() {
        return stringClass;
    }

    public IValueFieldConverter getFieldConverter() {
        try {
            return converterClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }
    }
}
