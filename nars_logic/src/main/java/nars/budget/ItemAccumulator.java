package nars.budget;

import com.gs.collections.api.block.procedure.Procedure2;
import nars.Global;
import nars.task.Task;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/** priority queue which merges equal tasks and accumulates their budget.
 * stores the highest item in the last position, and lowest item in the first.
 * one will generally only need to use these methods:
 *      --limit(n) - until size <= n: remove lowest items
 *      --next(n, consumer(n)) - visit N highest items
 *
 * */
public class ItemAccumulator<I extends Budgeted> implements BiFunction<I,I,I>, Serializable {


    public final Map<I,I> items =


//            new BoundedConcurrentHashMap(
//                        /* capacity */ 1024,
//                        /* concurrency */ 1,
//                        /* key equivalence */
//                    AnyEquivalence.getInstance(Budget.class),
//                    AnyEquivalence.getInstance(Budget.class));

            new ConcurrentHashMap();
            //new UnifiedMap<>();

//    final Comparator<? super I> floatValueComparator = new Comparator<I>() {
//        @Override public final int compare(final I o1, final I o2) {
//            return Float.compare( o1.getPriority(), o2.getPriority() );
//        }
//    };




    final static Comparator highestFirst = new HighestFirstComparator();
    final static Comparator lowestFirst = new LowestFirstComparator();

    /** ex: Bag.max, Bag.plus, Bag.average
     * first budget = target where the data is accumulated
     * second budget = incoming budget
     * */
    public final Procedure2<Budget, Budget> merge;

    //final BiFunction<I,I,I> updater;


    @Override
    final public I  apply(final I t, final I accumulated) {
        if (accumulated!=null) {
            merge.value(accumulated.getBudget(), t.getBudget());
            return accumulated;
        }
        else
            return t;
    }

    public ItemAccumulator(Procedure2<Budget, Budget> merge) {
        super();

        this.merge = merge;
    }


//
//    public Stream<I> stream() {
//        return items.keySet().stream();
//    }

    public void clear() {
        items.clear();
    }




    public final boolean add(final I t0) {
        items.compute(t0, this /*updater*/);
        return true;
    }

    public final int size() {
        return items.size();
    }

    public final boolean isEmpty() {
        return items.isEmpty();
    }

    public final <J extends I> J removeHighest() {
        final I i = highest();
        if (i == null) return null;
        items.remove(i);
        return (J)i;
    }
    public final <J extends I> J removeLowest() {
        final I i = lowest();
        if (i == null) return null;
        items.remove(i);
        return (J)i;
    }

    public final I lowest() {
        if (items.isEmpty()) return null;
        return lowestFirstKeyValues().get(0);
    }

    public final I highest() {
        if (items.isEmpty()) return null;
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

    private List<I> sortedKeyValues(Comparator<Budgeted> c, List<I> result) {
        if (result == null)
            result = Global.newArrayList(items.size());
        else {
            result.clear();
        }

        result.addAll(items.keySet());
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



    public int update(final int targetSize, List<I> sortedResult) {

        sortedResult.clear();

        final int s = items.size();

        lowestFirstKeyValues(sortedResult);

        if (s <= targetSize) {
            //size is small enough, nothing is discarded. everything retained
            //sortedResult has been sorted
            items.clear();
            return s;
        }

        final int toDiscard = s - targetSize;

        int r;
        for (r = 0; r < toDiscard; r++) {
            items.remove( sortedResult.get(r) );
        }

        return s - r;
    }

    public void addAll(final Iterable<I> x) {
        x.forEach( this::add );
    }

//    /** if size() capacity, remove lowest elements until size() is at capacity
//     * @return how many remain
//     * */
//    public int limit(int capacity, Consumer<I> onRemoved, @Nullable List<I> sortedResult) {
//
//        final int numToRemove = size() - capacity;
//
//        if (numToRemove > 0)
//            return update(numToRemove, sortedResult);
//
//        return 0;
//    }

//    public static int limit(ItemAccumulator<Task> acc, int capacity, List<Task> temporary, Memory m) {
//        return acc.limit(capacity, task -> m.remove(task, "Ignored"), temporary);
//    }


    /** iterates in no-specific order */
    public void forEach(Consumer<I> recv) {

        items.forEach((k,v) -> recv.accept(k));
        //items.forEachKey(recv::accept);
    }

    @Override
    public String toString() {
        return items.toString();
    }

    public Set<I> keySet() {
        return items.keySet();
    }

    public void limit(int capacity) {
        while (size() > capacity) {
            removeLowest();
        }
    }

    public void next(int rate, Consumer<Task> recv) {
        int sent = 0;
        Task next;
        while ((sent < rate) && ((next = removeHighest())!=null)) {
            if (!next.isDeleted()) {
                recv.accept(next);
                sent++;
            }
        }
    }


    static final class HighestFirstComparator implements Comparator<Budgeted>, Serializable {
        @Override public final int compare(final Budgeted a, final Budgeted b) {
            return Float.compare(b.getPriority(), a.getPriority());
        }
    }

    static final class LowestFirstComparator implements Comparator<Budgeted>, Serializable {
        @Override public final int compare(final Budgeted a, final Budgeted b) {
            return Float.compare(a.getPriority(), b.getPriority());
        }
    }
}
