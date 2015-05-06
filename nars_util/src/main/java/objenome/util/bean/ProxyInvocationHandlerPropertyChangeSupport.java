package objenome.util.bean;

import static objenome.util.bean.Annotations.isAnnotated;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.Method;
import java.util.Collection;

import objenome.util.bean.anno.IgnoreVeto;
import objenome.util.bean.anno.PropertyChangeEventMethod.Type;
import objenome.util.bean.anno.Unbound;
import objenome.util.bean.listener.BeanListenerSupport;
import objenome.util.bean.listener.BeanListenerSupportSoftRef;

/**
 * ProxyInvocationHandler that supports PropertyChangeSupport.
 * 
 * @author Peter Fichtner
 */
public class ProxyInvocationHandlerPropertyChangeSupport extends ProxyInvocationHandler {

    private static final long serialVersionUID = 0L;

    // TODO switch Soft/Hard
    private BeanListenerSupport<PropertyChangeListener> propertyChangeListeners = new BeanListenerSupportSoftRef<PropertyChangeListener>();
    private BeanListenerSupport<VetoableChangeListener> vetoableChangeListeners = new BeanListenerSupportSoftRef<VetoableChangeListener>();

    public ProxyInvocationHandlerPropertyChangeSupport(final Class<?> proxiedIface, final Collection<Class<?>> ifaces) {
        super(proxiedIface, ifaces);
        // TODO Check all methods to be valid in dependency of their annotations (e.g. returntypes
        // void, parameters in dependency of type (veto/non-veto)
        // PropertyChangeListener/VetoableChangeListener)
    }

    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
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

    private void removePropertyChangeListener(final PropertyChangeListener listener) {
        this.propertyChangeListeners.remove(listener);
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        this.propertyChangeListeners.add(listener);
    }

    public void addVetoableChangeListener(final VetoableChangeListener listener) {
        this.vetoableChangeListeners.add(listener);
    }

    public void removeVetoableChangeListener(final VetoableChangeListener listener) {
        this.vetoableChangeListeners.remove(listener);
    }

    protected Object handleSetter(final Object proxy, final PropertyDescriptor descriptor, final Object[] args) {
        final PropertyChangeEvent event = new PropertyChangeEvent(proxy, descriptor.getName(), handleGetter(
                proxy, descriptor), args[0]);
        final boolean announce = !descriptor.getWriteMethod().isAnnotationPresent(Unbound.class);
        if (announce && checkForVeto(event)
                && !descriptor.getWriteMethod().isAnnotationPresent(IgnoreVeto.class)) {
            return null;
        }
        final Object result = super.handleSetter(proxy, descriptor, args);
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
    private boolean checkForVeto(final PropertyChangeEvent event) {
        try {
            for (final VetoableChangeListener listener : this.vetoableChangeListeners) {
                listener.vetoableChange(event);
            }
            return false;
        } catch (final PropertyVetoException e) {
            return true;
        }
    }

    /**
     * Inform the PropertyChangeListeners.
     * 
     * @param event the event to pass to
     *            {@link PropertyChangeListener#propertyChange(PropertyChangeEvent)}
     */
    private void informListeners(final PropertyChangeEvent evt) {
        for (final PropertyChangeListener listener : this.propertyChangeListeners) {
            listener.propertyChange(evt);
        }
    }

    protected ProxyInvocationHandlerPropertyChangeSupport clone() throws CloneNotSupportedException {
        final ProxyInvocationHandlerPropertyChangeSupport result = (ProxyInvocationHandlerPropertyChangeSupport) super
                .clone();
        result.propertyChangeListeners = this.propertyChangeListeners.clone();
        result.vetoableChangeListeners = this.vetoableChangeListeners.clone();
        return result;
    }
}