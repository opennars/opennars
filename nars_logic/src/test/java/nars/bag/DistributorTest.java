package nars.bag;

import com.gs.collections.impl.list.mutable.primitive.DoubleArrayList;
import nars.util.math.Distributor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author me
 */


public class DistributorTest {

    @Test
    public void testDistributorProbabilities() {

        int levels = 20;
        Distributor d = Distributor.get(levels);
        int[] count = new int[levels];

        double total = 0;
        for (short x : d.order) {
            count[x]++;
            total++;
        }

        List<Double> probability = new ArrayList(levels);
        for (int i = 0; i < levels; i++) {
            probability.add(count[i] / total);
        }

        List<Double> probabilityActiveAdjusted = new ArrayList(levels);
        double activeIncrease = 0.009;
        double dormantDecrease = ((0.1 * levels) * activeIncrease) / ((1.0 - 0.1) * levels);
        for (int i = 0; i < levels; i++) {
            double p = count[i] / total;
            double pd = i < ((1.0 - 0.1) * levels) ? -dormantDecrease : activeIncrease;

            p += pd;

            probabilityActiveAdjusted.add(p);
            //System.out.println((i / ((double) levels)) + "\t" + p);
        }
        //System.out.println(probabilityActiveAdjusted);
        DoubleArrayList a = new DoubleArrayList(probabilityActiveAdjusted.size());
        probabilityActiveAdjusted.forEach(a::add);
        double[] aa = a.toArray();

        assertArrayEquals(new double[]{
                        0.0037619047619047623, 0.008523809523809524, 0.013285714285714286, 0.01804761904761905, 0.022809523809523807, 0.02757142857142857, 0.03233333333333333, 0.0370952380952381, 0.04185714285714286, 0.046619047619047616, 0.05138095238095238, 0.05614285714285714, 0.060904761904761906, 0.06566666666666666, 0.07042857142857142, 0.0751904761904762, 0.07995238095238096, 0.08471428571428571, 0.09947619047619047, 0.10423809523809523},
                aa, 0.05);

    }


}
