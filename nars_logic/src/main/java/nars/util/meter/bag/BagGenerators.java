package nars.util.meter.bag;

import nars.bag.BLink;
import nars.bag.Bag;
import nars.util.data.Util;
import nars.util.data.random.XORShiftRandom;

import java.util.Random;


/**
 * Created by me on 9/2/15.
 */
public enum BagGenerators {
    ;

    static final Random rng = new XORShiftRandom();




//    public static int[] testRemovalPriorityDistribution(int loops, int insertsPerLoop, float fractionToAdjust, float fractionToRemove, Bag<CharSequence, NullItem> f) {
//        return testRemovalPriorityDistribution(loops, insertsPerLoop, fractionToAdjust, fractionToRemove, f, true);
//    }

    public static int[] testRemovalPriorityDistribution(int loops, int insertsPerLoop, float fractionToRemove, Bag<CharSequence> f) {

        int levels = 13;
        int[] count = new int[levels];

        int[] nRemoved = {0};

//        Consumer<NullItem> prevRemoval = f.getOnRemoval();
//        f.setOnRemoval(r -> {
//            nRemoved[0] = removal(nRemoved[0], r.getPriority(), count);
//            prevRemoval.accept(r);
//        });


        for (int l = 0; l < loops; l++) {


            //fill with random items
            for (int i= 0; i < insertsPerLoop; i++) {

                CharSequence k = Float.toString(rng.nextFloat());

                BLink<CharSequence> overflow = f.put(k);
                if (overflow!=null) {
                    float p = overflow.getPriority();
                    nRemoved[0] = removal(nRemoved[0], p, count);
                }

            }

            //System.out.println(f.size() + "/" + f.capacity() + " : " + f.getPriorityMin() + ".." + f.getPriorityMax());


            //assertEquals(items.size(), f.size());

            //Assert.assertEquals(preadjustCount, postadjustCount);

            float min = f.getPriorityMin();
            float max = f.getPriorityMax();
            if (min > max) {
                //f.printAll();
                throw new RuntimeException("out of order");
            }

            /*if (requireOrder) {

                //Assert.assertTrue(max >= min);
            }*/


            //remove last than was inserted so the bag never gets empty
            for (int i = 0; i < insertsPerLoop * fractionToRemove; i++) {
                int sizeBefore = f.size();

                CharSequence t = f.sample().get();

                if (t == null) {
                    //Assert.assertTrue(sizeAfter == 0);
                    //Assert.assertEquals(sizeAfter, sizeBefore);
                    continue;
                }
                //Assert.assertEquals(sizeAfter, sizeBefore-1);

                int sizeAfter = f.size();

                float p = f.get(t).getPriority();

                //String expected = (min + " > "+ p + " > " + max);
                /*if (requireOrder) {
                    //Assert.assertTrue(expected, p >= min);
                    //Assert.assertTrue(expected, p <= max);
                    throw new RuntimeException("out of order2");
                }*/

                nRemoved[0] = removal(nRemoved[0], p, count);
            }

            //Assert.assertEquals(postadjustCount-nRemoved, f.size());

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

    protected static int removal(int nRemoved, float p, int[] count) {

        int l = count.length;
        int level = Util.bin(p, l);
        if (level >= l) level= l - 1;
        count[level]++;
        nRemoved++;
        return nRemoved;
    }

    public static int[] testRetaining(int loops, int insertsPerLoop, Bag<CharSequence> f) {

        int levels = 9;
        int[] count = new int[levels];


        for (int l = 0; l < loops; l++) {
            //fill with random items
            for (int i= 0; i < insertsPerLoop; i++) {
                f.put(Float.toString(rng.nextFloat()));
            }



            //nametable and itemtable consistent size
            //assertEquals(items.size(), f.size());
            //System.out.printMeaning("  "); items.reportPriority();

        }


        return count;
    }

/*
            int preadjustCount = f.size();
            //assertEquals(items.size(), f.size());

            //remove some, adjust their priority, and re-insert
            for (int i= 0; i < insertsPerLoop * adjustFraction; i++) {
                f.size();

                NullItem t = f.pop();

                f.size();

                if (t == null) break;
                if (i % 2 == 0)
                    t.setPriority(t.getPriority() * 0.99f);
                else
                    t.setPriority(Math.min(1.0f, t.getPriority() * 1.01f));

                NullItem overflow = f.put(t);
                if (overflow!=null)
                    removal(nRemoved[0], overflow.getPriority(), count);
            }

            int postadjustCount = f.size();
 */
}
