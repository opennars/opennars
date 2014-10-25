package nars.util.sort;

import java.util.Comparator;
import javolution.util.FastCollection;
import javolution.util.FastSortedTable;
import javolution.util.function.Equality;
import nars.entity.Item;
import nars.entity.Item.ItemPriorityComparator;


public class FractalSortedItemList<E extends Item> extends FastSortedTable<E> implements SortedItemList<E> {

    public static class ItemEquality<E extends Item> extends ItemPriorityComparator<E> implements Equality<E> {
        
        @Override public int compare(final E a, final E b) { 
            if (areEqual(a, b)) return 0;
            return super.compare(a, b);
        }
        
        @Override public int hashCodeOf(E t) {
            return t.hashCode();
        }

        @Override public boolean areEqual(E t, E t1) {
            return (t == t1);
        }
        
    }
    
    
    
    int capacity;

    public FractalSortedItemList() {
        this(1);
    }
    
    public FractalSortedItemList(int capacity) {
        super(new ItemEquality<E>());
        this.capacity = capacity;
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    
    @Override
    public E getLast() {        
        return max();
    }
    @Override
    public E getFirst() {
        return min();
    }

    
    
//    @Override
//    public final int positionOf(final E o) {
//        final float y = o.budget.getPriority();
//        final int s = size();
//        if (s > 0) {
//
//            //binary search
//            int low = 0;
//            int high = s - 1;
//
//            while (low <= high) {
//                int mid = (low + high) >>> 1;
//
//                E midVal = get(mid);
//
//                final float x = midVal.budget.getPriority();
//
//                if (x < y) {
//                    low = mid + 1;               
//                } else if (x > y) {
//                    high = mid - 1;
//                } else if (x == y) {
//                    return mid;
//                }
//
//            }
//            return low;
//        } else {
//            return 0;
//        }
//    }

//    public int positionOf(final E o) {
//        final E y = o;
//        final int s = size();
//        if (s > 0) {
//
//            //binary search
//            int low = 0;
//            int high = s - 1;
//
//            while (low <= high) {
//                int mid = (low + high) >>> 1;
//
//                E midVal = get(mid);
//
//                int cmp = comparator.compare(midVal, y);
//
//                if (cmp < 0) {
//                    low = mid + 1;
//                } else if (cmp > 0) {
//                    high = mid - 1;
//                } else {
//                    // key found, insert after it
//                    return mid;
//                }
//            }
//            return low;
//        } else {
//            return 0;
//        }
//    }
//    @Override
//    public final boolean add(final E o) {
//        int s = size();
//        if (s == 0) {
//            return super.add(o);
//        } else {
//            
//            boolean added = addIfAbsent(o);
//            
//            if (added  &&  ((s+1) >= capacity)) {
//                //remove lowest
//                reject(removeFirst());
//                return true;
//            }
//                        
//            return added;
//            
//        }
//    }

    
    public final int capacity() {
        return capacity;
    }

    public final int available() {
        return capacity() - size();
    }

    /**
     * can be handled in subclasses
     */
    protected void reject(E removeFirst) {
    }

}
/*
 public class PrioritySortedItemList<E extends Item> extends SortedList<E> {

 public PrioritySortedItemList() {
 super(null);
 }

 @Override
 public boolean add(final E o) {
        
 final int y = o.budget.getPriorityShort();
        
 if (size() > 0)  {
            
 //binary search
 int low = 0;
 int high = size()-1;

 while (low <= high) {
 int mid = (low + high) >>> 1;
 E midVal = get(mid);
                
 final int x = midVal.budget.getPriorityShort();
 int cmp = (x < y) ? -1 : ((x == y) ? 0 : 1);                   

 if (cmp < 0)
 low = mid + 1;
 else if (cmp > 0)
 high = mid - 1;
 else {
 // key found, insert after it
 super.add(mid, o);
 return true;
 }
 }
 super.add(low, o);
 return true;
 }
 else {
 super.add(0,o);
 return true;
 }
 }

    
    
    
 }
 */
