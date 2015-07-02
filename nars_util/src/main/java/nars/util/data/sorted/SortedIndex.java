package nars.util.data.sorted;


import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author me
 */
public interface SortedIndex<T> extends Collection<T> {

    @Override
    default boolean add(T t) {
        throw new RuntimeException("Use insert method which can return a displaced object");
    }

    public T insert(T i);
    public T get(int i);
    public T remove(int i);
    public T getFirst();
    public T getLast();
    public Iterator<T> descendingIterator();
    public void setCapacity(int capacity);
    
    default public void reportPriority() {
        for (T t : this) {
            String s;
            /*if (t instanceof Item)
                s = Float.toString(((Item)t).getPriority());
            else*/
                s = t.toString();
            System.out.print(s + ',');
        }
        System.out.println();
    }


}
