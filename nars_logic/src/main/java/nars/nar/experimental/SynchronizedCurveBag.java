package nars.nar.experimental;

import nars.Memory;
import nars.bag.BagTransaction;
import nars.bag.impl.CurveBag;
import nars.budget.Itemized;
import nars.util.ArraySortedIndex;
import nars.util.data.list.FasterList;

import java.util.Random;
import java.util.function.Consumer;

/**
 * Synchronized thread-safe CurveBag subclass
 */
public class SynchronizedCurveBag<K, V extends Itemized<K>> extends CurveBag<K, V> {

    final Object lock = new Object();

    public SynchronizedCurveBag(Random rng, int capacity) {
        super(new ArraySortedIndex<>(
                        new FasterList<V>(capacity).asSynchronized(),
                        capacity
                ),
              CurveBag.power6BagCurve,
              rng);

    }

    @Override
    public V remove(K key) {
        synchronized (lock) {
            return super.remove(key);
        }
    }

    @Override
    public V update(BagTransaction<K, V> selector) {
        synchronized (lock) {
            return super.update(selector);
        }
    }

    @Override
    public V peekNext(final boolean remove) {
        synchronized (lock) {
            return super.peekNext(remove);
        }
    }

    @Override
    public V put(V i) {
        synchronized (lock) {
            return super.put(i);
        }
    }


    @Override
    public void forEach(int max, Consumer<? super V> action) {
        synchronized (lock) {
            super.forEach(max, action);
        }
    }

    @Override
    public int forgetNext(float forgetCycles, V[] batch, int start, int stop, long now, int maxAdditionalAttempts) {
        synchronized (lock) {
            return super.forgetNext(forgetCycles, batch, start, stop, now, maxAdditionalAttempts);
        }
    }

    @Override
    public V forgetNext() {
        synchronized (lock) {
            return super.forgetNext();
        }
    }

    @Override
    protected void setForgetNext(float forgetDurations, Memory m) {
        synchronized (lock) {
            super.setForgetNext(forgetDurations, m);
        }
    }
}
