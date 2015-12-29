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
    public BagBudget<V> get(Object key) {
        return null;
    }

    @Override
    public BagBudget<V> sample() {
        return null;
    }

    @Override
    public BagBudget<V> remove(V key) {
        return null;
    }



    @Override
    public BagBudget<V> put(Object newItem) {
        return null;
    }

    @Override
    public BagBudget<V> put(Object i, Budget b, float scale) {
        return null;
    }

    @Override
    public int sample(int n, Predicate<BagBudget> each, Collection<BagBudget<V>> target) {
        throw new RuntimeException("unimpl");
    }

    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public BagBudget<V> pop() {
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
    public void forEachEntry(Consumer<BagBudget> each) {

    }

    @Override
    public void whileEachEntry(Predicate<BagBudget<V>> each) {

    }

    @Override
    public void forEachEntry(int limit, Consumer<BagBudget> each) {

    }

    @Override
    public void setCapacity(int c) {

    }
}
