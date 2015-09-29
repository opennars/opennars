package org.clockwise.util;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ClassUtils {

    /**
     * Determine whether the given class has a public method with the given
     * signature.
     * <p>
     * Essentially translates {@code NoSuchMethodException} to "false".
     * 
     * @param clazz
     *            the clazz to analyze
     * @param methodName
     *            the name of the method
     * @param paramTypes
     *            the parameter types of the method
     * @return whether the class has a corresponding method
     * @see Class#getMethod
     */
    public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return (getMethodIfAvailable(clazz, methodName, paramTypes) != null);
    }

    /**
     * Determine whether the given class has a public method with the given
     * signature, and return it if available (else return {@code null}).
     * <p>
     * In case of any signature specified, only returns the method if there is a
     * unique candidate, i.e. a single public method with the specified name.
     * <p>
     * Essentially translates {@code NoSuchMethodException} to {@code null}.
     * 
     * @param clazz
     *            the clazz to analyze
     * @param methodName
     *            the name of the method
     * @param paramTypes
     *            the parameter types of the method (may be {@code null} to
     *            indicate any signature)
     * @return the method, or {@code null} if not found
     * @see Class#getMethod
     */
    public static Method getMethodIfAvailable(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        // Assert.notNull(clazz, "Class must not be null");
        // Assert.notNull(methodName, "Method name must not be null");
        if (paramTypes != null) {
            try {
                return clazz.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ex) {
                return null;
            }
        } else {
            Set<Method> candidates = new HashSet<Method>(1);
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (methodName.equals(method.getName())) {
                    candidates.add(method);
                }
            }
            if (candidates.size() == 1) {
                return candidates.iterator().next();
            }
            return null;
        }
    }

    public static String getShortName(Class<? extends CustomizableThreadCreator> class1) {
        // TODO Auto-generated method stub
        return null;
    }
}
