package nars.bag;

import nars.analyze.experimental.BagPerf.NullItem;
import nars.bag.impl.CurveBag;
import nars.bag.impl.LevelBag;
import nars.model.impl.Default;
import nars.Memory;
import nars.NAR;
import nars.Param;
import nars.Global;
import nars.nal.Item;
import nars.bag.impl.CurveBag.BagCurve;
import nars.util.sort.ArraySortedIndex;
import nars.util.data.sorted.SortedIndex;
import objenome.util.random.XORShiftRandom;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 *
 * @author me
 */
public class CurveBagTest extends AbstractBagTest {

    final static Random rng = new XORShiftRandom();

    static {
        Global.DEBUG = true;
    }

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

        testCurveBag(true, items);
        testCurveBag(false, items);
        testCapacityLimit(new CurveBag(rng, 4, curve, true, items));
        testCapacityLimit(new CurveBag(rng, 4, curve, false, items));
        
        
        testAveragePriority(4, items);
        testAveragePriority(8, items);        
        
        int d[] = null;
        for (int capacity : new int[] { 10, 51, 100, 256 } ) {
            d = testRemovalPriorityDistribution(capacity, true, items);
        }
        
        return d;
    }
    
    public void testCurveBag(boolean random, SortedIndex<NullItem> items) {
        CurveBag<CharSequence, NullItem> f = new CurveBag(rng, 4, curve, random, items);
        
        f.put(new NullItem(.25f));
        assert(f.size() == 1);
        assert(f.mass() > 0);
        
        f.put(new NullItem(.9f));
        f.put(new NullItem(.75f));
        
        //System.out.println(f);
        
        //sorted
        assert(f.size() == 3);
        assert(f.items.get(0).getPriority() < f.items.get(1).getPriority());

        f.pop();
        
        assert(f.size() == 2);
        f.pop();
        assert(f.size() == 1);
        f.pop();
        assert(f.size() == 0);
        
        assert(f.mass() == 0);
    }

    public void testCapacityLimit(Bag<CharSequence,NullItem> f) {
        
        NullItem four = new NullItem(.4f);
        NullItem five = new NullItem(.5f);
        
        f.put(four);
        f.put(five);
        f.put(new NullItem(.6f));
        Item a = f.put(new NullItem(.7f));
        assert(a==null);
        
        assertEquals(4, f.size());

        assertEquals(f.size(), f.keySet().size());
                
        assertTrue(f.contains(five));    //5 should be in lowest position
        
        f.put(new NullItem(.8f)); //limit
        
        assertTrue(!f.contains(four)); //4 should get removed
        
        assertEquals(4, f.size());
    }
    
    
    
    
    public static void testRemovalDistribution(int capacity, boolean random) {
        int samples = 128 * capacity;
        
        int count[] = new int[capacity];
        
        SortedIndex<NullItem> items = new ArraySortedIndex<>(capacity);
        CurveBag<CharSequence, NullItem> f = new CurveBag(rng, capacity, curve, random, items);
        
        //fill
        for (int i= 0; i < capacity; i++) {
            f.put(new NullItem());
        }
        
        assertEquals(f.size(), f.capacity());
        
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
        
        CurveBag<CharSequence, NullItem> c = new CurveBag(rng, capacity, curve, false, items);
        LevelBag<NullItem,CharSequence> d = new LevelBag<>(capacity, 10);
        
        assertEquals(c.mass(), d.mass(), 0);
        assertEquals(c.getPriorityMean(), d.getPriorityMean(), 0);

        c.printAll(System.out);

        c.put(new NullItem(.25f));
        d.put(new NullItem(.25f));

        c.printAll(System.out);

        //check that continuousbag and discretebag calculate the same average priority value        
        assertEquals(0.25f, c.getPriorityMean(), priorityEpsilon);
        assertEquals(0.25f, d.getPriorityMean(), priorityEpsilon);
        
        c.clear();
        d.clear();
        
        assert(c.getPriorityMean() == 0.01f);
        assert(d.getPriorityMean() == 0.01f);
        
        c.put(new NullItem(.30f));
        d.put(new NullItem(.30f));
        c.put(new NullItem(.50f));
        d.put(new NullItem(.50f));
        
        assertEquals(0.4, c.getPriorityMean(), priorityEpsilon);
        assertEquals(0.4, d.getPriorityMean(), priorityEpsilon);

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


    @Test public void testEqualBudgetedItems() {
        int capacity = 4;

        CurveBag<CharSequence, NullItem> c = new CurveBag(rng, capacity, curve, true);

        NullItem a, b;
        c.put(a = new NullItem(0.5f));
        c.put(b = new NullItem(0.5f));

        assertEquals(2, c.size());

        NullItem aRemoved = c.remove(a.name());

        assertEquals(aRemoved, a);
        assertNotEquals(aRemoved, b);
        assertEquals(1, c.size());

        c.put(a);
        assertEquals(2, c.size());

        NullItem x = c.peekNext();
        assertNotNull(x);

        assertEquals(2, c.size());

        x = c.pop();

        assertTrue(x.equals(a) || x.equals(b));
        assertEquals(1, c.size());

    }

}
