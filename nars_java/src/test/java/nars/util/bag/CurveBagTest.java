package nars.util.bag;

import nars.core.Memory;
import nars.core.NAR;
import nars.core.Param;
import nars.build.Default;
import nars.logic.entity.Item;
import nars.analyze.BagPerf.NullItem;
import nars.util.bag.impl.CurveBag;
import nars.util.bag.impl.CurveBag.BagCurve;
import nars.util.bag.impl.LevelBag;
import nars.util.data.sorted.ArraySortedIndex;
import nars.util.data.sorted.SortedIndex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author me
 */
public class CurveBagTest extends AbstractBagTest {
    
    Param p = new NAR(new Default()).param;
    final static BagCurve curve = new CurveBag.FairPriorityProbabilityCurve();

    @Test 
    public void testBags() {

        
        //FractalSortedItemList<NullItem> f1 = new FractalSortedItemList<>();
        //int[] d2 = testCurveBag(f1);
        //int[] d3 = testCurveBag(new RedBlackSortedIndex<>());        
        int[] d1 = testCurveBag(new ArraySortedIndex<>(40));

        
        //use the final distribution to compare that each implementation generates exact same results
        //assertTrue(Arrays.equals(d1, d2));
        //assertTrue(Arrays.equals(d2, d3));
        
        for (int capacity : new int[] { 4, 7, 13, 16, 100 } ) {
            testRemovalDistribution(capacity, false);
            testRemovalDistribution(capacity, true);
        }

    }
    
    public int[] testCurveBag(SortedIndex<NullItem> items) {
        Memory.resetStatic(1);
        
        testCurveBag(true, items);
        testCurveBag(false, items);
        testCapacityLimit(new CurveBag(4, curve, true, items));
        testCapacityLimit(new CurveBag(4, curve, false, items));
        
        
        testAveragePriority(4, items);
        testAveragePriority(8, items);        
        
        int d[] = null;
        for (int capacity : new int[] { 10, 51, 100, 256 } ) {
            d = testRemovalPriorityDistribution(capacity, true, items);
        }
        
        return d;
    }
    
    public void testCurveBag(boolean random, SortedIndex<NullItem> items) {
        CurveBag<NullItem,CharSequence> f = new CurveBag(4, curve, random, items);
        
        f.PUT(new NullItem(.25f));
        assert(f.size() == 1);
        assert(f.getMass() > 0);
        
        f.PUT(new NullItem(.9f));
        f.PUT(new NullItem(.75f));
        
        //System.out.println(f);
        
        //sorted
        assert(f.size() == 3);
        assert(f.items.get(0).getPriority() < f.items.get(1).getPriority());

        f.TAKENEXT();
        
        assert(f.size() == 2);
        f.TAKENEXT();
        assert(f.size() == 1);
        f.TAKENEXT();
        assert(f.size() == 0);
        
        assert(f.getMass() == 0);
    }

    public void testCapacityLimit(Bag f) {
        
        NullItem four = new NullItem(.4f);
        NullItem five = new NullItem(.5f);
        
        f.PUT(four);
        f.PUT(five);
        f.PUT(new NullItem(.6f));
        Item a = f.PUT(new NullItem(.7f));
        assert(a==null);
        
        assertEquals(4, f.size());

        assertEquals(f.size(), f.keySet().size());
                
        assertTrue(f.contains(five));    //5 should be in lowest position
        
        f.PUT(new NullItem(.8f)); //limit
        
        assertTrue(!f.contains(four)); //4 should get removed
        
        assertEquals(4, f.size());
    }
    
    
    
    
    public void testRemovalDistribution(int capacity, boolean random) {
        int samples = 128 * capacity;
        
        int count[] = new int[capacity];
        
        SortedIndex<NullItem> items = new ArraySortedIndex<>(capacity);
        CurveBag<NullItem,CharSequence> f = new CurveBag(capacity, curve, random, items);
        
        //fill
        for (int i= 0; i < capacity; i++) {
            f.PUT(new NullItem());
        }
        
        assertEquals(f.size(), f.getCapacity());
        
        for (int i= 0; i < samples; i++) {
            count[f.nextRemovalIndex()]++;
        }
        
        //System.out.println(capacity +"," + random + " = " + Arrays.toString(count));
                
        assert(semiMonotonicallyIncreasing(count));
        
        //System.out.println(random + " " + Arrays.toString(count));
        //System.out.println(count[0] + " " + count[1] + " " + count[2] + " " + count[3]);
        
    }

    public void testAveragePriority(int capacity, SortedIndex<NullItem> items) {
        
        
        final float priorityEpsilon = 0.01f;
        
        CurveBag<NullItem,CharSequence> c = new CurveBag(capacity, curve, false, items);
        LevelBag<NullItem,CharSequence> d = new LevelBag<>(capacity, 10);
        
        assertEquals(c.getMass(), d.getMass(), 0);
        assertEquals(c.getAveragePriority(), d.getAveragePriority(), 0);
        
        c.PUT(new NullItem(.25f));
        d.PUT(new NullItem(.25f));
        
        //check that continuousbag and discretebag calculate the same average priority value        
        assertEquals(0.25f, c.getAveragePriority(), priorityEpsilon);
        assertEquals(0.25f, d.getAveragePriority(), priorityEpsilon);
        
        c.clear();
        d.clear();
        
        assert(c.getAveragePriority() == 0.01f);
        assert(d.getAveragePriority() == 0.01f);
        
        c.PUT(new NullItem(.30f));
        d.PUT(new NullItem(.30f));
        c.PUT(new NullItem(.50f));
        d.PUT(new NullItem(.50f));
        
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
