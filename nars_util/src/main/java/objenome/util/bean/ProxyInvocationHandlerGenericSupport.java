package objenome.util.bean;

import objenome.util.bean.anno.GenericBeanKeyMethod;
import objenome.util.bean.anno.GenericBeanKeyProvider;
import objenome.util.bean.anno.GenericBeanMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static objenome.util.bean.ProxyInvocationHandlerPropertyChangeSupport.isAnnotated;
import static objenome.util.bean.anno.GenericBeanMethod.Type.*;

public class ProxyInvocationHandlerGenericSupport implements InvocationHandler {

    private final ProxyInvocationHandler delegate;

    // TODO This attributes should be pushed to a config object which is created ONCE and passed in
    // by the BeanBuilder
    private final Map<Object, Method> getters, setters;
    private final Set<Object> keys;

    public ProxyInvocationHandlerGenericSupport(Collection<Class<?>> ifaces,
                                                ProxyInvocationHandler delegate) {
        this.delegate = delegate;
        Map<Object, Method> localGetters = new HashMap<>();
        Map<Object, Method> localSetters = new HashMap<>();
        try {
            fillMaps(ifaces, localGetters, localSetters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        getters = Collections.unmodifiableMap(localGetters);
        setters = Collections.unmodifiableMap(localSetters);
        keys = Collections.unmodifiableSet(determineKeys(localGetters, localSetters));
    }

    private static Set<Object> determineKeys(Map<Object, Method> gm, Map<Object, Method> sm) {
        Set<Object> result = new HashSet<>(gm.keySet().size() + sm.keySet().size());
        result.addAll(gm.keySet());
        result.addAll(sm.keySet());
        return result;
    }

    public static boolean isAnnotated(Method method,
                                      GenericBeanMethod.Type type) {
        GenericBeanMethod annotation = method.getAnnotation(GenericBeanMethod.class);
        return annotation != null && annotation.value().equals(type);
    }

    private void fillMaps(Collection<Class<?>> ifaces, Map<Object, Method> gm,
                          Map<Object, Method> sm) throws SecurityException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        // TODO Check all methods to be valid in dependency of their annotations (e.g. if annotated
        // with Type.SETTER it has to have TWO parameters and returntype void)
        for (Class<?> iface : ifaces) {
            List<Method> getters = Arrays.asList(ObjectUtil.getAllGetters(iface));
            List<Method> setters = Arrays.asList(ObjectUtil.getAllSetters(iface));
            for (Method method : iface.getMethods()) {
                for (Annotation annotation : method.getAnnotations()) {
                    GenericBeanKeyProvider beanKeyAnno = annotation.annotationType().getAnnotation(
                            GenericBeanKeyProvider.class);
                    if (beanKeyAnno != null) {
                        Object value = getGetAttributeMethod(annotation).invoke(annotation);
                        if (getters.contains(method)) {
                            gm.put(value, method);
                        } else if (setters.contains(method)) {
                            sm.put(value, method);
                        } else {
                            throw new IllegalStateException("Method " + method + " is annotated with " //$NON-NLS-1$ //$NON-NLS-2$
                                    + annotation.annotationType()
                                    + " but it seems not be a valid getter nor setter"); //$NON-NLS-1$
                        }
                    }
                }
            }
        }
    }

    private Method getGetAttributeMethod(Annotation annotation) throws SecurityException,
            NoSuchMethodException {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        for (Method method : annotationType.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GenericBeanKeyMethod.class)) {
                return method;
            }
        }
        return annotationType.getMethod("value"); //$NON-NLS-1$
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //noinspection IfStatementWithTooManyBranches
        if (isAnnotated(method, GENERIC_GET)) {
            return getters.get(args[0]).invoke(proxy);
        } else if (isAnnotated(method, GENERIC_SET)) {
            return setters.get(args[0]).invoke(proxy, args[1]);
        } else if (isAnnotated(method, KEYS)) {
            return keys;
        } else if (isAnnotated(method, IS_SET)) {
            return delegate.isSet(proxy, getters.get(args[0]));
        } else {
            return delegate.invoke(proxy, method, args);
        }
    }

}
