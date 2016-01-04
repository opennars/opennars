package objenome.util.bean;

import objenome.util.bean.anno.IgnoreVeto;
import objenome.util.bean.anno.PropertyChangeEventMethod;
import objenome.util.bean.anno.PropertyChangeEventMethod.Type;
import objenome.util.bean.anno.Unbound;
import objenome.util.bean.listener.BeanListenerSupport;
import objenome.util.bean.listener.BeanListenerSupportSoftRef;

import java.beans.*;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * ProxyInvocationHandler that supports PropertyChangeSupport.
 * 
 * @author Peter Fichtner
 */
public class ProxyInvocationHandlerPropertyChangeSupport extends ProxyInvocationHandler {

    private static final long serialVersionUID = 0L;

    // TODO switch Soft/Hard
    private BeanListenerSupport<PropertyChangeListener> propertyChangeListeners = new BeanListenerSupportSoftRef<>();
    private BeanListenerSupport<VetoableChangeListener> vetoableChangeListeners = new BeanListenerSupportSoftRef<>();

    public ProxyInvocationHandlerPropertyChangeSupport(Class<?> proxiedIface, Collection<Class<?>> ifaces) {
        super(proxiedIface, ifaces);
        // TODO Check all methods to be valid in dependency of their annotations (e.g. returntypes
        // void, parameters in dependency of type (veto/non-veto)
        // PropertyChangeListener/VetoableChangeListener)
    }

    public static boolean isAnnotated(Method method,
                                      Type type) {
        PropertyChangeEventMethod annotation = method.getAnnotation(PropertyChangeEventMethod.class);
        return annotation != null && annotation.value().equals(type);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args != null && args.length == 1) {
            if (isAnnotated(method, Type.ADD_LISTENER)) {
                addPropertyChangeListener((PropertyChangeListener) args[0]);
                return null;
            } else if (isAnnotated(method, Type.REMOVE_LISTENER)) {
                removePropertyChangeListener((PropertyChangeListener) args[0]);
                return null;
            } else if (isAnnotated(method, Type.ADD_VETO_LISTENER)) {
                addVetoableChangeListener((VetoableChangeListener) args[0]);
                return null;
            } else if (isAnnotated(method, Type.REMOVE_VETO_LISTENER)) {
                removeVetoableChangeListener((VetoableChangeListener) args[0]);
                return null;
            }
        }
        return super.invoke(proxy, method, args);
    }

    private void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeListeners.remove(listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeListeners.add(listener);
    }

    public void addVetoableChangeListener(VetoableChangeListener listener) {
        vetoableChangeListeners.add(listener);
    }

    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        vetoableChangeListeners.remove(listener);
    }

    @Override
    protected Object handleSetter(Object proxy, PropertyDescriptor descriptor, Object[] args) {
        PropertyChangeEvent event = new PropertyChangeEvent(proxy, descriptor.getName(), handleGetter(
                proxy, descriptor), args[0]);
        boolean announce = !descriptor.getWriteMethod().isAnnotationPresent(Unbound.class);
        if (announce && checkForVeto(event)
                && !descriptor.getWriteMethod().isAnnotationPresent(IgnoreVeto.class)) {
            return null;
        }
        Object result = super.handleSetter(proxy, descriptor, args);
        if (announce) {
            informListeners(event);
        }
        return result;
    }

    /**
     * Inform the VetoableChangeListeners.
     * 
     * @param event the event to pass to
     *            {@link VetoableChangeListener#vetoableChange(PropertyChangeEvent)}
     * @return <code>true</code> if there's a veto else <code>false</code>
     */
    private boolean checkForVeto(PropertyChangeEvent event) {
        try {
            for (VetoableChangeListener listener : vetoableChangeListeners) {
                listener.vetoableChange(event);
            }
            return false;
        } catch (PropertyVetoException e) {
            return true;
        }
    }

    /**
     * Inform the PropertyChangeListeners.
     * 
     * @param event the event to pass to
     *            {@link PropertyChangeListener#propertyChange(PropertyChangeEvent)}
     */
    private void informListeners(PropertyChangeEvent evt) {
        for (PropertyChangeListener listener : propertyChangeListeners) {
            listener.propertyChange(evt);
        }
    }

    @Override
    protected ProxyInvocationHandlerPropertyChangeSupport clone() throws CloneNotSupportedException {
        ProxyInvocationHandlerPropertyChangeSupport result = (ProxyInvocationHandlerPropertyChangeSupport) super
                .clone();
        result.propertyChangeListeners = propertyChangeListeners.clone();
        result.vetoableChangeListeners = vetoableChangeListeners.clone();
        return result;
    }
}