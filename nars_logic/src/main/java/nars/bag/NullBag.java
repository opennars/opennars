package nars.bag;

import nars.budget.Budget;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Bag which holds nothing
 */
public final class NullBag<V> extends Bag<V> {

    @Override
    public void clear() {

    }

    @Override
    public BLink<V> get(Object key) {
        return null;
    }

    @Override
    public BLink<V> sample() {
        return null;
    }

    @Override
    public BLink<V> remove(V key) {
        return null;
    }

    @Override
    public BLink<V> put(Object newItem) {
        return null;
    }

    @Override
    public BLink<V> put(Object i, Budget b, float scale) {
        return null;
    }

    @Override
    public NullBag<V> sample(int n, Predicate<BLink> each, Collection<BLink<V>> target) {
        throw new RuntimeException("unimpl");
    }

    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public BLink<V> pop() {
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
    public void commit() {

    }

    @Override
    public void top(Consumer<BLink> each) {

    }

    @Override
    public void topWhile(Predicate<BLink> each) {

    }

    @Override
    public void topN(int limit, Consumer<BLink> each) {

    }

    @Override
    public void setCapacity(int c) {

    }
}
