package nars.bag.impl;

import com.google.common.collect.Sets;
import nars.Memory;
import nars.budget.Itemized;
import org.apache.commons.math3.util.FastMath;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;


public interface CacheBag<K, V extends Itemized<K>> extends Iterable<V>, Serializable {
    

    void clear();

    V get(K key);

    V remove(K key);

    /** same semantics as Map.put; output value is an existing value or null if none */
    V put(V v);

    int size();

    /** called when memory is ready to begin using this,
     *  allows letting the cache be aware of the memory
     */
    void start(Memory n);

//    void setOnRemoval(Consumer<V> onRemoval);
//    Consumer<V> getOnRemoval();

    default void delete() {

    }

    /**  by value set */
    public static <K,V extends Itemized<K>> boolean equals(CacheBag<K,V> a, CacheBag<K,V> b) {
        if (a.size()!=b.size()) return false;
        HashSet<V> aa = Sets.newHashSet(a);
        HashSet<V> bb = Sets.newHashSet(b);

        //TODO test for budget equality, which must be done separately
        return aa.equals(bb);
    }

//
//    /** performs an exhaustive element comparison of two bags */
//    public static <K,V extends Itemized<K>> boolean equalsInSequence(CacheBag<K,V> a, CacheBag<K,V> b) {
//        if (a == b) return true;
//        if (a.getClass()!=b.getClass())
//            return false;
//
//        Iterator<V> iterator1 = a.iterator();
//        Iterator<V> iterator2 = b.iterator();
//
//        while(true) {
//            if(iterator1.hasNext()) {
//                if(!iterator2.hasNext()) {
//                    return false;
//                }
//
//                V o1 = iterator1.next();
//                V o2 = iterator2.next();
//                if (Objects.equal(o1, o2) && o1.getBudget().equalsBudget(o2.getBudget())) {
//                    continue;
//                }
//
//                return false;
//            }
//
//            return !iterator2.hasNext();
//        }
//
//        //return Iterables.elementsEqual(a, b);
//    }
//

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

    @Override
    Iterator<V> iterator();
}
