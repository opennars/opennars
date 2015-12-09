package objenome.util.bean;

import objenome.util.bean.util.DefaultPropertyChangeEventProvider;
import objenome.util.bean.util.DefaultVetoablePropertyChangeEventProvider;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import static org.junit.Assert.assertEquals;

public class TestPropertyChange {

    public interface TestBean extends Cloneable, DefaultPropertyChangeEventProvider, DefaultVetoablePropertyChangeEventProvider {

        String getA();

        void setA(String a);

        int getB();

        void setB(int b);

        TestBean clone();
    }

    @Test
    public void testPropertyChange() {
        TestBean toClone =  BeanProxyBuilder.on(TestBean.class).build();
        PropertyChangeListenerMock listener = new PropertyChangeListenerMock();
        toClone.addPropertyChangeListener(listener);
        TestBean testBean = toClone.clone();
        toClone.removePropertyChangeListener(listener);
        testBean.setA("a"); //$NON-NLS-1$
        testBean.setB(88);

        assertEquals(2, listener.getEvents().size());

        PropertyChangeEvent evt0 = listener.getEvents().get(0);
        assertEquals(null, evt0.getOldValue());
        assertEquals("a", evt0.getNewValue()); //$NON-NLS-1$

        PropertyChangeEvent evt1 = listener.getEvents().get(1);
        assertEquals(0, evt1.getOldValue());
        assertEquals(88, evt1.getNewValue());
    }

    @Test
    public void testVetoPropertyChange() {
        TestBean toClone = BeanProxyBuilder.on(TestBean.class).build();
        VetoableChangeListener listener = evt -> {
            if ("a".equals(evt.getPropertyName()) && "y".equals(evt.getOldValue()) && "z".equals(evt.getNewValue())) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                throw new PropertyVetoException("message", evt); //$NON-NLS-1$
            }

        };
        toClone.addVetoableChangeListener(listener);
        TestBean testBean = toClone.clone();
        toClone.removeVetoableChangeListener(listener);
        testBean.setA("x"); //$NON-NLS-1$
        testBean.setA("y"); //$NON-NLS-1$
        testBean.setA("z"); //$NON-NLS-1$

        assertEquals("y", testBean.getA()); //$NON-NLS-1$
    }

}
