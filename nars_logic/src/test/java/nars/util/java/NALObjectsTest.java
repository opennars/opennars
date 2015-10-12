package nars.util.java;

import com.google.common.collect.Lists;
import nars.NAR;
import nars.meter.EventCount;
import nars.nar.Default;
import nars.term.Term;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import static org.junit.Assert.*;


public class NALObjectsTest  {

    public static class TestClass {

        public int count = 0;
        public int val = -1;


        public double the() {
            double v = (64 * Math.random());
            this.val = (int)v;
            return v;
        }

        public void noParamMethodReturningVoid() {
            count++;
            //System.out.println("base call");
            //return Math.random();
        }

        public float multiply(float a, float b) {
            return a * b;
        }
    }

    @Test
    public void testDynamicProxyObjects() throws Exception {


        NAR n = new Default();

        EventCount count = new EventCount(n);

        TestClass tc = new NALObjects(n).build("myJavaObject", TestClass.class);

        tc.noParamMethodReturningVoid();
        assertEquals(6.0, tc.multiply(2, 3), 0.001);
        assertNotNull( tc.the() );


        n.frame(4);


        assertNotEquals(tc.getClass(), TestClass.class);
        assertTrue(1 <= count.numInputs());


    }

    /** test that the methods of invoking an instance method are indistinguishable
     * whether it occurred from outside, or from a NAR goal
     */
    @Test public void testMethodOperators() throws Exception {

        NAR n = new Default();

        String instance = "obj";
        NALObjects no = new NALObjects(n);
        TestClass nc = no.build(instance, TestClass.class);


        StringWriter ns;
        n.trace(new PrintWriter(ns = new StringWriter()));

        //n.stdout();

        nc.multiply(2,3);

        n.frame(16);

//        assertNotNull( n.memory.concept(
//                no.termClassInPackage(TestClass.class))
//        );


        n.input("TestClass_multiply(" + instance + ", 2, 3, #x)!");




        //System.out.println(ns.getBuffer().toString());
        //System.out.println();
        //System.out.println(ms.getBuffer().toString());

        String expect = "<{6} --> (/, ^TestClass_multiply, obj, 2, 3, _)>.";
        String bs = ns.getBuffer().toString();
        assertTrue(bs.contains(expect));

    }

    @Test public void testTermizerPrimitives() {

        testTermizer(null);

        testTermizer(0);
        testTermizer(3.14159);

        testTermizer('a');

        testTermizer("a b c"); //should result in quoted
    }

    @Test public void testTermizerBoxed() {
        testTermizer(new Integer(1));
        testTermizer(new Float(3.14159));
    }
    @Test public void testTermizerCollections() {
        testTermizer(Lists.newArrayList("x", "y"));
    }
    @Test public void testTermizerArray() {
        testTermizer(new String[] { "x", "y" } );
    }

    private void testTermizer(Object o) {
        DefaultTermizer t = new DefaultTermizer();
        Term term = t.term(o);
        assertNotNull(term);
        Object p = t.object(term);

        //System.out.println(t.objects);

        if (o!=null)
            assertEquals(p, o);
        else
            assertNull(p==null ? "('null' value)" : p.getClass().toString(),
                       p);


    }

    @Test public void testWrappedArrayList() throws Exception {
        NAR n = new Default();

        NALObjects no = new NALObjects(n);
        ArrayList nc = no.build("LIST", ArrayList.class);

        n.stdout();
        nc.add("item");
        //nc.add("x");

        n.frame(2);


        nc.clear();

        n.frame(2);


/*        nc.add(1);
        nc.get(0);
        nc.get(1);
        nc.size();
        nc.clear();*/

    }


}