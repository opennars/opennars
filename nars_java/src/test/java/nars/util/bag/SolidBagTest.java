package nars.util.bag;


import com.google.common.collect.Iterators;
import nars.core.Memory;
import nars.perf.BagPerf;
import nars.util.bag.experimental.SolidBag;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static nars.perf.BagPerf.NullItem;
import static nars.perf.BagPerf.itemID;
import static org.junit.Assert.assertTrue;

public class SolidBagTest extends AbstractBagTest {

    @Test
    public void testSolidBagSetOperations() {


        SolidBag<NullItem,CharSequence> s = new SolidBag(4);

        s.putIn(new NullItem(0.5f, "5"));
        s.putIn(new NullItem(0.3f, "3"));
        s.putIn(new NullItem(0.2f, "2"));

        assertEquals(0.333f, s.inPriority.getMean(), 0.001f);

        assertEquals(3, s.size());

        assertTrue(s.contains("3"));


        NullItem three = s.take("3");
        assertNotNull(three);
        assertEquals(2, s.size());
        assertEquals(0.3f, s.outPriority.getMean(), 0.001f);
        assertEquals(0.35f, s.getAveragePriority(), 0.001f);
        assertEquals(0.7f, s.getMass(), 0.001f);

        assertEquals("removed middle element does not appear in iterator", 2, Iterators.size(s.iterator()));

        assertEquals(1, s.iterator().next().name().length());

        assertEquals(2, s.size());

        s.putIn(new NullItem(0.8f, "8"));
        assertEquals(3, s.size());

        s.putIn(new NullItem(0.4f, "4"));
        assertEquals("max capacity reached", 4, s.size());


        NullItem overflow = s.putIn(new NullItem(0.1f, "1"));
        assertEquals("max capacity; overflow", 4, s.size());
        assertNotNull(overflow);

        overflow = s.putIn(new NullItem(0.0f, "0"));
        assertEquals("max capacity; exceeded ", 4, s.size());
        assertNotNull(overflow);

        NullItem r = s.takeNext();
        assertEquals("removed", 3, s.size());
        assertTrue(!s.contains(r));
        assertEquals(3, Iterators.size(s.iterator()));

    }

    @Test public void testActivity() {
        Memory.resetStatic(1);

        int c = 4;
        SolidBag<NullItem,CharSequence> s = new SolidBag(c);

        int ii = 15;
        for (int i = 0; i < ii; i++) {
            //System.out.println(s.size() + " " + s.getCapacity());
            s.putIn(new NullItem(Memory.randomNumber.nextFloat() * 0.95f));
        }

        assertEquals(c, s.size());



        NullItem prev = null;
        boolean shifted = false;
        for (int i = 0; i < 16; i++) {
            //System.out.println(s);
            NullItem p = s.peekNext();
            //System.out.println(" " + p);

            if (prev!=null)
                if (prev!=p)
                    shifted = true;
            prev = p;
        }
        assertTrue("peek()'d item has changed at least once", shifted);

    }


    @Test public void testPrioritization() {
        int loops = 100;
        int insertsPerLoop = 1;
        float fractionToAdjust = 0.1f;
        float fractionTRemove = 0.1f;
        SolidBag bag = new SolidBag(16);

        int[] dist =AbstractBagTest.testRemovalPriorityDistribution(
                loops, insertsPerLoop, fractionToAdjust, fractionTRemove, bag,
                false
        );

        assertTrue(dist[0] + dist[dist.length-1] > 0);
    }

    @Test public void testPrioritization2() {
        int loops = 100;
        int insertsPerLoop = 2;
        float fractionToAdjust = 0.2f;
        float fractionTRemove = 0.05f;
        SolidBag bag = new SolidBag(32);

        int[] dist =AbstractBagTest.testRemovalPriorityDistribution(
                loops, insertsPerLoop, fractionToAdjust, fractionTRemove, bag,
                false
        );

        System.out.println(Arrays.toString(dist));
    }

}
