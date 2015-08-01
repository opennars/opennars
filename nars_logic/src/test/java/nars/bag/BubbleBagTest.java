package nars.bag;


import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import nars.bag.impl.experimental.BubbleBag;
import nars.budget.Item;
import nars.util.data.random.XORShiftRandom;
import org.junit.Test;

import java.util.Random;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import nars.analyze.experimental.NullItem;
import static org.junit.Assert.assertTrue;

public class BubbleBagTest extends AbstractBagTest {

    final static Random rng = new XORShiftRandom();

    @Test
    public void testSolidBagSetOperations() {


        BubbleBag<NullItem,CharSequence> s = new BubbleBag(rng, 4);

        s.put(new NullItem(0.5f, "5"));
        s.put(new NullItem(0.3f, "3"));
        s.put(new NullItem(0.2f, "2"));

        assertEquals(3, s.inPriority. getN(), 0.001f);
        assertEquals(0.333f, s.inPriority.getMean(), 0.001f);

        assertEquals(3, s.size());

        assertTrue(s.contains("3"));


        NullItem three = s.remove("3");
        assertNotNull(three);
        assertEquals(2, s.size());
        assertEquals(0.3f, s.outPriority.getMean(), 0.001f);
        assertEquals(0.35f, s.getPriorityMean(), 0.001f);
        assertEquals(0.7f, s.getPrioritySum(), 0.001f);

        assertEquals("removed middle element does not appear in iterator", 2, Iterators.size(s.iterator()));

        assertEquals(1, s.iterator().next().name().length());

        assertEquals(2, s.size());

        s.put(new NullItem(0.8f, "8"));
        assertEquals(3, s.size());

        s.put(new NullItem(0.4f, "4"));
        assertEquals("max capacity reached", 4, s.size());


        NullItem overflow = s.put(new NullItem(0.1f, "1"));
        assertEquals("max capacity; overflow", 4, s.size());
        assertNotNull(overflow);

        overflow = s.put(new NullItem(0.0f, "0"));
        assertEquals("max capacity; exceeded ", 4, s.size());
        assertNotNull(overflow);

        NullItem r = s.pop();
        assertEquals("removed", 3, s.size());
        assertTrue(!s.contains(r));
        assertEquals(3, Iterators.size(s.iterator()));

    }

    @Test public void testActivity() {

        int c = 32;
        BubbleBag<NullItem,CharSequence> s = new BubbleBag(rng, c);

        int ii = c*3;
        for (int i = 0; i < ii; i++) {
            //System.out.println(s.size() + " " + s.getCapacity());
            s.put(new NullItem(rng.nextFloat() * 0.95f));
        }

        assertEquals(c, s.size());



        NullItem prev = null;
        boolean shifted = false;
        for (int i = 0; i < c*2; i++) {
            //System.out.println(s);
            NullItem p = s.peekNext();
            //System.out.println(" " + p);

            if (prev!=null)
                if (prev!=p)
                    shifted = true;
            prev = p;
        }
        assertTrue("peek()'d item has changed at least once; final state=" + Lists.newArrayList(s), shifted);

    }


    @Test public void testPrioritization() {
        int loops = 100;
        int insertsPerLoop = 1;
        float fractionToAdjust = 0.1f;
        float fractionTRemove = 0.1f;
        BubbleBag bag = new BubbleBag(rng, 16);

        int[] dist =AbstractBagTest.testRemovalPriorityDistribution(
                loops, insertsPerLoop, fractionToAdjust, fractionTRemove, bag,
                false
        );

        //System.out.println(Arrays.toString(dist));
        assertTrue(dist[0] + dist[dist.length - 1] > 0);
    }

    @Test public void testPrioritization2() {
        int loops = 5000;
        int insertsPerLoop = 1;

        BubbleBag bag = new BubbleBag(rng, 500) {

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

        /*
        System.out.println(bag.inPriority);
        System.out.println(bag.outPriority);
        System.out.println(bag.size());
        */

        //double[] dist = (bag.getPriorityDistribution(10));
        //Frequency f = bag.removal;
        //System.out.println(f);

    }

}
