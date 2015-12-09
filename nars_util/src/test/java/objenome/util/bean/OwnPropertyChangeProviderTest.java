package objenome.util.bean;


import objenome.util.bean.anno.PropertyChangeEventMethod;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import static objenome.util.bean.anno.PropertyChangeEventMethod.Type.ADD_LISTENER;
import static objenome.util.bean.anno.PropertyChangeEventMethod.Type.REMOVE_LISTENER;
import static org.junit.Assert.assertEquals;

public class OwnPropertyChangeProviderTest {

    public interface ForeignChangeProvider {

        @PropertyChangeEventMethod(ADD_LISTENER)
        void foo(PropertyChangeListener listener);

        @PropertyChangeEventMethod(REMOVE_LISTENER)
        void bar(PropertyChangeListener listener);
    }

    public interface TestBean extends ForeignChangeProvider {

        float getMe();

        void setMe(float me);
    }

    @Test
    public void testPropertyChange() {
        TestBean bean = BeanProxyBuilder.on(TestBean.class).build();
        PropertyChangeListenerMock listenerMock = new PropertyChangeListenerMock();
        // register Listener
        bean.foo(listenerMock);
        // set values
        bean.setMe(5);
        bean.setMe(12);

        // deregister Listener
        bean.bar(listenerMock);

        // do some more calls
        bean.setMe(-4);
        bean.setMe(999);
        bean.setMe(-75);

        List<PropertyChangeEvent> events = listenerMock.getEvents();
        // check the event count
        assertEquals(Integer.valueOf(2), Integer.valueOf(events.size()));

        PropertyChangeEvent evt0 = events.get(0);
        assertEquals("me", evt0.getPropertyName()); //$NON-NLS-1$
        assertEquals(0f, evt0.getOldValue());
        assertEquals(5f, evt0.getNewValue());

        PropertyChangeEvent evt1 = events.get(1);
        assertEquals("me", evt1.getPropertyName()); //$NON-NLS-1$
        assertEquals(5f, evt1.getOldValue());
        assertEquals(12f, evt1.getNewValue());
    }

}
