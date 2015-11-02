package nars.bag;

import nars.bag.impl.CurveBag;
import nars.meter.bag.BagGenerators;
import nars.meter.bag.NullItem;
import nars.util.data.random.XORShiftRandom;
import nars.util.data.sorted.SortedIndex;
import org.junit.Assert;

import java.util.Random;

/**
 * Created by me on 1/18/15.
 */
public class AbstractBagTest {
    final static Random rng = new XORShiftRandom();

    public static int[] testRemovalPriorityDistribution(int loops, int insertsPerLoop, float fractionToAdjust, float fractionToRemove, Bag<CharSequence, NullItem> f, boolean requireOrder) {

        int levels = 9;
        int count[] = new int[levels];

        float adjustFraction = fractionToAdjust;
        float removeFraction = fractionToRemove;


        for (int l = 0; l < loops; l++) {
            //fill with random items
            for (int i = 0; i < insertsPerLoop; i++) {
                NullItem ni = new NullItem(
                        rng.nextFloat(),
                        "" + (int) (rng.nextFloat() * insertsPerLoop * 1.2f)
                );
                f.put(ni);
            }


            int preadjustCount = f.size();
            //assertEquals(items.size(), f.size());

            //remove some, adjust their priority, and re-insert
            for (int i = 0; i < insertsPerLoop * adjustFraction; i++) {
                f.size();

                NullItem t = f.pop();

                f.size();

                if (t == null) break;
                if (i % 2 == 0)
                    t.setPriority(t.getPriority() * 0.99f);
                else
                    t.setPriority(Math.min(1.0f, t.getPriority() * 1.01f));

                f.put(t);
            }

            int postadjustCount = f.size();
            //assertEquals(items.size(), f.size());

            Assert.assertEquals(preadjustCount, postadjustCount);

            float min = f.getPriorityMin();
            float max = f.getPriorityMax();
            if (requireOrder) {
                if (min > max) {
                    f.printAll();
                }
                Assert.assertTrue(max >= min);
            }


            int nRemoved = 0;
            //remove last than was inserted so the bag never gets empty
            for (int i = 0; i < insertsPerLoop * removeFraction; i++) {
                int sizeBefore = f.size();

                NullItem t = f.pop();

                int sizeAfter = f.size();

                if (t == null) {
                    Assert.assertTrue(sizeAfter == 0);
                    Assert.assertEquals(sizeAfter, sizeBefore);
                    continue;
                } else {
                    Assert.assertEquals(sizeAfter, sizeBefore - 1);
                }

                float p = t.getPriority();

                String expected = (min + " > " + p + " > " + max);
                if (requireOrder) {
                    Assert.assertTrue(expected, p >= min);
                    Assert.assertTrue(expected, p <= max);
                }

                int level = (int) Math.floor(p * levels);
                if (level >= count.length) level = count.length - 1;
                count[level]++;
                nRemoved++;
            }

            Assert.assertEquals(postadjustCount - nRemoved, f.size());

            //nametable and itemtable consistent size
            //assertEquals(items.size(), f.size());
            //System.out.printMeaning("  "); items.reportPriority();

        }
        return count;
    }

    public static int[] testRemovalPriorityDistribution(SortedIndex<NullItem> items) {
        CurveBag<CharSequence, NullItem> f = new CurveBag(items, CurveBag.power6BagCurve, rng);
        return BagGenerators.testRemovalPriorityDistribution(8, f.capacity(), 0.2f, f);
    }
}
