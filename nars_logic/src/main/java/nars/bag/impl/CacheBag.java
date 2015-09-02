package nars.bag.impl;

import nars.budget.Itemized;
import org.apache.commons.math3.util.FastMath;

import java.util.function.Consumer;


public interface CacheBag<K, V extends Itemized<K>> extends Iterable<V> {
    

    public void clear();

    public V get(K key);

    public V remove(K key);

    /** same semantics as Map.put; output value is an existing value or null if none */
    public V put(V v);

    public int size();

    public void setOnRemoval(Consumer<V> onRemoval);
    public Consumer<V> getOnRemoval();

    default void delete() {

    }

    default public double[] getPriorityHistogram(int bins) {
        return getPriorityHistogram(new double[bins]);
    }

    default public double[] getPriorityHistogram(final double[] x) {
        int bins = x.length;
        forEach(e -> {
            final float p = e.getPriority();
            final int b = decimalize(p, bins - 1);
            x[b]++;
        });
        double total = 0;
        for (double e : x) {
            total += e;
        }
        if (total > 0) {
            for (int i = 0; i < bins; i++)
                x[i] /= total;
        }
        return x;
    }

    public static int decimalize(final float x, final int bins) {
        return (int) FastMath.floor((x + (0.5f / bins)) * bins);
    }

    /** bins a priority value to an integer */
    public static int decimalize(float v) {
        return decimalize(v,10);
    }


    /** finds the mean value of a given bin */
    public static float unbinCenter(final int b, final int bins) {
        return ((float)b)/bins;
    }

}
