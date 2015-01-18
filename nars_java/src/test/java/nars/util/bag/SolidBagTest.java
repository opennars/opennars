package nars.util.bag;


import nars.util.bag.experimental.SolidBag;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static nars.perf.BagPerf.NullItem;
import static org.junit.Assert.assertTrue;

public class SolidBagTest {

    @Test
    public void testSolidBagSetOperations() {


        SolidBag<NullItem,CharSequence> s = new SolidBag(4);

        s.putIn(new NullItem(0.5f, "5"));
        s.putIn(new NullItem(0.3f, "3"));
        s.putIn(new NullItem(0.2f, "2"));

        assertEquals(3, s.size());

        assertTrue(s.contains("3"));

        NullItem three = s.take("3");
        assertNotNull(three);

        assertEquals(2, s.size());

        assertEquals(1, s.iterator().next().name().length());

        assertEquals(2, s.size());

        s.putIn(new NullItem(0.8f, "8"));
        assertEquals(3, s.size());

        s.putIn(new NullItem(0.4f, "4"));
        assertEquals("max capacity reached", 4, s.size());

        s.print(System.out);

        NullItem overflow = s.putIn(new NullItem(0.1f, "1"));
        assertEquals("max capacity not exceeded but overflow", 4, s.size());
        assertNotNull(overflow);

        overflow = s.putIn(new NullItem(0.0f, "0"));
        assertEquals("max capacity not exceeded ", 4, s.size());
        assertNotNull(overflow);




    }

}
