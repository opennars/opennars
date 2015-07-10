package nars.util.bind;

import junit.framework.TestCase;
import nars.Events;
import nars.NAR;
import nars.io.out.TextOutput;
import nars.meter.CountIOEvents;
import nars.nar.Default;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertNotEquals;


public class NALObjectsTest extends TestCase {

    public static class TestClass {

        public double the() {
            return Math.random();
        }

        public void noParamMethodReturningVoid() {
            //System.out.println("base call");
            //return Math.random();
        }

        public float multiply(float a, float b) {
            return a * b;
        }
    }

    @Test
    public void testDynamicProxyObjects() throws Exception {

        AtomicInteger statements = new AtomicInteger(0);

        NAR n = new NAR(new Default());

        CountIOEvents count = new CountIOEvents(n);

        TestClass tc = new NALObjects(n).build("myJavaObject", TestClass.class);

        tc.noParamMethodReturningVoid();
        assertEquals(6.0, tc.multiply(2, 3), 0.001);
        assertNotNull( tc.the() );


        n.frame(4);


        assertNotEquals(tc.getClass(), TestClass.class);
        assertEquals(3, count.numInputs());


    }

}