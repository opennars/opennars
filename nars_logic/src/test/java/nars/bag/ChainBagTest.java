package nars.bag;

import nars.bag.impl.experimental.ChainBag;
import nars.budget.Item;
import nars.util.data.random.XORShiftRandom;
import nars.util.meter.bag.NullItem;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by me on 3/9/15.
 */
public class ChainBagTest {

    @Test
    public void testChainBagSequence() {

        testChainBagSequence(8, 8*8); //no restriction
        testChainBagSequence(8, 8); //restricted
    }
    public void testChainBagSequence(int loops, int capacity) {

        XORShiftRandom rng = new XORShiftRandom();

        ChainBag bag = new ChainBag(rng, capacity);


        float fractionToAdjust = 0.1f;
        float fractionTRemove = 0.1f;

        int inputs = loops * loops;
        for (int i = 0 ;i < inputs; i++) {
            bag.put(new NullItem(rng.nextFloat()));
            assertTrue(capacity >= bag.size());
        }

        assertEquals(capacity, bag.size());

        for (int j = 0; j < loops-1; j++) {
            for (int i = 0; i < loops; i++) {
                Item x = bag.peekNext();
                assertNotNull(x);
                //System.out.println(x);
            }

            Item r = bag.pop();
            //System.out.println("pop: " + r);
            assertNotNull(r);
            assertEquals((capacity)- (1 + j), bag.size());
        }


    }
}
