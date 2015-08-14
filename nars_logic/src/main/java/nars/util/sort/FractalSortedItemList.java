package nars.util.sort;

import javolution.util.FastSortedTable;
import javolution.util.function.Equality;
import nars.budget.Item;
import nars.util.data.sorted.SortedIndex;

import java.util.List;

/**
 * EXPERIMENTAL - NOT READY YET - may have a bug / leak
 * @param <E> 
 */
public class FractalSortedItemList<E extends Item> extends FastSortedTable<E> implements SortedIndex<E> {

    public static class ItemEquality<E extends Item> implements Equality<E> {

        @Override
        public int compare(final E a, final E b) {
            if (areEqual(a, b)) return 0;
            
            float ap = a.getPriority();
            float bp = b.getPriority();
            if (ap < bp) return -1;
            else if (bp < ap) return 1;
            return -1;
        }

        @Override
        public int hashCodeOf(E t) {
            return t.name().hashCode();
        }

        @Override
        public boolean areEqual(E t, E t1) {
            if (t == t1) return true;
            return t.name().equals(t1.name());
        }

    }

    @Override
    public List<E> getList() {
        return this;
    }

    @Override
    public boolean isSorted() {
        throw new RuntimeException("Not implemented yet");
    }

    int capacity;

    public FractalSortedItemList() {
        this(1);
    }

    public FractalSortedItemList(int capacity) {
        super(new ItemEquality());
        this.capacity = capacity;
    }


    @Override
    public E insert(E i) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

//    @Override
//    public boolean add(final E o) {
//        if (isEmpty()) {
//            return super.add(o);
//        } else {
//            if (size() == capacity) {
//
//                if (positionOf(o) == 0) {
//                    //priority too low to join this list
//                    return false;
//                }
//
//                reject(removeFirst()); //maybe should be last
//            }
//            super.add(positionOf(o), o);
//            return true;
//        }
//    }

    
    
//    @Override
//    public boolean remove(Object obj) {        
//        int i = positionOf( ((E)obj) );
//        if ((i >= size()) || (i == -1))
//            return false;
//        E r = remove(i);
//        if (r!=null) {
//            if (r.name().equals( ((E)obj).name()) )
//                return true;
//            else {
//                System.out.println( ((E)obj).name() + " =?= " + r.name() );
//                throw new RuntimeException("inconsistent bag");
//            }
//        }
//        return false;
//    }

    /*
    
    @Override
    public final int positionOf(final E o) {
        final float y = o.budget.getPriority();
        final int s = size();
        if (s > 0)  {

            //binary search
            int low = 0;
            int high = s-1;

            while (low <= high) {
                int mid = (low + high) >>> 1;

                E midVal = get(mid);

                final float x = midVal.budget.getPriority();

                if (x < y) low = mid + 1;
                else if (x == y) return mid;
                else if (x > y) high = mid - 1;                    

            }
            return low;
        }
        else {
            return -1;
        }
    }
      */
    /*
    @Override
    public E getLast() {
        int s = size();
        if (s == 0) return null;
        return get(size()-1);
    }

    @Override
    public E getFirst() {
        int s = size();
        if (s == 0) return null;
        return get(0);
    }
*/
    
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
