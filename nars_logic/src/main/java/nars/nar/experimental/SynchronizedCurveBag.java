package nars.nar.experimental;

import nars.bag.BagTransaction;
import nars.bag.impl.CurveBag;
import nars.budget.Itemized;

import java.util.Random;

/**
 * Synchronized thread-safe CurveBag subclass
 */
public class SynchronizedCurveBag<K,V extends Itemized<K>> extends CurveBag<K,V> {

    final Object lock = new Object();

    public SynchronizedCurveBag(Random rng, int capacity) {
        super(rng, capacity);
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
}
