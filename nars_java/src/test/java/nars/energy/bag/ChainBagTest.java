package nars.energy.bag;

import nars.analyze.experimental.BagPerf;
import nars.Memory;
import nars.nal.entity.Item;
import nars.energy.bag.experimental.ChainBag;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Created by me on 3/9/15.
 */
public class ChainBagTest {

    @Test
    public void testChainBagSequence() {

        int loops = 8;

        ChainBag bag = new ChainBag(loops * loops);


        float fractionToAdjust = 0.1f;
        float fractionTRemove = 0.1f;

        for (int i = 0 ;i < loops; i++) {
            for (int j = 0; j < loops; j++) {
                bag.put(new BagPerf.NullItem(Memory.randomNumber.nextFloat()));
            }
        }

        assertEquals(loops * loops, bag.size());

        for (int j = 0; j < loops-1; j++) {
            for (int i = 0; i < loops; i++) {
                Item x = bag.peekNext();
                assertNotNull(x);
                //System.out.println(x);
            }

            Item r = bag.pop();
            //System.out.println("pop: " + r);
            assertNotNull(r);
            assertEquals(loops * loops - (1+j), bag.size());
        }


    }
}
