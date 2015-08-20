package nars.util.java;

import junit.framework.TestCase;
import nars.NAR;
import nars.io.out.TextOutput;
import nars.meter.CountIOEvents;
import nars.nar.Default;
import nars.term.Atom;
import org.junit.Test;

import java.io.StringWriter;
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

    /** test that the methods of invoking an instance method are indistinguishable
     * whether it occurred from outside, or from a NAR goal
     */
    @Test public void testMethodOperators() throws Exception {

        NAR n = new NAR(new Default());
        NAR m = new NAR(new Default());

        String instance = "obj";
        TestClass nc = new NALObjects(n).build(instance, TestClass.class);
        TestClass mc = new NALObjects(m).build(instance, TestClass.class);

        assertNotNull( n.memory.exe.all(Atom.the("TestClass_multiply")) );

        StringWriter ns, ms;
        new TextOutput(n, ns = new StringWriter());
        new TextOutput(m, ms = new StringWriter());

        n.input("TestClass_multiply(" + instance + ", 2, 3, #x)!");

        n.frame(16);


        mc.multiply(2,3);

        m.frame(32);

        System.out.println(ns.getBuffer().toString());
        System.out.println();
        System.out.println(ms.getBuffer().toString());

        String expect = "IN: <\"6.0\" --> (/, ^TestClass_multiply, obj, \"2.0\", \"3.0\", _)>. :|: %1.00;";
        assertTrue(ns.getBuffer().toString().contains(expect));
        assertTrue(ms.getBuffer().toString().contains(expect));
    }

}