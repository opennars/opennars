package nars.test.util;

import nars.core.Param;
import nars.core.build.DefaultNARBuilder;
import nars.perf.BagPerf.NullItem;
import nars.storage.AbstractBag;
import nars.storage.Bag;
import nars.storage.ContinuousBag;
import nars.storage.ContinuousBag2;
import nars.storage.DefaultBag;
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
        ContinuousBag<NullItem> f = new ContinuousBag(4, p.conceptForgetDurations, random);
        
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
        ContinuousBag2<NullItem> f = new ContinuousBag2(4, p.conceptForgetDurations, new ContinuousBag2.DefaultBagCurve(), random);
        
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
    
    public void testFastBagCapacityLimit(AbstractBag f) {
        
        NullItem four = new NullItem(.4f);
        NullItem five = new NullItem(.5f);
        
        f.putIn(four);
        f.putIn(five);
        f.putIn(new NullItem(.6f));
        boolean a = f.putIn(new NullItem(.7f));
        assert(a);
        
        f.putIn(new NullItem(.6f)); //limit

        assertEquals(4, f.size());

        assertEquals(f.size(), f.keySet().size());
        
        assertTrue(f.contains(five));    //5 should be in lowest position
        assertTrue(!f.contains(four)); //4 should get removed
        
        
        f.putIn(new NullItem(.8f)); //limit
        assertEquals(4, f.size());
    }
    
    public void testFastBagCapacityLimit() {
        testFastBagCapacityLimit(new ContinuousBag(4, p.conceptForgetDurations, true));
        testFastBagCapacityLimit(new ContinuousBag(4, p.conceptForgetDurations, false));
        testFastBagCapacityLimit(new ContinuousBag2(4, p.conceptForgetDurations, new ContinuousBag2.DefaultBagCurve(), true));
        testFastBagCapacityLimit(new ContinuousBag2(4, p.conceptForgetDurations, new ContinuousBag2.DefaultBagCurve(),false));
    }
    
    
    
    public void testRemovalDistribution(int N, boolean random) {
        int samples = 256 * N;
        
        int count[] = new int[N];
        
        ContinuousBag<NullItem> f = new ContinuousBag(N, p.conceptForgetDurations, random);
        
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
        
        ContinuousBag<NullItem> c = new ContinuousBag(capacity, p.conceptForgetDurations, false);
        Bag<NullItem> d = new DefaultBag<NullItem>(capacity, 10, p.conceptForgetDurations);
        
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
