package objenome.util.bean;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Serializable InvocationHandler for data holding classes. All attributes are accessible via the
 * normal bean getters and setters (get/set/has/is). Furthermore the methods <code>hashCode</code>,
 * <code>equals</code>, <code>toString</code> and <code>clone</code> are implemented.
 * 
 * @author Joachim Baumann
 * @author Peter Fichtner
 */
public class ProxyInvocationHandler implements InvocationHandler, Serializable, Cloneable {

    private static final long serialVersionUID = 0L;

    private final Class<?> proxiedIface;
    private FixedMap<String, Object> data;

    public ProxyInvocationHandler(Class<?> proxiedIface, final Collection<Class<?>> beanClasses) {
        this.proxiedIface = proxiedIface;
        this.data = new FixedMap<String, Object>(countAttributes(beanClasses));
    }

    private static Set<String> countAttributes(final Collection<Class<?>> beanClasses) {
        final Set<String> names = new HashSet<String>();
        for (final Class<?> clazz : beanClasses) {
            for (final PropertyDescriptor propertyDescriptor : ObjectUtil.getBeanInfo(clazz)
                    .getPropertyDescriptors()) {
                names.add(propertyDescriptor.getName());
            }
        }
        return names;
    }

    public Class<?> getProxiedIface() {
        return this.proxiedIface;
    }

    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String methodName = method.getName();
        final String returnTypeName = method.getReturnType().getName();
        final int paramCount = method.getParameterTypes().length;

        // hashCode
        if ("hashCode".equals(methodName) && paramCount == 0 && "int".equals(returnTypeName)) { //$NON-NLS-1$ //$NON-NLS-2$
            return Integer.valueOf(this.data.hashCode());
        }

        // equals
        if ("equals".equals(methodName) && paramCount == 1 && "boolean".equals(returnTypeName)) { //$NON-NLS-1$ //$NON-NLS-2$
            return Boolean.valueOf(proxy == args[0] || args[0] != null
                    && Proxy.isProxyClass(args[0].getClass())
                    && Proxy.getInvocationHandler(args[0]).getClass() == getClass()
                    && this.data.equals(((ProxyInvocationHandler) Proxy.getInvocationHandler(args[0])).data));
        }

        // toString
        if ("toString".equals(methodName) && paramCount == 0 && "java.lang.String".equals(returnTypeName)) { //$NON-NLS-1$ //$NON-NLS-2$
            return "BeanProxy@" + System.identityHashCode(this) + " " + this.data.toString(); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // clone
        if ("clone".equals(methodName) && paramCount == 0 && method.getReturnType().isAssignableFrom(method.getDeclaringClass())) { //$NON-NLS-1$
            final Collection<Class<?>> collectInterfaces = ObjectUtil.collectInterfaces(proxy.getClass());
            return Proxy.newProxyInstance(proxy.getClass().getClassLoader(),
                    collectInterfaces.toArray(new Class<?>[collectInterfaces.size()]), clone());
        }

        /*******************************************************************************************
         * Start with getters and setters
         ******************************************************************************************/

        // setter
        if ("void".equals(returnTypeName) && paramCount == 1) { //$NON-NLS-1$
            for (final PropertyDescriptor descriptor : ObjectUtil.getBeanInfo(method.getDeclaringClass())
                    .getPropertyDescriptors()) {
                if (method.equals(descriptor.getWriteMethod())) {
                    return handleSetter(proxy, descriptor, args);
                }
            }
        }

        // getter
        if (paramCount == 0) {
            final PropertyDescriptor descriptor = getDescriptor(method);
            if (descriptor != null) {
                return handleGetter(proxy, descriptor);
            }
        }

        throw new IllegalArgumentException("Method " + method.getReturnType() + " " + method.getName() + "(" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + Arrays.toString(method.getParameterTypes()) + ") not supported"); //$NON-NLS-1$
    }

    private static PropertyDescriptor getDescriptor(final Method method) {
        for (final PropertyDescriptor descriptor : ObjectUtil.getBeanInfo(method.getDeclaringClass())
                .getPropertyDescriptors()) {
            if (method.equals(descriptor.getReadMethod()) || method.equals(descriptor.getWriteMethod())) {
                return descriptor;
            }
        }
        return null;
    }

    protected Object handleGetter(final Object proxy, final PropertyDescriptor descriptor) {
        final Object val = this.data.get(descriptor.getName());
        // fix null object for primitive types
        final Class<?> returnType = descriptor.getReadMethod().getReturnType();
        return val == null && returnType.isPrimitive() ? WrapperMapper.getNullObject(returnType) : val;
    }

    protected Object handleSetter(final Object proxy, final PropertyDescriptor descriptor, final Object[] args) {
        this.data.put(descriptor.getName(), args[0]);
        return null; // void
    }

    public boolean isSet(final Object proxy, final Method method) {
        final PropertyDescriptor descriptor = getDescriptor(method);
        return descriptor != null && this.data.get(descriptor.getName()) != null;
    }

    protected ProxyInvocationHandler clone() throws CloneNotSupportedException {
        final ProxyInvocationHandler result = (ProxyInvocationHandler) super.clone();
        result.data = new FixedMap<String, Object>(this.data);
        return result;
    }

}