package nars.util.data.sorted;


import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Stores items with highest priority at index = 0,
 * lowest = size()-1
 */
public abstract class SortedIndex<T> implements Collection<T>, Serializable {

    @Override
    public boolean add(T t) {
        throw new RuntimeException("Use insert method which can return a displaced object");
    }

    public abstract T insert(T i);

    /** numeric access */
    public abstract T get(int i);
    public abstract T remove(int i);


    public final T getLast() {  return get(size()-1);    }
    public final T getFirst() { return get(0); }

    public abstract Iterator<T> descendingIterator();
    public abstract void setCapacity(int capacity);

    public abstract List<T> getList();
    
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


    public abstract boolean isSorted();

    public abstract int capacity();

    public void print(PrintStream out) {
        forEach(out::println);
    }
}
