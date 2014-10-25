package nars.util.sort;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author me
 */
public interface SortedItemList<T> extends List<T> {
 
    public T getFirst();
    public T getLast();
    public Iterator<T> descendingIterator();
    
}
