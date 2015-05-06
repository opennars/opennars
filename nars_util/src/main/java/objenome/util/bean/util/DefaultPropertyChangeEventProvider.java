package objenome.util.bean.util;

import static objenome.util.bean.anno.PropertyChangeEventMethod.Type.ADD_LISTENER;
import static objenome.util.bean.anno.PropertyChangeEventMethod.Type.REMOVE_LISTENER;

import java.beans.PropertyChangeListener;

import objenome.util.bean.anno.PropertyChangeEventMethod;

/**
 * Beans (interfaces) implementing this interface support PropertyChangeEvents (each setter call
 * will trigger a PropertyChangeEvent). Of course you can combine PropertyChangeEventProvider and
 * VetoablePropertyChangeEventProvider by extending both interfaces.
 * 
 * <pre>
 * Example:
 * 
 * public interface MyBean extends PropertyChangeEventProvider {
 *     
 *     void setFoo(String foo);
 *     String getFoo();
 *     
 * }
 * 
 * [...]
 *      MyBean bean = ProxyFactory.getProxyInstance(MyBean.class);
 *      PropertyChangeListener listener = ....;
 *      bean.addPropertyChangeListener(listener);
 *      bean.setFoo(&quot;newFoo&quot;) // &lt;-- will call propertyChange on listener
 * [...]
 * </pre>
 * 
 * @author Peter Fichtner
 * @see DefaultVetoablePropertyChangeEventProvider
 */
public interface DefaultPropertyChangeEventProvider {

    @PropertyChangeEventMethod(ADD_LISTENER)
    void addPropertyChangeListener(PropertyChangeListener listener);

    @PropertyChangeEventMethod(REMOVE_LISTENER)
    void removePropertyChangeListener(PropertyChangeListener listener);

}
