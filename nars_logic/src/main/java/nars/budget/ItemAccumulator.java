package nars.budget;

import nars.bag.BLink;
import nars.bag.impl.ArrayBag;
import nars.util.ArraySortedIndex;
import nars.util.data.sorted.SortedIndex;

import java.io.PrintStream;

/** priority queue which merges equal tasks and accumulates their budget.
 * stores the highest item in the last position, and lowest item in the first.
 * one will generally only need to use these methods:
 *      --limit(n) - until size <= n: remove lowest items
 *      --next(n, consumer(n)) - visit N highest items
 *
 * */
public class ItemAccumulator<V extends Budgeted > {


    private final MyArrayBag arrayBag;

    public ItemAccumulator(int capacity) {
        arrayBag = new MyArrayBag(new ArraySortedIndex(capacity));
        arrayBag.mergePlus();
    }


    public void print(PrintStream out) {
        arrayBag.items.print(out);
    }

    public ArrayBag<V> getArrayBag() {
        return arrayBag;
    }

    private class MyArrayBag extends ArrayBag<V> {
        public MyArrayBag(SortedIndex<BLink<V>> items) {
            super(items);
        }

        @Override
        public final BLink<V> sample() {
            return items.getFirst();
        }

        @Override
        public BLink<V> pop() {
            return removeHighest();
        }

        @Override
        public void update(BLink<V> v) {
            super.update(v);
            v.get().getBudget().set(v); //TODO replace instance's budget on insert so this copy isnt necessary
        }

        @Override
        public final boolean contains(V t) {
            return index.containsKey(t);
        }
    }



}
