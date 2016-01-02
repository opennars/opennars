package objenome.util.bean;

import objenome.util.bean.anno.IgnoreVeto;
import objenome.util.bean.anno.Unbound;
import objenome.util.bean.util.DefaultVetoablePropertyChangeEventProvider;
import org.junit.Test;

import java.beans.PropertyVetoException;

import static org.junit.Assert.assertEquals;

public class DoNotAnnounceTest {

    public interface TestBean extends DefaultVetoablePropertyChangeEventProvider {

        int getFoo();

        void setFoo(int foo);

        int getBar();

        @Unbound
        void setBar(int bar);

        int getFooBar();

        void setFooBar(int foo);
        
        float getValue();
     
        @IgnoreVeto
        void setValue(float val);
    }

    @Test
    public void testDoNotAnnounce() {
        TestBean bean = BeanProxyBuilder.on(TestBean.class).build();
        bean.addVetoableChangeListener(evt -> {
            throw new PropertyVetoException("No changes allowed!", evt); //$NON-NLS-1$
        });
        bean.setFoo(4);
        bean.setBar(5);
        bean.setFooBar(6);
        bean.setValue(44);
        assertEquals(Integer.valueOf(0), Integer.valueOf(bean.getFoo()));
        // 5 was set because the property change was not announced, so no veto
        assertEquals(Integer.valueOf(5), Integer.valueOf(bean.getBar()));
        assertEquals(Integer.valueOf(0), Integer.valueOf(bean.getFooBar()));
        // 44 was set because the changes of the property are not vetoable 
        assertEquals(Float.valueOf(44), Float.valueOf(bean.getValue()));
    }
}
