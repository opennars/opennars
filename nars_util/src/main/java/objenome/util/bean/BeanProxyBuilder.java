package objenome.util.bean;

import objenome.util.bean.anno.GenericBeanKeyProvider;
import objenome.util.bean.anno.Initializer;
import objenome.util.bean.anno.PropertyChangeEventMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Builder for proxied JavaBeans.
 * 
 * @author Peter Fichtner
 * 
 * @param <T> type this Builder will produce
 */
public class BeanProxyBuilder<T> {

    /** Check each class only once so cache the check classes. */
    private static final Set<Class<?>> CHECKED_CLASSES = new CopyOnWriteArraySet<>();

    private final Class<T> iface;

    private boolean check = true;

    private boolean init = true;

    // evaluated by constructor
    private final Collection<Class<?>> allIfaces;

    // pre-set by constructor
    private boolean propertyChangeSupport;

    // pre-set by constructor
    private final boolean genericSupport;

    // -----------------------------------------------------

    private static boolean checkForGenericSupport(Collection<Class<?>> ifaces) {
        for (Class<?> iface : ifaces) {
            for (Method method : iface.getDeclaredMethods()) {
                for (Annotation annotation : method.getAnnotations()) {
                    if (annotation.annotationType().isAnnotationPresent(GenericBeanKeyProvider.class)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void checkIFace(Class<?> clazz) {
        // No synchronization needed because it's no problem to check a class concurrent (and
        // CHECKED_CLASSES is thread-safe)
        // TODO use WeakReferences?
        if (!CHECKED_CLASSES.contains(clazz)) {
            BeanCheck.check(clazz);
            CHECKED_CLASSES.add(clazz);
        }
    }

    public static <T> BeanProxyBuilder<T> on(Class<T> iface) {
        return new BeanProxyBuilder<>(iface);
    }

    private BeanProxyBuilder(Class<T> iface) {
        if (iface == null) {
            throw new IllegalArgumentException("Iface must not be null"); //$NON-NLS-1$
        }
        if (!Modifier.isInterface(iface.getModifiers())) {
            throw new IllegalArgumentException(iface + " must be an interface"); //$NON-NLS-1$
        }
        this.iface = iface;
        allIfaces = new HashSet<>(ObjectUtil.collectInterfaces(this.iface));
        propertyChangeSupport = hasMethodWithAnnotation(allIfaces,
                PropertyChangeEventMethod.class);
        genericSupport = checkForGenericSupport(allIfaces);
    }

    public static boolean hasMethodWithAnnotation(Collection<Class<?>> ifaces,
                                                  Class<? extends Annotation> anno) {
        for (Class<?> iface : ifaces) {
            if (Annotations.hasMethodWithAnnotation(iface, anno)) {
                return true;
            }
        }
        return false;
    }

    /**
     * If <code>true</code> all interfaces are checked for valid getter/setter pairs.
     * 
     * @param check boolean whether to check the classes (interfaces) or not
     * @return the Builder instance
     */
    public BeanProxyBuilder<T> check(boolean check) {
        this.check = check;
        return this;
    }

    /**
     * If <code>true</code> the initialization code will be called (if an interface has
     * initialization code; see {@link Initializer}).
     * 
     * @param init boolean whether to do the initialization code or not
     * 
     * @return the Builder instance
     * @see Initializer
     */
    public BeanProxyBuilder<T> init(boolean init) {
        this.init = init;
        return this;
    }

    /**
     * Creates the bean.
     * 
     * @return newly created bean
     */
    public T build() {
        if (check) {
            checkIFace(iface);
        }
        Class<?>[] array = allIfaces.toArray(new Class<?>[allIfaces.size()]);
        T proxy = iface.cast(Proxy.newProxyInstance(iface.getClassLoader(), array,
                create(iface)));
        if (init) {
            for (Class<?> iface1 : array) {
                BeanInitializer.initialize(iface1, proxy);
            }
        }
        return proxy;
    }

    private InvocationHandler create(Class<T> proxiedIface) {
        ProxyInvocationHandler invocationHandler = propertyChangeSupport ? new ProxyInvocationHandlerPropertyChangeSupport(
                proxiedIface, allIfaces) : new ProxyInvocationHandler(proxiedIface, allIfaces);
        return genericSupport ? new ProxyInvocationHandlerGenericSupport(allIfaces,
                invocationHandler) : invocationHandler;
    }

    public BeanProxyBuilder<T> add(Class<?> class1) {
        allIfaces.add(class1);
        propertyChangeSupport = hasMethodWithAnnotation(allIfaces,
                PropertyChangeEventMethod.class);
        return this;
    }

}
