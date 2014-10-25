package nars.util.sort;

import java.util.Collection;
import java.util.Iterator;
import nars.entity.Item;

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
    
    default public void reportPriority() {
        for (T t : this) {
            String s;
            if (t instanceof Item)
                s = Float.toString(((Item)t).getPriority());
            else
                s = t.toString();
            System.out.print(s + ",");
        }
        System.out.println();
    }
    
}
