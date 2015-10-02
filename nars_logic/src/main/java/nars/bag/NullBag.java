package nars.bag;

import nars.budget.Itemized;

import java.util.Iterator;
import java.util.Set;

/**
 * Bag which holds nothing
 */
public final class NullBag<K, V extends Itemized<K>> extends Bag<K, V> {

    @Override
    public void clear() {

    }

    @Override
    public V peekNext() {
        return null;
    }

    @Override
    public V remove(K key) {
        return null;
    }

    @Override
    public V put(V newItem) {
        return newItem; //bounces
    }

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public V pop() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Iterator<V> iterator() {
        return null;
    }

    @Override
    public void setCapacity(int c) {

    }
}
