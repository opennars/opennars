package nars.test.util;

import nars.perf.BagPerf.NullItem;
import nars.storage.Bag;
import nars.storage.DefaultBag;
import nars.util.ContinuousBag;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */


public class ContinuousBagTest {
 
    @Test 
    public void testContinuousBag() {
        testFastBag(false);
        testFastBag(true);
        
        testFastBagCapacityLimit(false);
        testFastBagCapacityLimit(true);
        
        
        
        testRemovalDistribution(4, false);
        testRemovalDistribution(4, true);
        
        testRemovalDistribution(7, false);
        testRemovalDistribution(7, true);
        
        testRemovalDistribution(16, false);
        testRemovalDistribution(16, true);

        testRemovalDistribution(13, false);
        testRemovalDistribution(13, true);
        
    }
    
    public void testFastBag(boolean random) {
        ContinuousBag<NullItem> f = new ContinuousBag(4, 10, random);
        
        f.putIn(new NullItem(.25f));
        assert(f.size() == 1);
        assert(f.getMass() > 0);
        
        f.putIn(new NullItem(.9f));
        f.putIn(new NullItem(.75f));
        
        //System.out.println(f);
        
        //sorted
        assert(f.items.get(0).getPriority() < f.items.get(1).getPriority());

        assert(f.size() == 3);
        f.takeOut();
        
        assert(f.size() == 2);
        f.takeOut();
        assert(f.size() == 1);
        f.takeOut();
        assert(f.size() == 0);
        
        assert(f.getMass() == 0);
    }

    public void testFastBagCapacityLimit(boolean random) {
        ContinuousBag<NullItem> f = new ContinuousBag(4, 10, random);
        f.putIn(new NullItem());
        f.putIn(new NullItem());
        f.putIn(new NullItem());
        boolean a = f.putIn(new NullItem());
        assert(a);
        f.putIn(new NullItem()); //limit
        assert(f.size() == 4);
        f.putIn(new NullItem()); //limit
        assert(f.size() == 4);

    }
    
    public void testRemovalDistribution(int N, boolean random) {
        int samples = 256 * N;
        
        int count[] = new int[N];
        
        ContinuousBag<NullItem> f = new ContinuousBag(N, 10, random);
        
        //fill
        for (int i= 0; i < N; i++) {
            f.putIn(new NullItem());
        }
        
        for (int i= 0; i < samples; i++) {
            count[f.nextRemovalIndex()]++;
        }
        //System.out.println(Arrays.toString(count));
                
        //removal rates are approximately monotonically increasing function
        assert(count[0] <= count[N-1]);
        //assert(count[0] <= count[1]);
        //assert(count[0] < count[N-1]);        
        //assert(count[N-2] < count[N-1]);        
        assert(count[N/2] <= count[N-1]);
        
        //System.out.println(random + " " + Arrays.toString(count));
        //System.out.println(count[0] + " " + count[1] + " " + count[2] + " " + count[3]);
        
    }
    
    @Test
    public void testAveragePriority() {
        testAveragePriority(4);
        testAveragePriority(8);
    }
    
    public void testAveragePriority(int capacity) {
        
        final float priorityEpsilon = 0.1f;
        
        ContinuousBag<NullItem> c = new ContinuousBag(capacity, 10, false);
        Bag<NullItem> d = new DefaultBag<NullItem>(capacity, 10, 10);
        
        assertEquals(c.getMass(), d.getMass(), 0);
        assertEquals(c.getAveragePriority(), d.getAveragePriority(), 0);
        
        c.putIn(new NullItem(.25f));
        d.putIn(new NullItem(.25f));
        
        //check that continuousbag and discretebag calculate the same average priority value
        assertTrue(c.getMass() != d.getMass()); //method of tracking mass does not need to be the same
        assertEquals(c.getAveragePriority(), d.getAveragePriority(), priorityEpsilon);
        
        c.clear();
        d.clear();
        
        assert(c.getAveragePriority() == 0.01f);
        assert(d.getAveragePriority() == 0.01f);
        
        c.putIn(new NullItem(.25f));
        d.putIn(new NullItem(.25f));
        c.putIn(new NullItem(.87f));
        d.putIn(new NullItem(.87f));
        
        assertEquals(c.getAveragePriority(), d.getAveragePriority(), priorityEpsilon);
                //System.out.println(c.getAveragePriority() + " "+ d.getAveragePriority());

    }
}
