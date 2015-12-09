package objenome.util.bean;

import org.junit.Test;

import java.io.Serializable;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class CloneTest {

    public interface A {

        void setA(String a);

        String getA();
    }

    public interface B extends B1 {

        void setB(int b);

        int getB();
    }

    public interface B1 extends B2 {

        void setB1(Object b);

        Object getB1();
    }

    public interface B2 {

        void setMap(Map<Float, Boolean> map);

        Map<Float, Boolean> getMap();
    }

    public interface C extends A, B, List<String>, Serializable, Cloneable {

        C clone();
    }

    @Test
    public void testClone() {
        // do not check since "boolean List#isEmpty" has no corresponding getter (setEmpty(boolean))
        C orig = BeanProxyBuilder.on(C.class).check(false).build();
        orig.setA("a"); //$NON-NLS-1$
        orig.setB(7);
        Map<Float, Boolean> map = Collections.singletonMap(4f, Boolean.TRUE);
        orig.setMap(map);
        C clone = orig.clone();

        // check implemented interfaces
        // 10: A, B1, B2, B, C, List, Collection, Iterable, Cloneable, Serializable
        assertEquals(Integer.valueOf(10), Integer.valueOf(clone.getClass().getInterfaces().length));
        assertEquals(new HashSet<>(Arrays.asList(orig.getClass().getInterfaces())),
                new HashSet<>(Arrays.asList(clone.getClass().getInterfaces())));

        // check equals
        assertNotSame(orig, clone);
        assertEquals(orig, clone);

        // set of an value in the "orig" bean must NOT affect the values of "clone"
        orig.setA("--------------------"); //$NON-NLS-1$

        // check content
        assertEquals("a", clone.getA()); //$NON-NLS-1$
        assertEquals(Integer.valueOf(7), Integer.valueOf(clone.getB()));
        assertEquals(map, clone.getMap());
    }

}
