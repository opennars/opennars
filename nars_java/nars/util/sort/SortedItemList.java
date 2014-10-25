package nars.util.sort;

import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author me
 */
public interface SortedItemList<T> extends Collection<T> {
 
    public T get(int i);
    public T remove(int i);
    public T getFirst();
    public T getLast();
    public Iterator<T> descendingIterator();
    public void setCapacity(int capacity);
    
}
