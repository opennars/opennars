package nars.util;

import java.util.Comparator;
import javolution.util.FastTable;


//public class PrioritySortedItemList<E extends Item> extends GapList<E>  {    
//public class PrioritySortedItemList<E extends Item> extends ArrayList<E>  {    
abstract public class SortedItemList<E> extends FastTable<E>  {
    private Comparator<E> comparator = null;
    int capacity = Integer.MAX_VALUE;
    

    
    public SortedItemList(Comparator<E> c) { 
        this(c, Integer.MAX_VALUE);
    }
    public SortedItemList(Comparator<E> c, int capacity) { 
        super();
        this.capacity = capacity;        
    }
    
    
    
//    public PrioritySortedItemList(int capacity) {
//        //super(capacity);
//        super();
//    }
    
    public int positionOf(final E o) {
        final E y = o;
        final int s = size();
        if (s > 0)  {

            //binary search
            int low = 0;
            int high = s-1;

            while (low <= high) {
                int mid = (low + high) >>> 1;

                E midVal = get(mid);

                int cmp = comparator.compare(midVal, y);

                if (cmp < 0)
                    low = mid + 1;
                else if (cmp > 0)
                    high = mid - 1;
                else {
                    // key found, insert after it
                    return mid;
                }
            }
            return low;
        }
        else {
            return 0;
        }
    }
    

    @Override
    public boolean add(final E o) {
        if (isEmpty()) {
            return super.add(o);
        }
        else {
            if (size() == capacity) {
                
                if (positionOf(o)==0) {
                    //priority too low to join this list
                    return false;
                }
                
                reject(removeFirst()); //maybe should be last
            }
            super.add(positionOf(o), o);
            return true;
        }
    }

    public int capacity() {
        return capacity;
    }

    
    public int available() {
        return capacity() - size();
    }
    
    
    /** can be handled in subclasses */
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