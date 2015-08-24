package nars.bag;

import com.google.common.collect.Iterables;
import nars.Global;
import nars.NAR;
import nars.Param;
import nars.analyze.experimental.NullItem;
import nars.bag.impl.CurveBag;
import nars.bag.impl.CurveBag.BagCurve;
import nars.bag.impl.HeapBag;
import nars.bag.impl.LevelBag;
import nars.budget.Item;
import nars.nar.Default;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author me
 */
public class HeapBagTest extends AbstractBagTest {

    static {
        Global.DEBUG = true;

    }

    Param p = new NAR(new Default()).param;
    final static BagCurve curve = new CurveBag.FairPriorityProbabilityCurve();

    @Test 
    public void testBags() {
        for (int capacity : new int[] { 4, 7, 13, 16, 100 } ) {
            testRemovalDistribution(capacity);
        }
        
        //FractalSortedItemList<NullItem> f1 = new FractalSortedItemList<>();
        //int[] d2 = testCurveBag(f1);
        //int[] d3 = testCurveBag(new RedBlackSortedIndex<>());        
        int[] d1 = testBag(40);

        
        //use the final distribution to compare that each implementation generates exact same results
        //assertTrue(Arrays.equals(d1, d2));
        //assertTrue(Arrays.equals(d2, d3));
        


    }
    
    public int[] testBag(int items) {

        testHeapBag(items);
        testCapacityLimit(new HeapBag(rng, 4, curve));
        
        
        testAveragePriority(4);
        testAveragePriority(8);
        
        int d[] = null;
//        for (int capacity : new int[] { 10, 51, 100, 256 } ) {
//            d = testRemovalPriorityDistribution(capacity, true);
//        }
//
        return d;
    }
    
    public void testHeapBag(int items) {
        HeapBag<CharSequence, NullItem> f = new HeapBag(rng, items, curve);
        
        f.put(new NullItem(.25f));
        assert(f.size() == 1);
        assert(f.getPrioritySum() > 0);
        
        f.put(new NullItem(.9f));
        f.put(new NullItem(.75f));
        
        //System.out.println(f);
        
        //sorted
        assert(f.size() == 3);
        //assert(f.items.get(0).getPriority() < f.items.get(1).getPriority());

        f.pop();
        
        assert(f.size() == 2);
        f.pop();
        assert(f.size() == 1);
        f.pop();
        assert(f.isEmpty());
        
        assertEquals(0, f.getPrioritySum(), 0.001f);
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
                
        assertTrue(five + "? " + Iterables.toString(f), f.contains(five));    //5 should be in lowest position
        
        f.put(new NullItem(.8f)); //limit

        assertEquals(4, f.size());

        //assertTrue(four + "? " + Iterables.toString(f), !f.contains(four)); //4 should get removed
        
    }
    
    
    
    
    public static void testRemovalDistribution(int capacity) {
        int samples = 128 * capacity;
        
        int count[] = new int[capacity];
        
        HeapBag<CharSequence, NullItem> f = new HeapBag(rng, capacity, curve);
        
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
        
        //System.out.println(Arrays.toString(count));
        //System.out.println(count[0] + " " + count[1] + " " + count[2] + " " + count[3]);
        
    }

    public void testAveragePriority(int capacity) {
        
        
        final float priorityEpsilon = 0.01f;
        
        HeapBag<CharSequence, NullItem> c = new HeapBag(rng, capacity, curve);
        LevelBag<CharSequence, NullItem> d = new LevelBag<>(capacity, 10);
        
        assertEquals(c.getPrioritySum(), d.getPrioritySum(), 0);
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

        assertEquals(c.getPriorityMean(), 0f, 0.001f);
        assertEquals(d.getPriorityMean(), 0f, 0.001f);
        
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

        HeapBag<CharSequence, NullItem> c = new HeapBag(rng, capacity, curve);

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
