package nars.util.version;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 12/3/15.
 */
public class VersioningTest {

    @Test
    public void test1() {
        Versioning v = new Versioning();
        VersionMap m = new VersionMap(v);
        m.put("x", "a");
        assertEquals("", m.toString());
    }

}