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

    public static boolean hasMethodWithAnnotation(Class<?> iface, Class<? extends Annotation> anno) {
        for (Method method : iface.getDeclaredMethods()) {
            if (method.isAnnotationPresent(anno)) {
                return true;
            }
        }
        return false;
    }

}
