package nars.util.data.sorted;


import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Stores items with highest priority at index = 0, lowest = size()-1
 */
abstract public class SortedIndex<T> implements Collection<T>, Serializable {

    @Override
    public boolean add(T t) {
        throw new RuntimeException("Use insert method which can return a displaced object");
    }

    abstract public T insert(T i);
    abstract public T get(int i);
    abstract public T remove(int i);


    final public T getLast() {  return get(size()-1);    }
    final public T getFirst() { return get(0); }

    abstract public Iterator<T> descendingIterator();
    abstract public void setCapacity(int capacity);

    abstract public List<T> getList();
    
//    public void reportPriority() {
//        for (T t : this) {
//            String s;
//            /*if (t instanceof Item)
//                s = Float.toString(((Item)t).getPriority());
//            else*/
//                s = t.toString();
//            System.out.print(s + ',');
//        }
//        System.out.println();
//    }


    abstract public boolean isSorted();
}
