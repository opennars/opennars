package nars.budget;

import com.gs.collections.impl.map.mutable.UnifiedMap;
import nars.Global;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
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

    final static Comparator<Item> highestFirst = new HighestFirstComparator();
    final static Comparator<Item> lowestFirst = new LowestFirstComparator();


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
        return lowestFirstKeyValues().get(0);
    }

    public I highest() {
        if (buffer.isEmpty()) return null;
        return highestFirstKeyValues().get(0);
    }


    private List<I> lowestFirstKeyValues() {
        return lowestFirstKeyValues(null);
    }
    private List<I> highestFirstKeyValues() {
        return highestFirstKeyValues(null);
    }

    private List<I> lowestFirstKeyValues(@Nullable List<I> result) {
        return sortedKeyValues(lowestFirst, result);
    }

    private List<I> highestFirstKeyValues(@Nullable List<I> result) {
        return sortedKeyValues(highestFirst, result);
    }

    private List<I> sortedKeyValues(Comparator<Item> c, List<I> result) {
        if (result == null)
            result = Global.newArrayList(buffer.size());
        else {
            result.clear();
        }

        result.addAll(buffer.values());
        result.sort(c);

        return result;
    }

    /** iterates the items highest first */
    public Iterator<I> iterateHighestFirst() {
        return highestFirstKeyValues().iterator();
    }

    /** iterates the items highest first */
    public Iterator<I> iterateHighestFirst(List<I> temporary) {
        return highestFirstKeyValues(temporary).iterator();
    }

    public Iterator<I> iterateLowestFirst() {
        return lowestFirstKeyValues().iterator();
    }

    public int removeLowest(final int n, @Nullable List<I> temporary) {
        final int s = buffer.size();
        if (s <= n) {
            buffer.clear();
            return s;
        }

        final List<I> lf = lowestFirstKeyValues(temporary);

        int r;
        for (r = 0; r < n; r++) {
            buffer.remove( lf.get(r) );
        }

        temporary.clear();

        return r;
    }

    public void addAll(Collection<I> x) {
        for (I i : x)
            add(i);
    }

    /** if size() capacity, remove lowest elements until size() is at capacity
     * @return how many removed
     * */
    public int limit(int capacity, Consumer<I> onRemoved, @Nullable List<I> temporary) {

        int numToRemove = size() - capacity;

        if (numToRemove > 0)
            return removeLowest(numToRemove, temporary);

        return 0;
    }


    @Override
    public String toString() {
        return buffer.toString();
    }


    static final class HighestFirstComparator<I extends Item> implements Comparator<I> {
        @Override public final int compare(final I a, final I b) {
            return Float.compare(b.getPriority(), a.getPriority());
        }
    }

    static final class LowestFirstComparator<I extends Item> implements Comparator<I> {
        @Override public final int compare(final I a, final I b) {
            return Float.compare(a.getPriority(), b.getPriority());
        }
    }
}
