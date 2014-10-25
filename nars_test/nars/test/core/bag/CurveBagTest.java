package nars.test.core.bag;

import java.util.Arrays;
import nars.core.Param;
import nars.core.build.DefaultNARBuilder;
import nars.entity.Item;
import nars.perf.BagPerf.NullItem;
import nars.storage.Bag;
import nars.storage.LevelBag;
import nars.storage.CurveBag;
import nars.storage.CurveBag.BagCurve;
import nars.util.sort.ArraySortedItemList;
import nars.util.sort.FractalSortedItemList;
import nars.util.sort.RedBlackSortedItemList;
import nars.util.sort.SortedItemList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */


public class CurveBagTest {
    
    Param p = new DefaultNARBuilder().build().param();
    final BagCurve curve = new CurveBag.FairPriorityProbabilityCurve();

    @Test 
    public void testBags() {
        testCurveBag(new FractalSortedItemList<>());
        testCurveBag(new ArraySortedItemList<>());
        testCurveBag(new RedBlackSortedItemList<>());
    }
    
    public void testCurveBag(SortedItemList<NullItem> items) {
        testCurveBag(true, items);
        testCurveBag(false, items);
        testCapacityLimit(new CurveBag(4, curve, true, items));
        testCapacityLimit(new CurveBag(4, curve, false, items));
        
        for (int capacity : new int[] { 4, 7, 13, 16 } ) {
            testRemovalDistribution(capacity, false, items);
            testRemovalDistribution(capacity, true, items);        
        }
        
        for (int capacity : new int[] { 10, 100, 256, 1000 } ) {
            testRemovalPriorityDistribution(capacity, true, items);
        }
        testAveragePriority(4, items);
        testAveragePriority(8, items);        
    }
    
    public void testCurveBag(boolean random, SortedItemList<NullItem> items) {
        CurveBag<NullItem,CharSequence> f = new CurveBag(4, curve, random, items);
        
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

    public void testCapacityLimit(Bag f) {
        
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
    
    
    
    
    public void testRemovalDistribution(int capacity, boolean random, SortedItemList<NullItem> items) {
        int samples = 256 * capacity;
        
        int count[] = new int[capacity];
        
        CurveBag<NullItem,CharSequence> f = new CurveBag(capacity, curve, random, items);
        
        //fill
        for (int i= 0; i < capacity; i++) {
            f.putIn(new NullItem());
        }
        
        assertEquals(f.size(), f.getCapacity());
        
        for (int i= 0; i < samples; i++) {
            count[f.nextRemovalIndex()]++;
        }
        
                
        //removal rates are approximately monotonically increasing function
        assert(count[0] <= count[capacity-2]);
        //assert(count[0] <= count[1]);
        //assert(count[0] < count[N-1]);        
        //assert(count[N-2] < count[N-1]);        
        assert(count[capacity/2] <= count[capacity-2]);
        
        //System.out.println(random + " " + Arrays.toString(count));
        //System.out.println(count[0] + " " + count[1] + " " + count[2] + " " + count[3]);
        
    }

    public void testRemovalPriorityDistribution(int capacity, boolean random, SortedItemList<NullItem> items) {
        int loops = 4;
        
        int levels = 10;
        int count[] = new int[levels];
        
        float removeFraction = 0.5f;
        
        CurveBag<NullItem,CharSequence> f = new CurveBag(capacity, curve, random, items);
        
        for (int l = 0; l < loops; l++) {
            //fill with random items
            for (int i= 0; i < capacity; i++) {
                f.putIn(new NullItem());
            }

            for (int i= 0; i < capacity * removeFraction; i++) {
                NullItem t = f.takeOut();
                float p = t.getPriority();
                int level = (int)Math.floor(p * levels);
                count[level]++;
            }
            
            if (f.size() > 16) {
                float min = f.getMinPriority();
                float max = f.getMaxPriority();
                assertTrue(max > min);
            }
        }
        
        System.out.println(items.getClass().getSimpleName() + "," + random + "," + capacity + ": " + Arrays.toString(count));
        
                
        //removal rates are approximately monotonically increasing function
        //assert(count[0] <= count[N-2]);
        //assert(count[N/2] <= count[N-2]);
        
        //System.out.println(random + " " + Arrays.toString(count));
        //System.out.println(count[0] + " " + count[1] + " " + count[2] + " " + count[3]);
        
    }
    
    public void testAveragePriority(int capacity, SortedItemList<NullItem> items) {
        
        
        final float priorityEpsilon = 0.01f;
        
        CurveBag<NullItem,CharSequence> c = new CurveBag(capacity, curve, false, items);
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


//    public void testCurveBag2(boolean random) {
//        ContinuousBag2<NullItem,CharSequence> f = new ContinuousBag2(4, new ContinuousBag2.PriorityProbabilityApproximateCurve(), random);
//        
//        f.putIn(new NullItem(.25f));
//        assert(f.size() == 1);
//        assert(f.getMass() > 0);
//        
//        f.putIn(new NullItem(.9f));
//        f.putIn(new NullItem(.75f));
//        assert(f.size() == 3);
//        
//        //System.out.println(f);
//
//        //sorted
//        assert(f.items.first().getPriority() < f.items.last().getPriority());
//        assert(f.items.first().getPriority() < f.items.exact(1).getPriority());
//
//        assert(f.items.size() == f.nameTable.size());
//        
//        assert(f.size() == 3);
//        
//        f.takeOut();
//        assert(f.size() == 2);
//        assert(f.items.size() == f.nameTable.size());        
//        
//        f.takeOut();
//        assert(f.size() == 1);
//        assert(f.items.size() == f.nameTable.size());        
//        assert(f.getMass() > 0);
//        
//        f.takeOut();
//        assert(f.size() == 0);
//        assert(f.getMass() == 0);
//        assert(f.items.size() == f.nameTable.size());                
//        
//    }
    

}
