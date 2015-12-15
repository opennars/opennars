package nars.bag;

import nars.budget.Budget;

import java.util.Iterator;
import java.util.function.Consumer;

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
    public BagBudget<V> peekNext() {
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
    public void update() {

    }

    @Override
    public void forEachEntry(Consumer<BagBudget> each) {

    }

    @Override
    public void forEachEntry(int limit, Consumer<BagBudget> each) {

    }

    @Override
    public void setCapacity(int c) {

    }
}
