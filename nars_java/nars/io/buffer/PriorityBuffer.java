package nars.io.buffer;

import java.util.Comparator;
import nars.entity.Item;
import nars.util.SortedItemList;

/**
 * Priority-sorted input buffer.
 * 
 * Note: The "head" of java.util.PriorityQueue is the item with the least value
 * according to the comparator
 */
public class PriorityBuffer<B> extends SortedItemList<B> implements Buffer<B> {
    
    public static class ItemPriorityComparator<I extends Item> implements Comparator<I> {
        @Override public int compare(I a, I b) {
            //TODO check ordering
            return Short.compare( a.budget.getPriorityShort(), b.budget.getPriorityShort() );
        }        
    }
    
    public PriorityBuffer(Comparator<B> comparator, int capacity) {
        super(comparator, capacity);
    }

    
    
}
