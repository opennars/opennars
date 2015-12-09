package objenome.util.bean;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Helper class with static methods for accessing bean methods (getters/setters).
 * 
 * @author Peter Fichtner
 */
public final class ObjectUtil {

    private ObjectUtil() {
        throw new IllegalStateException();
    }

    /**
     * Get all valid getters for the passed class <code>clazz</code>.
     * 
     * @param clazz class to determine getters from
     * @return all valid getters of class <code>clazz</code>
     */
    public static Method[] getAllGetters(final Class<?> clazz) {
        final List<Method> methods = new ArrayList<>();
        fillGetters(clazz, methods);
        for (final Class<?> parent : clazz.getInterfaces()) {
            fillGetters(parent, methods);
        }
        return methods.toArray(new Method[methods.size()]);
    }

    protected static void fillGetters(final Class<?> clazz, final Collection<Method> methods) {
        final PropertyDescriptor[] descriptors = getBeanInfo(clazz).getPropertyDescriptors();
        for (final PropertyDescriptor descriptor : descriptors) {
            final Method method = descriptor.getReadMethod();
            // filter java.lang.Object#getClass()
            if (method != null
                    && !("getClass".equals(method.getName()) && "java.lang.Object".equals(method.getDeclaringClass().getName()) && "java.lang.Class".equals(method.getReturnType().getName()) && method.getParameterTypes().length == 0)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                methods.add(method);
            }
        }
    }

    /**
     * Get all valid setters for the passed class <code>clazz</code>.
     * 
     * @param clazz class to determine setters from
     * @return all valid setters of class <code>clazz</code>
     */
    public static Method[] getAllSetters(final Class<?> clazz) {
        final List<Method> methods = new ArrayList<>();
        fillSetters(clazz, methods);
        for (final Class<?> parent : clazz.getInterfaces()) {
            fillSetters(parent, methods);
        }
        return methods.toArray(new Method[methods.size()]);
    }

    protected static void fillSetters(final Class<?> clazz, final Collection<Method> methods) {
        for (final PropertyDescriptor propertyDescriptor : getBeanInfo(clazz).getPropertyDescriptors()) {
            final Method method = propertyDescriptor.getWriteMethod();
            if (method != null) {
                methods.add(method);
            }
        }
    }

    public static BeanInfo getBeanInfo(final Class<?> clazz) {
        try {
            return Introspector.getBeanInfo(clazz);
        } catch (final IntrospectionException e) {
            throw new IllegalStateException("Error getting  BeanInfo for " //$NON-NLS-1$
                    + clazz);
        }
    }

    /**
     * Gets the appropriate getter for the passed setter.
     * 
     * @param clazz clazz to determine getter from
     * @param setter setter for which the appropriate getter should be returned
     * @return the appropriate getter for the passed setter or <code>null</code> if no appropriate
     *         getter is available
     */
    public static Method getGetter(final Class<?> clazz, final Method setter) {
        return getGetter(clazz, setter, clazz);
    }

    private static Method getGetter(final Class<?> clazz, final Method setter, final Class<?> baseClass) {
        if (clazz == null) {
            return null;
        }
        for (final PropertyDescriptor descriptor : getBeanInfo(clazz).getPropertyDescriptors()) {
            if (descriptor.getWriteMethod() != null
                    && checkNameAndParameterTypes(setter, descriptor.getWriteMethod())) {
                // we found the setter
                return findGetter(baseClass, descriptor);
            }
        }
        for (final Class<?> parent : clazz.getInterfaces()) {
            final Method getter = getGetter(parent, setter, baseClass);
            if (getter != null) {
                return getter;
            }
        }
        return null;
    }

    private static Method findGetter(final Class<?> clazz, final PropertyDescriptor descriptor) {
        for (final PropertyDescriptor d : getBeanInfo(clazz).getPropertyDescriptors()) {
            if (compare(d, descriptor) && d.getReadMethod() != null) {
                return d.getReadMethod();
            }
        }
        for (final Class<?> parent : clazz.getInterfaces()) {
            final Method setter = findGetter(parent, descriptor);
            if (setter != null) {
                return setter;
            }
        }
        return null;
    }

    private static boolean compare(final PropertyDescriptor a, final PropertyDescriptor b) {
        return a.getName().equals(b.getName()) && a.getPropertyType().equals(b.getPropertyType());
    }

    /**
     * Gets the appropriate setter for the passed getter.
     * 
     * @param clazz class to determine setter from
     * @param getter getter for which the appropriate setter should be returned
     * @return the appropriate setter for the passed getter or <code>null</code> if no appropriate
     *         setter is available
     */
    public static Method getSetter(final Class<?> clazz, final Method getter) {
        return getSetter(clazz, getter, clazz);
    }

    protected static Method getSetter(final Class<?> clazz, final Method getter, final Class<?> baseClass) {
        if (clazz == null) {
            return null;
        }
        for (final PropertyDescriptor descriptor : getBeanInfo(clazz).getPropertyDescriptors()) {
            if (descriptor.getReadMethod() != null
                    && checkNameAndReturnType(getter, descriptor.getReadMethod())) {
                // we found the getter
                return findSetter(baseClass, descriptor);
            }
        }
        for (final Class<?> parent : clazz.getInterfaces()) {
            final Method setter = getSetter(parent, getter, baseClass);
            if (setter != null) {
                return setter;
            }
        }
        return null;
    }

    private static Method findSetter(final Class<?> clazz, final PropertyDescriptor descriptor) {
        for (final PropertyDescriptor d : getBeanInfo(clazz).getPropertyDescriptors()) {
            if (compare(d, descriptor) && d.getWriteMethod() != null) {
                return d.getWriteMethod();
            }
        }
        for (final Class<?> parent : clazz.getInterfaces()) {
            final Method setter = findSetter(parent, descriptor);
            if (setter != null) {
                return setter;
            }
        }
        return null;
    }

    private static boolean checkNameAndParameterTypes(final Method a, final Method b) {
        return a == b
                || (a != null && b != null && a.getName().equals(b.getName()) && Arrays.equals(
                        a.getParameterTypes(), b.getParameterTypes()));
    }

    private static boolean checkNameAndReturnType(final Method a, final Method b) {
        return a == b
                || (a != null && b != null && a.getName().equals(b.getName()) && a.getReturnType().equals(
                        b.getReturnType()));
    }

    public static Collection<Class<?>> collectInterfaces(final Class<?> clazz) {
        final Collection<Class<?>> all = new HashSet<>();
        if (Modifier.isInterface(clazz.getModifiers())) {
            all.add(clazz);
        }
        collectInterfaces(clazz, all);
        return all;
    }

    private static void collectInterfaces(final Class<?> clazz, final Collection<Class<?>> list) {
        final Class<?>[] ifaces = clazz.getInterfaces();
        if (ifaces.length == 0) {
            return;
        }
        Collections.addAll(list, ifaces);
        for (final Class<?> iface : ifaces) {
            collectInterfaces(iface, list);
        }
    }

}
