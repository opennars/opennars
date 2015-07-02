package objenome.util.bean.util;

import objenome.util.bean.anno.PropertyChangeEventMethod;

import java.beans.VetoableChangeListener;

import static objenome.util.bean.anno.PropertyChangeEventMethod.Type.ADD_VETO_LISTENER;
import static objenome.util.bean.anno.PropertyChangeEventMethod.Type.REMOVE_VETO_LISTENER;

/**
 * Beans (interfaces) implementing this interface support VetoablePropertyChangeEvents. (each setter
 * call will trigger a vetoable propertyChangeEvent). Of course you can combine
 * VetoablePropertyChangeEventProvider and PropertyChangeEventProvider by extending both interfaces.
 * 
 * <pre>
 * Example:
 * 
 * public interface MyBean extends VetoablePropertyChangeEventProvider {
 *     
 *     void setFoo(String foo);
 *     String getFoo();
 *     
 * }
 * 
 * [...]
 *      MyBean bean = ProxyFactory.getProxyInstance(MyBean.class);
 *      VetoableChangeListener listener = ....;
 *      bean.addVetoableChangeListener(listener);
 *      bean.setFoo(&quot;newFoo&quot;) // &lt;-- will call vetoableChange on listener
 * [...]
 * </pre>
 * 
 * @author Peter Fichtner
 * @see DefaultPropertyChangeEventProvider
 */
public interface DefaultVetoablePropertyChangeEventProvider {

    @PropertyChangeEventMethod(ADD_VETO_LISTENER)
    void addVetoableChangeListener(VetoableChangeListener listener);

    @PropertyChangeEventMethod(REMOVE_VETO_LISTENER)
    void removeVetoableChangeListener(VetoableChangeListener listener);

}
