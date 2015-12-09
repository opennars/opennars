package objenome.util.bean;

import objenome.util.bean.anno.GenericBeanMethod;
import objenome.util.bean.anno.PropertyChangeEventMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

public final class Annotations {

    private Annotations() {
        throw new IllegalStateException();
    }

    public static boolean isAnnotated(Method method,
                                      objenome.util.bean.anno.GenericBeanMethod.Type type) {
        GenericBeanMethod annotation = method.getAnnotation(GenericBeanMethod.class);
        return annotation != null && annotation.value().equals(type);
    }

    public static boolean isAnnotated(Method method,
                                      objenome.util.bean.anno.PropertyChangeEventMethod.Type type) {
        PropertyChangeEventMethod annotation = method.getAnnotation(PropertyChangeEventMethod.class);
        return annotation != null && annotation.value().equals(type);
    }

    public static boolean hasMethodWithAnnotation(Collection<Class<?>> ifaces,
                                                  Class<? extends Annotation> anno) {
        for (Class<?> iface : ifaces) {
            if (hasMethodWithAnnotation(iface, anno)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasMethodWithAnnotation(Class<?> iface, Class<? extends Annotation> anno) {
        for (Method method : iface.getDeclaredMethods()) {
            if (method.isAnnotationPresent(anno)) {
                return true;
            }
        }
        return false;
    }

}
