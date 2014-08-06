package nars.util;

import javolution.util.FastTable;
import nars.entity.Item;


public class PrioritySortedItemList<E extends Item> extends FastTable<E>  {
//public class PrioritySortedItemList<E extends Item> extends GapList<E>  {    
//public class PrioritySortedItemList<E extends Item> extends ArrayList<E>  {    
    
    public PrioritySortedItemList() {
        super();
    }
    
    public PrioritySortedItemList(int capacity) {
        //super(capacity);
        super();
    }
    
    public int positionOf(E o) {
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
    public boolean add(E o) {
        if (size() == 0) {
            return super.add(o);
        }
        else {
            int p = positionOf(o);
            super.add(p, o);
            return true;
        }
    }
    
    /*
    public static class ItemEquality<E extends Item> implements Equality<E> {


        @Override
        public int hashCodeOf(E t) {
            return t.hashCode();
        }

        @Override
        public boolean areEqual(E a, E b) {
            return a == b;
        }

        @Override
        public int compare(final E x, final E y) {
            System.out.println("position of" + x + " " + y);
            
            final short a = x.budget.getPriorityShort();
            final short b = y.budget.getPriorityShort();
            return (a < b) ? -1 : ((a == b) ? 0 : 1);
        }
        
    }
    */
    

    

    
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