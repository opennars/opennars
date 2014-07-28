package nars.util;

import java.util.Comparator;
import nars.entity.Item;


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
