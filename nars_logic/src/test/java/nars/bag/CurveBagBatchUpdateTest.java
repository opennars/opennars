package nars.bag;

import com.google.common.collect.Lists;
import nars.Global;
import nars.bag.impl.CurveBag;
import nars.bag.impl.LevelBag;
import nars.bag.impl.experimental.ChainBag;
import nars.util.data.random.XorShift1024StarRandom;
import nars.util.meter.bag.BagGenerators;
import nars.util.meter.bag.NullItem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotEquals;

/**
 */
public class CurveBagBatchUpdateTest {

    final XorShift1024StarRandom rng = new XorShift1024StarRandom(1);

    @Test
    public void testDefaultBatchImpl() {

        testDefaultBatchImpl(
                new CurveBag<>(32, rng)
        );
    }
    @Test
    public void testDefaultBatchImpl2() {
        testDefaultBatchImpl(
                new ChainBag<>(rng, 32)
        );
    }
    @Test
    public void testDefaultBatchImpl3() {
        testDefaultBatchImpl(
                new LevelBag<>(8, 32)
        );


    }

    public void testDefaultBatchImpl(Bag<CharSequence,NullItem> cb) {


        Global.DEBUG = true;

        int cap = cb.capacity();
        int batch = 2;
        NullItem[] b = new NullItem[batch];
        int loops = 256;
        int insertsPerLoop = cap/2;


        int[] x = BagGenerators.testRemovalPriorityDistribution(
                loops, insertsPerLoop, 1.0f, cb
        );
        System.out.println(Arrays.toString(x));

        assertTrue(  Math.abs(cap - cb.size()) < cap/8.0 );

        cb.clear();
        x = BagGenerators.testRemovalPriorityDistribution(
                2, cap, 0.5f , cb
        );
        //cb.printAll();

        int count1 = cb.peekNext(BagSelector.anyItemSelector, b, 3);

        assertEquals(b.length, count1);

        ArrayList<NullItem> b1 = snapshot(b);


        int count2 = cb.peekNext(BagSelector.anyItemSelector, b, 3);
        assertEquals(b.length, count1);

        ArrayList<NullItem> b2 = snapshot(b);

        assertNotEquals(b1, b2);

        //System.out.println(b1);
        //System.out.println(b2);
        //cb.printAll();

        assertTrue(cb.getPriorityMin() <  cb.getPriorityMax());

    }

    protected static ArrayList<NullItem> snapshot(NullItem[] b) {

        ArrayList<NullItem> b1;

        //test for nulls
        for (NullItem o : b)
            if (o == null)
                throw new RuntimeException(Arrays.toString(b) + " has at least 1 null");

        b1 = Lists.newArrayList(b);

        Arrays.fill(b, null);

        return b1;
    }
}
