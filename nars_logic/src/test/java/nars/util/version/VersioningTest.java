package nars.util.version;

import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by me on 12/3/15.
 */
public class VersioningTest {

    Versioning v = new Versioning();
    Versioned a = new Versioned(v);
    Versioned b = new Versioned(v);

    @Test
    public void test1() {
        Versioning w = new Versioning();
        VersionMap m = new VersionMap(w);
        m.put("x", "a");
        assertEquals("{x=a}", m.toString());
        assertEquals(1, w.now());
        m.put("x", "b");

        assertEquals("{x=b}", m.toString());

        Versioned mvx = m.version("x");

        assertEquals("(1:a, 2:b)", mvx.toStackString());
        assertEquals(2, w.now());
        assertEquals(2, mvx.lastUpdatedAt());
        assertEquals(2, mvx.size());

        w.revert(2); //should have no effect:
        assertEquals(2, w.now());
        assertEquals(2, mvx.lastUpdatedAt());
        assertEquals("(1:a, 2:b)", mvx.toStackString());
        assertEquals(2, mvx.size());
        assertEquals(2, w.size());

        w.revert(1);
        assertEquals(1, w.now());
        assertEquals(1, mvx.lastUpdatedAt());
        assertEquals("{x=a}", m.toString());
        assertEquals("(1:a)", mvx.toStackString());
        assertEquals(1, mvx.size());
        assertEquals(1, w.size());

        w.revert(0);
        assertEquals(0, w.size());
        assertEquals(0, w.now());
        assertEquals(0, mvx.size());
        assertEquals(-1, mvx.lastUpdatedAt());
        assertEquals("{}", m.toString());

        assertEquals(true, m.isEmpty());
        assertNull(m.get("x")); //removed from map because it did not exist at version 0 which is effectively empty


    }

    public void initTestSequence1(boolean print) {

        if (print) System.out.println(v);      a.set("a0");
        if (print) System.out.println(v);      a.set("a1");
        if (print) System.out.println(v);      b.set("b0");
        if (print) System.out.println(v);      a.set("a2");
        if (print) System.out.println(v);      a.set("a3");
        if (print) System.out.println(v);      b.set("b1");

    }

    @Test
    public void test2() {
        initTestSequence1();

        Supplier<String> s = () -> a + " " + b;

        System.out.println(v);
        assertEquals(6, v.size()); assertEquals("a3 b1", s.get());

        v.revert(); System.out.println(v);
        assertEquals(5, v.size()); assertEquals("a3 b0", s.get());

        v.revert(); System.out.println(v);
        assertEquals(4, v.size()); assertEquals("a2 b0", s.get());

        v.revert(); System.out.println(v);
        assertEquals(3, v.size()); assertEquals("a1 b0", s.get());

        v.revert(); System.out.println(v);
        assertEquals(2, v.size());  assertEquals("a1 null", s.get());

        v.revert(); System.out.println(v);
        assertEquals(1, v.size());  assertEquals("a0 null", s.get());

        v.revert(); System.out.println(v);
        assertEquals(0, v.size()); assertEquals("null null", s.get());

    }

    public void initTestSequence1() {
        initTestSequence1(false);
    }


    @Test
    public void testRewind() {

        initTestSequence1();

        Supplier<String> s = () -> a + " " + b;

        System.out.println(v);
        assertEquals(6, v.size()); assertEquals("a3 b1", s.get());
        assertEquals(6, v.now());

        System.out.println("revert to 3");

        //skip behind to halfway
        v.revert(3); System.out.println(v);
        assertEquals(3, v.now());
        assertEquals(3, v.size()); assertEquals("a1 b0", s.get());

        v.revert();  System.out.println(v);
        assertEquals(2, v.now());
        assertEquals(2, v.size()); assertEquals("a1 null", s.get());

    }

}