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
    private static final Set<Class<?>> CHECKED_CLASSES = new CopyOnWriteArraySet<Class<?>>();

    private final Class<T> iface;

    private boolean check = true;

    private boolean init = true;

    // evaluated by constructor
    private final Collection<Class<?>> allIfaces;

    // pre-set by constructor
    private boolean propertyChangeSupport;

    // pre-set by constructor
    private boolean genericSupport;

    // -----------------------------------------------------

    private static boolean checkForGenericSupport(final Collection<Class<?>> ifaces) {
        for (final Class<?> iface : ifaces) {
            for (final Method method : iface.getDeclaredMethods()) {
                for (final Annotation annotation : method.getAnnotations()) {
                    if (annotation.annotationType().isAnnotationPresent(GenericBeanKeyProvider.class)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void checkIFace(final Class<?> clazz) {
        // No synchronization needed because it's no problem to check a class concurrent (and
        // CHECKED_CLASSES is thread-safe)
        // TODO use WeakReferences?
        if (!CHECKED_CLASSES.contains(clazz)) {
            BeanCheck.check(clazz);
            CHECKED_CLASSES.add(clazz);
        }
    }

    public static <T> BeanProxyBuilder<T> on(final Class<T> iface) {
        return new BeanProxyBuilder<T>(iface);
    }

    private BeanProxyBuilder(final Class<T> iface) {
        if (iface == null) {
            throw new IllegalArgumentException("Iface must not be null"); //$NON-NLS-1$
        }
        if (!Modifier.isInterface(iface.getModifiers())) {
            throw new IllegalArgumentException(iface + " must be an interface"); //$NON-NLS-1$
        }
        this.iface = iface;
        this.allIfaces = new HashSet<Class<?>>(ObjectUtil.collectInterfaces(this.iface));
        this.propertyChangeSupport = Annotations.hasMethodWithAnnotation(this.allIfaces,
                PropertyChangeEventMethod.class);
        this.genericSupport = checkForGenericSupport(this.allIfaces);
    }

    /**
     * If <code>true</code> all interfaces are checked for valid getter/setter pairs.
     * 
     * @param check boolean whether to check the classes (interfaces) or not
     * @return the Builder instance
     */
    public BeanProxyBuilder<T> check(final boolean check) {
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
    public BeanProxyBuilder<T> init(final boolean init) {
        this.init = init;
        return this;
    }

    /**
     * Creates the bean.
     * 
     * @return newly created bean
     */
    public T build() {
        if (this.check) {
            checkIFace(this.iface);
        }
        Class<?>[] array = this.allIfaces.toArray(new Class<?>[this.allIfaces.size()]);
        final T proxy = this.iface.cast(Proxy.newProxyInstance(this.iface.getClassLoader(), array,
                create(this.iface)));
        if (this.init) {
            for (final Class<?> iface1 : array) {
                BeanInitializer.initialize(iface1, proxy);
            }
        }
        return proxy;
    }

    private InvocationHandler create(final Class<T> proxiedIface) {
        final ProxyInvocationHandler invocationHandler = this.propertyChangeSupport ? new ProxyInvocationHandlerPropertyChangeSupport(
                proxiedIface, this.allIfaces) : new ProxyInvocationHandler(proxiedIface, this.allIfaces);
        return this.genericSupport ? new ProxyInvocationHandlerGenericSupport(this.allIfaces,
                invocationHandler) : invocationHandler;
    }

    public BeanProxyBuilder<T> add(Class<?> class1) {
        this.allIfaces.add(class1);
        this.propertyChangeSupport = Annotations.hasMethodWithAnnotation(this.allIfaces,
                PropertyChangeEventMethod.class);
        return this;
    }

}
