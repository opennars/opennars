package objenome.util.bean;



import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import objenome.util.bean.anno.PropertyChangeEventMethod;
import static objenome.util.bean.anno.PropertyChangeEventMethod.Type.ADD_LISTENER;
import static objenome.util.bean.anno.PropertyChangeEventMethod.Type.REMOVE_LISTENER;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class OwnPropertyChangeProviderTest {

    public static interface ForeignChangeProvider {

        @PropertyChangeEventMethod(ADD_LISTENER)
        void foo(PropertyChangeListener listener);

        @PropertyChangeEventMethod(REMOVE_LISTENER)
        void bar(PropertyChangeListener listener);
    }

    public static interface TestBean extends ForeignChangeProvider {

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
        assertEquals(Float.valueOf(0), evt0.getOldValue());
        assertEquals(Float.valueOf(5), evt0.getNewValue());

        PropertyChangeEvent evt1 = events.get(1);
        assertEquals("me", evt1.getPropertyName()); //$NON-NLS-1$
        assertEquals(Float.valueOf(5), evt1.getOldValue());
        assertEquals(Float.valueOf(12), evt1.getNewValue());
    }

}
