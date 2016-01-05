package objenome.util.bean.util;

import objenome.util.bean.anno.GenericBeanMethod;
import objenome.util.bean.anno.GenericBeanMethod.Type;

import java.lang.reflect.Method;

public   enum GenericBeanMapper {
    ;

    private enum OnNull {
        THROW_EXCEPTION, RETURN_NULL
    }

    /**
     * Map all set values from the src bean to the target bean. To get this work the beans have to
     * define the generic bean methods set/get annotated with {@link GenericBeanMethod} and the
     * appropriate {@link Type}. If the src object defines an isSet method an attribute is only
     * copied if the src attribute was set.
     * 
     * @param <S> source type
     * @param <T> target type
     * @param src the source bean
     * @param target the source bean
     * @return the passed in target bean
     */
    @SuppressWarnings("unchecked")
    public static <S, T> T map(S src, T target) {
        Method get = findMethod(src.getClass(), Type.GENERIC_GET, OnNull.THROW_EXCEPTION);
        Method set = findMethod(target.getClass(), Type.GENERIC_SET, OnNull.THROW_EXCEPTION);
        Method isSet = findMethod(target.getClass(), Type.IS_SET, OnNull.RETURN_NULL);
        try {
            for (Object key : (Iterable<Object>) findMethod(src.getClass(), Type.KEYS,
                    OnNull.THROW_EXCEPTION).invoke(src)) {
                if (isSet == null || (Boolean) isSet.invoke(src, key)) {
                    set.invoke(target, key, get.invoke(src, key));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return target;
    }

    private static Method findMethod(Class<?> clazz, Type type, OnNull onNull) {
        for (Class<?> iface : clazz.getInterfaces()) {
            for (Method method : iface.getMethods()) {
                GenericBeanMethod anno = method.getAnnotation(GenericBeanMethod.class);
                if (anno != null && anno.value().equals(type)) {
                    return method;
                }
            }
        }
        if (onNull == OnNull.RETURN_NULL) {
            return null;
        }
        throw new IllegalStateException(clazz + " does not define a method annotated with " //$NON-NLS-1$
                + GenericBeanMethod.class.getName() + " and type " + Type.KEYS); //$NON-NLS-1$
    }

}
