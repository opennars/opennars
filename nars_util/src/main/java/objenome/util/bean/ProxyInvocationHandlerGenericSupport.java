package objenome.util.bean;

import objenome.util.bean.anno.GenericBeanKeyMethod;
import objenome.util.bean.anno.GenericBeanKeyProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static objenome.util.bean.Annotations.isAnnotated;
import static objenome.util.bean.anno.GenericBeanMethod.Type.*;

public class ProxyInvocationHandlerGenericSupport implements InvocationHandler {

    private final ProxyInvocationHandler delegate;

    // TODO This attributes should be pushed to a config object which is created ONCE and passed in
    // by the BeanBuilder
    private final Map<Object, Method> getters, setters;
    private final Set<Object> keys;

    public ProxyInvocationHandlerGenericSupport(final Collection<Class<?>> ifaces,
            final ProxyInvocationHandler delegate) {
        this.delegate = delegate;
        final Map<Object, Method> localGetters = new HashMap<Object, Method>();
        final Map<Object, Method> localSetters = new HashMap<Object, Method>();
        try {
            fillMaps(ifaces, localGetters, localSetters);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        this.getters = Collections.unmodifiableMap(localGetters);
        this.setters = Collections.unmodifiableMap(localSetters);
        this.keys = Collections.unmodifiableSet(determineKeys(localGetters, localSetters));
    }

    private static Set<Object> determineKeys(final Map<Object, Method> gm, final Map<Object, Method> sm) {
        final Set<Object> result = new HashSet<Object>(gm.keySet().size() + sm.keySet().size());
        result.addAll(gm.keySet());
        result.addAll(sm.keySet());
        return result;
    }

    private void fillMaps(final Collection<Class<?>> ifaces, final Map<Object, Method> gm,
            final Map<Object, Method> sm) throws SecurityException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        // TODO Check all methods to be valid in dependency of their annotations (e.g. if annotated
        // with Type.SETTER it has to have TWO parameters and returntype void)
        for (final Class<?> iface : ifaces) {
            final List<Method> getters = Arrays.asList(ObjectUtil.getAllGetters(iface));
            final List<Method> setters = Arrays.asList(ObjectUtil.getAllSetters(iface));
            for (final Method method : iface.getMethods()) {
                for (final Annotation annotation : method.getAnnotations()) {
                    final GenericBeanKeyProvider beanKeyAnno = annotation.annotationType().getAnnotation(
                            GenericBeanKeyProvider.class);
                    if (beanKeyAnno != null) {
                        final Object value = getGetAttributeMethod(annotation).invoke(annotation);
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

    private Method getGetAttributeMethod(final Annotation annotation) throws SecurityException,
            NoSuchMethodException {
        final Class<? extends Annotation> annotationType = annotation.annotationType();
        for (final Method method : annotationType.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GenericBeanKeyMethod.class)) {
                return method;
            }
        }
        return annotationType.getMethod("value"); //$NON-NLS-1$
    }

    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (isAnnotated(method, GENERIC_GET)) {
            return this.getters.get(args[0]).invoke(proxy);
        } else if (isAnnotated(method, GENERIC_SET)) {
            return this.setters.get(args[0]).invoke(proxy, args[1]);
        } else if (isAnnotated(method, KEYS)) {
            return this.keys;
        } else if (isAnnotated(method, IS_SET)) {
            return Boolean.valueOf(this.delegate.isSet(proxy, this.getters.get(args[0])));
        } else {
            return this.delegate.invoke(proxy, method, args);
        }
    }

}
