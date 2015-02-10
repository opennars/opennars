package nars.util.bag;


import com.google.common.collect.Iterators;
import nars.core.Memory;
import nars.logic.entity.Item;
import nars.util.bag.impl.experimental.BubbleBag;
import org.apache.commons.math3.stat.Frequency;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static nars.analyze.experimental.BagPerf.NullItem;
import static org.junit.Assert.assertTrue;

public class SolidBagTest extends AbstractBagTest {

    @Test
    public void testSolidBagSetOperations() {


        BubbleBag<NullItem,CharSequence> s = new BubbleBag(4);

        s.PUT(new NullItem(0.5f, "5"));
        s.PUT(new NullItem(0.3f, "3"));
        s.PUT(new NullItem(0.2f, "2"));

        assertEquals(3, s.inPriority. getN(), 0.001f);
        assertEquals(0.333f, s.inPriority.getMean(), 0.001f);

        assertEquals(3, s.size());

        assertTrue(s.contains("3"));


        NullItem three = s.TAKE("3");
        assertNotNull(three);
        assertEquals(2, s.size());
        assertEquals(0.3f, s.outPriority.getMean(), 0.001f);
        assertEquals(0.35f, s.getAveragePriority(), 0.001f);
        assertEquals(0.7f, s.getMass(), 0.001f);

        assertEquals("removed middle element does not appear in iterator", 2, Iterators.size(s.iterator()));

        assertEquals(1, s.iterator().next().name().length());

        assertEquals(2, s.size());

        s.PUT(new NullItem(0.8f, "8"));
        assertEquals(3, s.size());

        s.PUT(new NullItem(0.4f, "4"));
        assertEquals("max capacity reached", 4, s.size());


        NullItem overflow = s.PUT(new NullItem(0.1f, "1"));
        assertEquals("max capacity; overflow", 4, s.size());
        assertNotNull(overflow);

        overflow = s.PUT(new NullItem(0.0f, "0"));
        assertEquals("max capacity; exceeded ", 4, s.size());
        assertNotNull(overflow);

        NullItem r = s.TAKENEXT();
        assertEquals("removed", 3, s.size());
        assertTrue(!s.contains(r));
        assertEquals(3, Iterators.size(s.iterator()));

    }

    @Test public void testActivity() {
        Memory.resetStatic(1);

        int c = 4;
        BubbleBag<NullItem,CharSequence> s = new BubbleBag(c);

        int ii = 15;
        for (int i = 0; i < ii; i++) {
            //System.out.println(s.size() + " " + s.getCapacity());
            s.PUT(new NullItem(Memory.randomNumber.nextFloat() * 0.95f));
        }

        assertEquals(c, s.size());



        NullItem prev = null;
        boolean shifted = false;
        for (int i = 0; i < 16; i++) {
            //System.out.println(s);
            NullItem p = s.PEEKNEXT();
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
        BubbleBag bag = new BubbleBag(16);

        int[] dist =AbstractBagTest.testRemovalPriorityDistribution(
                loops, insertsPerLoop, fractionToAdjust, fractionTRemove, bag,
                false
        );

        assertTrue(dist[0] + dist[dist.length - 1] > 0);
    }

    @Test public void testPrioritization2() {
        int loops = 5000;
        int insertsPerLoop = 1;

        BubbleBag bag = new BubbleBag(500) {

            @Override
            public Item onExit(Item item) {
                Item e = super.onExit(item);
                if (e!=null) {

                }
                return e;
            }
        };

        AbstractBagTest.testRetaining(
                loops, insertsPerLoop, bag
        );

        System.out.println(bag.inPriority);
        System.out.println(bag.outPriority);
        System.out.println(bag.size());

        //double[] dist = (bag.getPriorityDistribution(10));
        Frequency f = bag.removal;
        System.out.println(f);

    }

}
