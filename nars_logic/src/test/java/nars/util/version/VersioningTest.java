package nars.util.version;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by me on 12/3/15.
 */
public class VersioningTest {

    @Test
    public void test1() {
        Versioning v = new Versioning();
        VersionMap m = new VersionMap(v);
        m.put("x", "a");
        assertEquals("{x=a}", m.toString());
        assertEquals(1, v.now());
        m.put("x", "b");

        assertEquals("{x=b}", m.toString());

        Versioned mvx = m.version("x");

        assertEquals("(1:a, 2:b)", mvx.toStackString());
        assertEquals(2, v.now());
        assertEquals(2, mvx.lastUpdatedAt());
        assertEquals(2, mvx.size());

        v.revert(2); //should have no effect:
        assertEquals(2, v.now());
        assertEquals(2, mvx.lastUpdatedAt());
        assertEquals("(1:a, 2:b)", mvx.toStackString());
        assertEquals(2, mvx.size());
        assertEquals(2, v.size());

        v.revert(1);
        assertEquals(1, v.now());
        assertEquals(1, mvx.lastUpdatedAt());
        assertEquals("{x=a}", m.toString());
        assertEquals("(1:a)", mvx.toStackString());
        assertEquals(1, mvx.size());
        assertEquals(1, v.size());

        v.revert(0);
        assertEquals(0, v.size());
        assertEquals(0, v.now());
        assertEquals(0, mvx.size());
        assertNull(m.get("x")); //removed from map because it did not exist at version 0 which is effectively empty

        assertEquals(-1, mvx.lastUpdatedAt());
        assertEquals("{}", m.toString());
        assertEquals(true, m.isEmpty());

    }

}