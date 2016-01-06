package objenome.util.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public   enum Annotations {
    ;

    public static boolean hasMethodWithAnnotation(Class<?> iface, Class<? extends Annotation> anno) {
        for (Method method : iface.getDeclaredMethods()) {
            if (method.isAnnotationPresent(anno)) {
                return true;
            }
        }
        return false;
    }

}
