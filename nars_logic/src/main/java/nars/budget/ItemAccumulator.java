package nars.budget;

import nars.bag.BagBudget;
import nars.bag.impl.ArrayBag;
import nars.util.ArraySortedIndex;

import java.io.PrintStream;
import java.util.function.Consumer;

/** priority queue which merges equal tasks and accumulates their budget.
 * stores the highest item in the last position, and lowest item in the first.
 * one will generally only need to use these methods:
 *      --limit(n) - until size <= n: remove lowest items
 *      --next(n, consumer(n)) - visit N highest items
 *
 * */
public class ItemAccumulator<V extends Budgeted> extends ArrayBag<V> {


    public ItemAccumulator(int capacity) {
        super(new ArraySortedIndex(capacity));
        mergePlus();
    }

    @Override
    public final BagBudget<V> peekNext() {
        return items.getFirst();
    }

    @Override
    public BagBudget<V> pop() {
        return removeHighest();
    }

    @Override
    public void update(BagBudget v, Consumer<BagBudget> updater) {
        super.update(v, updater);
        ((Budgeted)v.get()).getBudget().set(v); //TODO replace instance's budget on insert so this copy isnt necessary
    }

    @Override
    public final boolean contains(V t) {
        return index.containsKey(t);
    }


    public void print(PrintStream out) {
        items.print(out);
    }


//    static final class HighestFirstComparator implements Comparator<Budgeted>, Serializable {
//        @Override public final int compare(final Budgeted a, final Budgeted b) {
//            return Float.compare(b.getPriority(), a.getPriority());
//        }
//    }
//
//    static final class LowestFirstComparator implements Comparator<Budgeted>, Serializable {
//        @Override public final int compare(final Budgeted a, final Budgeted b) {
//            return Float.compare(a.getPriority(), b.getPriority());
//        }
//    }

//    //@Override
//    final public Itemized<K> apply(final Itemized<K> t, Itemized<K> accumulated) {
//
//        if (t.getBudget().isDeleted())
//            return accumulated;
//
//        if (accumulated!=null) {
//            if (accumulated.getBudget().isDeleted()) {
//                return t;
//            }
//
//            merge.value(accumulated.getBudget(), t.getBudget());
//            return accumulated;
//        }
//        else
//            return t;
//    }



//
//    public Stream<I> stream() {
//        return items.keySet().stream();
//    }
//    public final boolean add(final I t0) {
//        items.compute(t0, this /*updater*/);
//        return true;
//    }
//    public final <J extends I> J removeHighest() {
//        final I i = highest();
//        if (i == null) return null;
//        items.remove(i);
//        return (J)i;
//    }
//    public final <J extends I> J removeLowest() {
//        final I i = lowest();
//        if (i == null) return null;
//        items.remove(i);
//        return (J)i;
//    }
//
//    public final I lowest() {
//        if (items.isEmpty()) return null;
//        return lowestFirstKeyValues().get(0);
//    }
//
//    public final I highest() {
//        if (items.isEmpty()) return null;
//        return highestFirstKeyValues().get(0);
//    }
//
//    private List<I> lowestFirstKeyValues() {
//        return lowestFirstKeyValues(null);
//    }
//    private List<I> highestFirstKeyValues() {
//        return highestFirstKeyValues(null);
//    }
//
//    private List<I> lowestFirstKeyValues(@Nullable List<I> result) {
//        return sortedKeyValues(lowestFirst, result);
//    }
//
//    private List<I> highestFirstKeyValues(@Nullable List<I> result) {
//        return sortedKeyValues(highestFirst, result);
//    }
//
//    private List<I> sortedKeyValues(Comparator<Budgeted> c, List<I> result) {
//        if (result == null)
//            result = Global.newArrayList(items.size());
//        else {
//            result.clear();
//        }
//
//        result.addAll(items.keySet());
//        result.sort(c);
//
//        return result;
//    }
//
//    /** iterates the items highest first */
//    public Iterator<I> iterateHighestFirst() {
//        return highestFirstKeyValues().iterator();
//    }
//
//    /** iterates the items highest first */
//    public Iterator<I> iterateHighestFirst(List<I> temporary) {
//        return highestFirstKeyValues(temporary).iterator();
//    }
//
//    public Iterator<I> iterateLowestFirst() {
//        return lowestFirstKeyValues().iterator();
//    }



//    public int update(final int targetSize, List<I> sortedResult) {
//
//        sortedResult.clear();
//
//        final int s = items.size();
//
//        lowestFirstKeyValues(sortedResult);
//
//        if (s <= targetSize) {
//            //size is small enough, nothing is discarded. everything retained
//            //sortedResult has been sorted
//            items.clear();
//            return s;
//        }
//
//        final int toDiscard = s - targetSize;
//
//        int r;
//        for (r = 0; r < toDiscard; r++) {
//            items.remove( sortedResult.get(r) );
//        }
//
//        return s - r;
//    }
//
//    public void addAll(final Iterable<I> x) {
//        x.forEach( this::add );
//    }

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

//
//    /** iterates in no-specific order */
//    public final void forEach(Consumer<I> recv) {
//        items.forEach((k,v) -> recv.accept(k));
//        //items.forEachKey(recv::accept);
//    }
//
//    public final void limit(int capacity) {
//        while (size() > capacity) {
//            removeLowest();
//        }
//    }
//
//    public final  void next(int rate, Consumer<Task> recv) {
//        int sent = 0;
//        Task next;
//        while ((sent < rate) && ((next = removeHighest())!=null)) {
//            if (!next.isDeleted()) {
//                recv.accept(next);
//                sent++;
//            }
//        }
//    }

}
