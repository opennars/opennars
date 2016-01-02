/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome;

import objenome.util.bean.util.DefaultPropertyChangeEventProvider;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;

/**
 * @see: http://beanproxy.sourceforge.net/
 */
public class ContainerBeanTest {

    public interface SomeInterface {
        int getIntValue();
        void setIntValue(int intValue);
        String getStringValue();
        void setStringValue(String stringValue);
    } 
    
    @Test public void testBean() {
        Container c = new Container();
        SomeInterface s = c.bean(SomeInterface.class);
        s.setStringValue("x");
        assertEquals("x", s.getStringValue());
    }
    
    
    /*If you need PropertyChangeSupport (or even VetoableChangeSupport) all you have to do is let your interfaces extend DefaultPropertyChangeEventProvider (and/or DefaultVetoablePropertyChangeEventProvider if you like to support vetos)

public interface NiceExample extends DefaultPropertyChangeEventProvider {*/

    public interface ObservableInterface extends SomeInterface, DefaultPropertyChangeEventProvider {
        
    }

    @Test public void testObservableBean() {
        Container c = new Container();
        
        AtomicBoolean success = new AtomicBoolean(false);
        
        ObservableInterface s = c.bean(ObservableInterface.class);
        s.addPropertyChangeListener(pce -> success.set(true));
        s.setStringValue("x");
        
        assertEquals("x", s.getStringValue());        
        assertEquals(true, success.get());
    }
}
