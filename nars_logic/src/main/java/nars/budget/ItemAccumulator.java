package nars.budget;

import com.google.common.collect.Iterators;
import com.gs.collections.api.RichIterable;
import com.gs.collections.api.list.MutableList;
import com.gs.collections.api.tuple.Pair;
import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;

/** priority queue which merges equal tasks and accumulates their budget.
 * stores the highest item in the last position, and lowest item in the first.
 *
 * TODO reimplement merging functions (currently uses default Plus method)
 *
 * */
public class ItemAccumulator<I extends Item> {


    public final UnifiedMap<I,I> buffer = new UnifiedMap();

//    final Comparator<? super I> floatValueComparator = new Comparator<I>() {
//        @Override public final int compare(final I o1, final I o2) {
//            return Float.compare( o1.getPriority(), o2.getPriority() );
//        }
//    };

    final static Comparator<? super Pair<? extends Item,? extends Item>> highestFirst = new HighestFirstComparator();
    final static Comparator<? super Pair<? extends Item,? extends Item>> lowestFirst = new LowestFirstComparator();


    public ItemAccumulator(@Deprecated ItemComparator comp) {

        //this.buffer = new TreeSet(comp);
        ////this.newTasks = new ConcurrentSkipListSet(new TaskComparator(memory.param.getDerivationDuplicationMode()));
    }

    public void clear() {
        buffer.clear();
    }

    public boolean add(I t) {

        I accumulated = buffer.remove(t);

        if (accumulated!=null)
            accumulated.accumulate(t.getBudget());
        else
            accumulated = t;

        buffer.put(t, accumulated);

//        buffer.updateValue(t, 0, v -> {
//
//            return v + t.getPriority();
//        });

        return true;
    }

    public int size() {
        return buffer.size();
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    public I removeHighest() {
        if (buffer.isEmpty()) return null;
        I i = highest();
        buffer.remove(i);
        return i;
    }
    public I removeLowest() {
        if (buffer.isEmpty()) return null;
        I i = lowest();
        buffer.remove(i);
        return i;
    }

    public I lowest() {
        if (buffer.isEmpty()) return null;
        return lowestFirstKeyValues().getFirst().getTwo();
    }

    public I highest() {
        if (buffer.isEmpty()) return null;
        return lowestFirstKeyValues().getLast().getTwo();
    }



    private MutableList<Pair<I,I>> lowestFirstKeyValues() {
        return buffer.keyValuesView().toSortedList(lowestFirst);
    }
    private MutableList<Pair<I, I>> highestFirstKeyValues() {
        return buffer.keyValuesView().toSortedList(highestFirst);
    }

    /** iterates the items highest first */
    public Iterator<I> iterateSorted() {
        return Iterators.transform( highestFirstKeyValues().iterator(),
                input -> input.getTwo() );
    }
    public Iterator<I> iterateSortedInverse() {
        return Iterators.transform( lowestFirstKeyValues().iterator(),
                input -> input.getTwo() );
    }

    public int removeLowest(final int n) {
        final int s = buffer.size();
        if (s <= n) {
            buffer.clear();
            return s;
        }

        MutableList<Pair<I, I>> lf = lowestFirstKeyValues();

        int r;
        for (r = 0; r < n; r++) {
            buffer.remove( lf.get(r).getOne() );
        }

        return r;
    }

    public void addAll(Collection<I> x) {
        for (I i : x)
            add(i);
    }

    /** if size() capacity, remove lowest elements until size() is at capacity
     * @return how many removed
     * */
    public int limit(int capacity, Consumer<I> onRemoved) {

        int numToRemove = size() - capacity;

        if (numToRemove > 0)
            return removeLowest(numToRemove);

        return 0;
    }


    @Override
    public String toString() {
        return buffer.toString();
    }


    static final class HighestFirstComparator<I extends Item> implements Comparator<Pair<I, I>> {
        @Override
        public int compare(Pair<I, I> a, Pair<I, I> b) {
            return Float.compare(b.getTwo().getPriority(), a.getTwo().getPriority());
        }
    }

    static final class LowestFirstComparator<I extends Item> implements Comparator<Pair<I, I>> {
        @Override
        public int compare(Pair<I, I> a, Pair<I, I> b) {
            return Float.compare(a.getTwo().getPriority(), b.getTwo().getPriority());
        }
    }
}
