package objenome.util.bean;

import java.lang.reflect.Method;

/**
 * A checker class which is able to check a class of its correctness regarding the getters and
 * setters.
 * 
 * @author Peter Fichtner
 */
public final class BeanCheck {

    private BeanCheck() {
        throw new IllegalStateException();
    }

    /**
     * Check the passed class for its getter/setter correctness. If the class is invalid a
     * <code>RuntimeException</code> is thrown.<br>
     * A class is not valid if it defines a setter without the appropriate getter or vice versa.
     * 
     * @param clazz class to check
     */
    public static void check(final Class<?> clazz) {
        searchSetterForGetters(clazz);
        searchGetterForSetters(clazz);
    }

    private static void searchGetterForSetters(final Class<?> clazz) {
        for (final Method setter : ObjectUtil.getAllSetters(clazz)) {
            if (ObjectUtil.getGetter(clazz, setter) == null) {
                throw new IllegalArgumentException("Invalid interface " + clazz.getName() + //$NON-NLS-1$
                        ": No getter found for setter " + setter.getName()); //$NON-NLS-1$
            }
        }
    }

    private static void searchSetterForGetters(final Class<?> clazz) {
        for (final Method getter : ObjectUtil.getAllGetters(clazz)) {
            if (ObjectUtil.getSetter(clazz, getter) == null) {
                throw new IllegalArgumentException("Invalid interface " + clazz.getName() + //$NON-NLS-1$
                        ": No setter found for getter " + getter.getName()); //$NON-NLS-1$
            }
        }
    }

}
