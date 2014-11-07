package nars.test.core.bag;

import nars.core.Memory;
import nars.core.Param;
import nars.core.build.Default;
import nars.entity.Item;
import nars.perf.BagPerf.NullItem;
import nars.storage.Bag;
import nars.storage.LevelBag;
import nars.storage.CurveBag;
import nars.storage.CurveBag.BagCurve;
import nars.util.sort.ArraySortedIndex;
import nars.util.sort.SortedIndex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */
public class CurveBagTest {
    
    Param p = new Default().build().param;
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
        Memory.resetStatic();
        
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
        
        f.putIn(new NullItem(.25f));
        assert(f.size() == 1);
        assert(f.getMass() > 0);
        
        f.putIn(new NullItem(.9f));
        f.putIn(new NullItem(.75f));
        
        //System.out.println(f);
        
        //sorted
        assert(f.size() == 3);
        assert(f.items.get(0).getPriority() < f.items.get(1).getPriority());

        f.takeNext();
        
        assert(f.size() == 2);
        f.takeNext();
        assert(f.size() == 1);
        f.takeNext();
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
        
        assertEquals(4, f.size());

        assertEquals(f.size(), f.keySet().size());
                
        assertTrue(f.contains(five));    //5 should be in lowest position
        
        f.putIn(new NullItem(.8f)); //limit
        
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
            f.putIn(new NullItem());
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

    public static int[] testRemovalPriorityDistribution(int capacity, boolean random, SortedIndex<NullItem> items) {
        CurveBag<NullItem,CharSequence> f = new CurveBag(capacity, curve, random, items);
        return testRemovalPriorityDistribution(8, capacity, 0.2f, 0.2f, f);
    }
    
    public static int[] testRemovalPriorityDistribution(int loops, int insertsPerLoop, float fractionToAdjust, float fractionToRemove, Bag<NullItem,CharSequence> f) {
        
        int levels = 9;
        int count[] = new int[levels];
        
        float adjustFraction = fractionToAdjust;
        float removeFraction = fractionToRemove;
        
        
        
        for (int l = 0; l < loops; l++) {
            //fill with random items
            for (int i= 0; i < insertsPerLoop; i++) {
                NullItem ni = new NullItem();
                ni.key = "" + (int)(Memory.randomNumber.nextFloat() * insertsPerLoop * 1.2f);
                f.putIn(ni);
            }
            

            int preadjustCount = f.size();
            //assertEquals(items.size(), f.size());
            
            //remove some, adjust their priority, and re-insert
            for (int i= 0; i < insertsPerLoop * adjustFraction; i++) {
                NullItem t = f.takeNext();
                if (i % 2 == 0)
                    t.budget.setPriority(t.budget.getPriority()*0.99f);
                else
                    t.budget.setPriority(Math.min(1.0f,t.budget.getPriority()*1.01f));
                f.putIn(t);
            }
            
            int postadjustCount = f.size();
            //assertEquals(items.size(), f.size());
            
            assertEquals(preadjustCount, postadjustCount);

            float min = f.getMinPriority();
            float max = f.getMaxPriority();
            assertTrue(max > min);
                        
            
            int nRemoved = 0;
            //remove last than was inserted so the bag never gets empty
            for (int i= 0; i < insertsPerLoop * removeFraction; i++) {
                int sizeBefore = f.size();
                
                NullItem t = f.takeNext();
                
                int sizeAfter = f.size();
                
                if (t == null) {
                    assertTrue(sizeAfter == 0);
                    assertEquals(sizeAfter, sizeBefore);
                    continue;
                }
                else {
                    assertEquals(sizeAfter, sizeBefore-1);
                }
                
                float p = t.getPriority();
                
                assertTrue(p >= min);
                assertTrue(p <= max);
                
                int level = (int)Math.floor(p * levels);      
                if (level >= count.length) level=count.length-1;
                count[level]++;
                nRemoved++;
            }
            
            assertEquals(postadjustCount-nRemoved, f.size());
            
            //nametable and itemtable consistent size
            //assertEquals(items.size(), f.size());
            //System.out.print("  "); items.reportPriority();            
            
        }

        
        //System.out.println(items.getClass().getSimpleName() + "," + random + "," + capacity + ": " + Arrays.toString(count));
        
        return count;
        
                
        //removal rates are approximately monotonically increasing function
        //assert(count[0] <= count[N-2]);
        //assert(count[N/2] <= count[N-2]);
        
        //System.out.println(random + " " + Arrays.toString(count));
        //System.out.println(count[0] + " " + count[1] + " " + count[2] + " " + count[3]);
        
    }
    
    public void testAveragePriority(int capacity, SortedIndex<NullItem> items) {
        
        
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

    /** removal rates are approximately monotonically increasing function; tests first, mid and last for this  ordering */
    public static boolean semiMonotonicallyIncreasing(int[] count) {
        
        int cl = count.length;
        return 
                (count[0] <= count[cl-1]) &&
                (count[cl/2] <= count[cl-1]);
    }
    

}
