package objenome.util.bean;

import objenome.util.bean.anno.Initializer;
import objenome.util.bean.anno.InitializerMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public enum BeanInitializer {
    ;

    public static <T> T initialize(Class<?> iface, T bean) {
        Initializer anno = iface.getAnnotation(Initializer.class);
        if (anno != null) {
            try {
                findMethod(anno.value()).invoke(null, bean);
            } catch (Exception e) {
                throw new IllegalStateException("Error initializing " + bean, e); //$NON-NLS-1$
            }
        }
        return bean;
    }

    private static Method findMethod(Class<?> initializerClass) {
        for (Method method : initializerClass.getMethods()) {
            if (method.isAnnotationPresent(InitializerMethod.class)) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalStateException("Method " + method //$NON-NLS-1$
                            + " must be static"); //$NON-NLS-1$
                }
                if (method.getParameterTypes().length != 1
                        || initializerClass.isAssignableFrom(method.getParameterTypes()[0])) {
                    throw new IllegalStateException("Method " + method //$NON-NLS-1$
                            + " must have exactly one parameter of type " + initializerClass.getName()); //$NON-NLS-1$
                }
                return method;
            }
        }
        throw new IllegalStateException(initializerClass
                + " must define one method annotated with " + InitializerMethod.class.getName()); //$NON-NLS-1$
    }

}
