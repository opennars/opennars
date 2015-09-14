package nars.bag.impl;

import nars.budget.Itemized;
import org.apache.commons.math3.util.FastMath;

import java.io.Serializable;
import java.util.function.Consumer;


public interface CacheBag<K, V extends Itemized<K>> extends Iterable<V>, Serializable {
    

    void clear();

    V get(K key);

    V remove(K key);

    /** same semantics as Map.put; output value is an existing value or null if none */
    V put(V v);

    int size();

    void setOnRemoval(Consumer<V> onRemoval);
    Consumer<V> getOnRemoval();

    default void delete() {

    }

    default double[] getPriorityHistogram(int bins) {
        return getPriorityHistogram(new double[bins]);
    }

    default double[] getPriorityHistogram(final double[] x) {
        int bins = x.length;
        forEach(e -> {
            final float p = e.getPriority();
            final int b = bin(p, bins - 1);
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

    static int bin(final float x, final int bins) {
        return (int) FastMath.floor((x + (0.5f / bins)) * bins);
    }

    /** bins a priority value to an integer */
    static int decimalize(float v) {
        return bin(v,10);
    }


    /** finds the mean value of a given bin */
    static float unbinCenter(final int b, final int bins) {
        return ((float)b)/bins;
    }

}
