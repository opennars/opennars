package nars.test.util;

import nars.core.Param;
import nars.core.build.DefaultNARBuilder;
import nars.entity.Item;
import nars.perf.BagPerf.NullItem;
import nars.storage.Bag;
import nars.storage.LevelBag;
import nars.storage.ContinuousBag;
import nars.storage.ContinuousBag2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */


public class ContinuousBagTest {
    
    Param p = new DefaultNARBuilder().build().param();

    @Test 
    public void testContinuousBag() {
        testFastBag(false);
        testFastBag(true);        
        
        testFastBag2(false);
        testFastBag2(true);
        
        testFastBagCapacityLimit();


        testRemovalDistribution(13, false);
        testRemovalDistribution(13, true);
        
        testRemovalDistribution(4, false);
        testRemovalDistribution(4, true);
        
        testRemovalDistribution(7, false);
        testRemovalDistribution(7, true);
        
        testRemovalDistribution(16, false);
        testRemovalDistribution(16, true);

        
    }
    
    public void testFastBag(boolean random) {
        ContinuousBag<NullItem,CharSequence> f = new ContinuousBag(4, random);
        
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

    public void testFastBag2(boolean random) {
        ContinuousBag2<NullItem,CharSequence> f = new ContinuousBag2(4, new ContinuousBag2.PriorityProbabilityApproximateCurve(), random);
        
        f.putIn(new NullItem(.25f));
        assert(f.size() == 1);
        assert(f.getMass() > 0);
        
        f.putIn(new NullItem(.9f));
        f.putIn(new NullItem(.75f));
        assert(f.size() == 3);
        
        //System.out.println(f);

        //sorted
        assert(f.items.first().getPriority() < f.items.last().getPriority());
        assert(f.items.first().getPriority() < f.items.exact(1).getPriority());

        assert(f.items.size() == f.nameTable.size());
        
        assert(f.size() == 3);
        
        f.takeOut();
        assert(f.size() == 2);
        assert(f.items.size() == f.nameTable.size());        
        
        f.takeOut();
        assert(f.size() == 1);
        assert(f.items.size() == f.nameTable.size());        
        assert(f.getMass() > 0);
        
        f.takeOut();
        assert(f.size() == 0);
        assert(f.getMass() == 0);
        assert(f.items.size() == f.nameTable.size());                
        
    }
    
    public void testFastBagCapacityLimit(Bag f) {
        
        NullItem four = new NullItem(.4f);
        NullItem five = new NullItem(.5f);
        
        f.putIn(four);
        f.putIn(five);
        f.putIn(new NullItem(.6f));
        Item a = f.putIn(new NullItem(.7f));
        assert(a==null);
        
        f.putIn(new NullItem(.6f)); //limit

        assertEquals(4, f.size());

        assertEquals(f.size(), f.keySet().size());
        
        assertTrue(f.contains(five));    //5 should be in lowest position
        assertTrue(!f.contains(four)); //4 should get removed
        
        
        f.putIn(new NullItem(.8f)); //limit
        assertEquals(4, f.size());
    }
    
    public void testFastBagCapacityLimit() {
        testFastBagCapacityLimit(new ContinuousBag(4, true));
        testFastBagCapacityLimit(new ContinuousBag(4, false));
        testFastBagCapacityLimit(new ContinuousBag2(4, new ContinuousBag2.PriorityProbabilityApproximateCurve(), true));
        testFastBagCapacityLimit(new ContinuousBag2(4, new ContinuousBag2.PriorityProbabilityApproximateCurve(),false));
    }
    
    
    
    public void testRemovalDistribution(int N, boolean random) {
        int samples = 256 * N;
        
        int count[] = new int[N];
        
        ContinuousBag<NullItem,CharSequence> f = new ContinuousBag(N, random);
        
        //fill
        for (int i= 0; i < N; i++) {
            f.putIn(new NullItem());
        }
        
        assertEquals(f.size(), f.getCapacity());
        
        for (int i= 0; i < samples; i++) {
            count[f.nextRemovalIndex()]++;
        }
                
        //removal rates are approximately monotonically increasing function
        assert(count[0] <= count[N-2]);
        //assert(count[0] <= count[1]);
        //assert(count[0] < count[N-1]);        
        //assert(count[N-2] < count[N-1]);        
        assert(count[N/2] <= count[N-2]);
        
        //System.out.println(random + " " + Arrays.toString(count));
        //System.out.println(count[0] + " " + count[1] + " " + count[2] + " " + count[3]);
        
    }
    
    @Test
    public void testAveragePriority() {
        testAveragePriority(4);
        testAveragePriority(8);
    }
    
    public void testAveragePriority(int capacity) {
        
        
        final float priorityEpsilon = 0.01f;
        
        ContinuousBag<NullItem,CharSequence> c = new ContinuousBag(capacity, false);
        LevelBag<NullItem,CharSequence> d = new LevelBag<>(capacity, 10);
        
        assertEquals(c.getMass(), d.getMass(), 0);
        assertEquals(c.getAveragePriority(), d.getAveragePriority(), 0);
        
        c.putIn(new NullItem(.25f));
        d.putIn(new NullItem(.25f));
        
        //check that continuousbag and discretebag calculate the same average priority value        
        assertEquals(0.25f, c.getAveragePriority(), priorityEpsilon);
        assertEquals(0.25f, d.getAveragePriority(), priorityEpsilon);
        
        c.clear();
        d.clear();
        
        assert(c.getAveragePriority() == 0.01f);
        assert(d.getAveragePriority() == 0.01f);
        
        c.putIn(new NullItem(.30f));
        d.putIn(new NullItem(.30f));
        c.putIn(new NullItem(.50f));
        d.putIn(new NullItem(.50f));
        
        assertEquals(0.4, c.getAveragePriority(), priorityEpsilon);
        assertEquals(0.4, d.getAveragePriority(), priorityEpsilon);

    }
}
