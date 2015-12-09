package nars.bag;

import nars.bag.impl.CurveBag;
import nars.bag.impl.HeapBag;
import nars.bag.impl.LevelBag;
import nars.bag.impl.experimental.ChainBag;
import nars.budget.Item;
import nars.util.Texts;
import nars.util.data.Util;
import nars.util.data.random.XorShift1024StarRandom;
import nars.util.meter.bag.NullItem;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static junit.framework.Assert.assertEquals;

/**
 * Created by me on 7/3/15.
 */
public class BagSamplingPriorityTest {

    public static class BagSampleAnalysis<K,V extends Item<K>> {

        public final Bag<K, V> bag;

        public final Frequency removal = new Frequency();

        final int bins;

        final double binWidth;

        public BagSampleAnalysis(Bag<K, V> bag, int bins) {
            this.bag = bag;
            this.bins = bins;

            binWidth = 1.0 / bins;
        }

        public void clear() {
            bag.clear();
            removal.clear();
        }

        public int discretize(final float p) {
            //return (int)Math.floor(p * (bins-1));
            return Util.bin(p, bins);
        }
        public double undiscretize(int bin) {
            return Util.unbinCenter(bin, bins);
            //return ((double)bin)/(bins-1);
        }

        public void run(int iterations) {

            for (int i = 0; i < iterations; i++) {
                record(removal, bag.peekNext());
            }

        }

        private void record(final Frequency removal, final V p) {
            if (p != null) {
                removal.addValue(discretize(p.getPriority()));
            } else {
                removal.addValue(-1);
            }
        }

        public Frequency getBagState() {
            Frequency f = new Frequency();
            for (V v : bag) {
                record(f, v);
            }
            return f;
        }

        public DescriptiveStatistics getErrorStats() {
            return getErrorStats(null);
        }

        public double getErrorMean() {
            return getErrorStats().getMean();
        }

        public DescriptiveStatistics getErrorStats(PrintWriter p) {

            Frequency bagged = getBagState();

            DescriptiveStatistics errorRate = new DescriptiveStatistics();
            Iterator<Map.Entry<Comparable<?>, Long>> i = removal.entrySetIterator();
            while (i.hasNext()) {
                Map.Entry<Comparable<?>, Long> e = i.next();
                int bin = ((Number)e.getKey()).intValue();
                if (bin < 0) continue;

                double pri = undiscretize(bin);
                double prob = removal.getPct(bin);

                double a = Math.max(pri - binWidth / 2.0, 0);
                double b = Math.min(pri + binWidth / 2.0, 1.0);
                double pctBagItemsInBin = bagged.getPct(bin);
                double idealProb =
                        getIdealProbabilityForLinearPriorityProbabilityCurve(a, b);


                double diff = Math.abs(idealProb - prob);


                double error = Math.abs((prob - idealProb) / idealProb);

                //normalize the error by the actual number percentage of items this bin holds
                double errorNormalized = error * pctBagItemsInBin;

                if (p!=null) {
                    p.println(
                            Texts.n3(a) + ".." + Texts.n3(b) + "(" + Texts.n3(pctBagItemsInBin) + "):   " +
                                    "prob: " + Texts.n3(prob) + "    idealProb: " + Texts.n3(idealProb) + "    err: " + Texts.n3(errorNormalized));
                }


                errorRate.addValue(errorNormalized);
            }

            return errorRate;
        }

        /** integrates the area under the flat probability distribution curve, y=x
         * which has integral x*x*0.5
         * @param max
         * @param min
         * @return
         */
        private double getIdealProbabilityForLinearPriorityProbabilityCurve(double min, double max) {
            double b = max*max*0.5;
            double a = min*min*0.5;
            return b - a;
        }

    }

    public static class DefaultBagSampleAnalysis extends BagSampleAnalysis<CharSequence, NullItem> {

        public DefaultBagSampleAnalysis(Bag<CharSequence, NullItem> bag, int bins) {
            super(bag, bins);
        }

        public void add(float pri) {
            bag.put(new NullItem(pri));
        }

        /** fills the bag to capacity with values determined by a distribution curve */
        public void populateCurve(Function<Float,Float> curve) {
            double dp = 1.0 / bag.capacity();
            for (float i = 0; i < 1.0f; i+= dp) {
                add(curve.apply(i));
            }
        }

        public void populateUniform() {
            populateCurve(x -> x);
        }
    }

    static final Random rng = new XorShift1024StarRandom(1);

    static final Function<Float,Float> uniform = ( x -> x);
    static final Function<Float,Float> skewedLow = ( x -> (float)Math.pow(1.0f - x, 2) );


    @Test
    public void testBags() {

        int capacity = 64;

        int bins = (int)Math.sqrt(capacity)/2;

        int iterations = capacity * capacity;
        //CurveBag<CharSequence, BagPerf.NullItem> c = new CurveBag(rng, capacity);


        for (int i = 0; i < 2; i++) {

            Function<Float, Float> distr = null;

            System.out.print("ITEM PRIORITY DISTRIBUTION: ");
            switch (i) {
                case 0:
                    distr = uniform;
                    System.out.println("UNIFORM");
                    break;
                case 1:
                    distr = skewedLow;
                    System.out.println("SKEWED LOW");
                    break;
            }

            for (int levels = 1; levels < Math.sqrt(capacity)*2; levels+=3) {


                System.out.println("  LevelBag l=" + levels + ":  err=" +
                                getBagError(new LevelBag(levels, capacity), iterations,
                                        capacity, bins,
                                        distr)
                );
            }
            for (CurveBag.BagCurve c : new CurveBag.BagCurve[]{
                    new CurveBag.FairPriorityProbabilityCurve(),
                    new CurveBag.Power6BagCurve(),
                    new CurveBag.Power4BagCurve(),
                    new CurveBag.CubicBagCurve(),
                    new CurveBag.QuadraticBagCurve()
            }) {

                System.out.println("  CurveBag curve=" + c + ":  err=" +
                                getBagError(new CurveBag(c, capacity, rng), iterations,
                                        capacity, bins,
                                        distr)
                );
            }

            System.out.println("  ChainBag:  err=" +
                            getBagError(new ChainBag(rng, capacity), iterations,
                                    capacity, bins,
                                    distr)
            );

            System.out.println("  HeapBag:  err=" +
                            getBagError(new HeapBag(rng, capacity), iterations,
                                    capacity, bins,
                                    distr)
            );


            System.out.println("\n");

        }

    }

    public static double getBagError(Bag<CharSequence, NullItem> c, int iterations, int capacity, int bins, Function<Float, Float> itemDistributionCurve) {


        DefaultBagSampleAnalysis a = new DefaultBagSampleAnalysis(c, bins);

        a.populateCurve(itemDistributionCurve);

        assertEquals("filled to capacity", c.capacity(), c.size());



        //System.out.println(a.getBagState());

        a.run(iterations);

        assertEquals("discards nothing since nothing was added", c.capacity(), c.size());

        //System.out.println(a.removal);
        DescriptiveStatistics e = a.getErrorStats();

        return e.getMean();
    }


}
