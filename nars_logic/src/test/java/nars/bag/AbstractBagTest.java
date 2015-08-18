package nars.bag;

import nars.analyze.experimental.NullItem;
import nars.bag.impl.CurveBag;
import nars.util.data.random.XORShiftRandom;
import nars.util.data.sorted.SortedIndex;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 1/18/15.
 */
public class AbstractBagTest {
    final static Random rng = new XORShiftRandom();

    public static int[] testRemovalPriorityDistribution(int capacity, SortedIndex<NullItem> items) {
        CurveBag<CharSequence, NullItem> f = new CurveBag(rng, capacity, CurveBagTest.curve, items);
        return testRemovalPriorityDistribution(8, capacity, 0.2f, 0.2f, f);
    }

    public static int[] testRemovalPriorityDistribution(int loops, int insertsPerLoop, float fractionToAdjust, float fractionToRemove, Bag<CharSequence, NullItem> f) {
        return testRemovalPriorityDistribution(loops, insertsPerLoop, fractionToAdjust, fractionToRemove, f, true);
    }

    public static int[] testRemovalPriorityDistribution(int loops, int insertsPerLoop, float fractionToAdjust, float fractionToRemove, Bag<CharSequence, NullItem> f, boolean requireOrder) {

        int levels = 9;
        int count[] = new int[levels];

        float adjustFraction = fractionToAdjust;
        float removeFraction = fractionToRemove;



        for (int l = 0; l < loops; l++) {
            //fill with random items
            for (int i= 0; i < insertsPerLoop; i++) {
                NullItem ni = new NullItem();
                ni.key = "" + (int)(rng.nextFloat() * insertsPerLoop * 1.2f);
                f.put(ni);
            }


            int preadjustCount = f.size();
            //assertEquals(items.size(), f.size());

            //remove some, adjust their priority, and re-insert
            for (int i= 0; i < insertsPerLoop * adjustFraction; i++) {
                NullItem t = f.pop();
                if (t == null) break;
                if (i % 2 == 0)
                    t.setPriority(t.getPriority() * 0.99f);
                else
                    t.setPriority(Math.min(1.0f, t.getPriority() * 1.01f));
                f.put(t);
            }

            int postadjustCount = f.size();
            //assertEquals(items.size(), f.size());

            assertEquals(preadjustCount, postadjustCount);

            float min = f.getPriorityMin();
            float max = f.getPriorityMax();
            if (requireOrder) {
                if (min > max) {
                    f.printAll();
                }
                assertTrue(max >= min);
            }


            int nRemoved = 0;
            //remove last than was inserted so the bag never gets empty
            for (int i= 0; i < insertsPerLoop * removeFraction; i++) {
                int sizeBefore = f.size();

                NullItem t = f.pop();

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

                String expected = (min + " > "+ p + " > " + max);
                if (requireOrder) {
                    assertTrue(expected, p >= min);
                    assertTrue(expected, p <= max);
                }

                int level = (int)Math.floor(p * levels);
                if (level >= count.length) level=count.length-1;
                count[level]++;
                nRemoved++;
            }

            assertEquals(postadjustCount-nRemoved, f.size());

            //nametable and itemtable consistent size
            //assertEquals(items.size(), f.size());
            //System.out.printMeaning("  "); items.reportPriority();

        }


        //System.out.println(items.getClass().getSimpleName() + "," + random + "," + capacity + ": " + Arrays.toString(count));

        return count;


        //removal rates are approximately monotonically increasing function
        //assert(count[0] <= count[N-2]);
        //assert(count[N/2] <= count[N-2]);

        //System.out.println(random + " " + Arrays.toString(count));
        //System.out.println(count[0] + " " + count[1] + " " + count[2] + " " + count[3]);

    }


    public static int[] testRetaining(int loops, int insertsPerLoop, Bag<CharSequence, NullItem> f) {

        int levels = 9;
        int count[] = new int[levels];


        for (int l = 0; l < loops; l++) {
            //fill with random items
            for (int i= 0; i < insertsPerLoop; i++) {
                NullItem ni = new NullItem(rng.nextFloat());
                f.put(ni);
            }



            //nametable and itemtable consistent size
            //assertEquals(items.size(), f.size());
            //System.out.printMeaning("  "); items.reportPriority();

        }


        return count;
    }


    /** removal rates are approximately monotonically increasing function;
     * tests first, mid and last for this  ordering
     * first items are highest, so it is actually descending order
     * */
    public static boolean semiMonotonicallyIncreasing(int[] count) {

        int cl = count.length;
        return
                (count[0] >= count[cl-1]) &&
                (count[cl/2] >= count[cl-1]);
    }
}
