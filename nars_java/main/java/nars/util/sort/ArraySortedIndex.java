package nars.util.sort;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javolution.util.FastSortedTable;
import nars.core.Parameters;
import nars.entity.Item;

//public class PrioritySortedItemList<E extends Item> extends GapList<E>  {    
//public class PrioritySortedItemList<E extends Item> extends ArrayList<E>  {    
//abstract public class SortedItemList<E> extends FastTable<E> {
public class ArraySortedIndex<E extends Item>  implements SortedIndex<E> {

    int capacity = Integer.MAX_VALUE;
    private List<E> reverse;

    public final List<E> list;
    
    public static <E> List<E> bestList(int capacity) {
        return capacity < 50 ? new ArrayList() : new FastSortedTable();
    }

    public ArraySortedIndex(int capacity) {
        this(capacity, 
                Parameters.THREADS == 1 ? bestList(capacity) :
                        Collections.synchronizedList(bestList(capacity))
        );
    }
    
    public ArraySortedIndex(int capacity, List<E> list) {
        super();
        setCapacity(1);
        this.list = list;
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public final int positionOf(final E o) {
        final float y = o.budget.getPriority();
        if ((y < 0) || (y > 1.0f)) {
            System.err.println("Invalid priority value: " + o);
            System.exit(1);
        }
        
        final int s = size();
        if (s > 0) {

            //binary search
            int low = 0;
            int high = s - 1;

            while (low <= high) {
                int mid = (low + high) >>> 1;

                E midVal = get(mid);

                final float x = midVal.budget.getPriority();

                if (x < y) {
                    low = mid + 1;
                } else if (x == y) {
                    return mid;
                } else if (x > y) {
                    high = mid - 1;
                }

            }
            return low;
        } else {
            return 0;
        }
    }

    public int validPosition(int i) {
        if (i > size()) i = size();
        if (i < 0) i = 0;
        return i;
    }
    
    @Override
    public E get(int i) {
        return list.get(i);
    }

   
    @Override
    public boolean add(final E o) {
                
        if (isEmpty()) {
            return list.add(o);
        } else {
            if (size() >= capacity) {

                if (positionOf(o) == 0) {
                    //priority too low to join this list
                    return false;
                }

                reject(remove(0));
            }
            
            list.add(validPosition(positionOf(o)), o);
            return true;
        }
    }

    @Override
    public E getFirst() {
        if (isEmpty()) {
            return null;
        }
        return get(0);
    }

    @Override
    public E getLast() {
        if (isEmpty()) {
            return null;
        }
        return get(size() - 1);
    }

    public int capacity() {
        return capacity;
    }

    public int available() {
        return capacity() - size();
    }

    @Override
    public Iterator<E> descendingIterator() {
        if (reverse == null) {
            reverse = Lists.reverse(list);
        }
        return reverse.iterator();
    }

    /**
     * can be handled in subclasses
     */
    protected void reject(E removeFirst) {
    }

    
    @Override public E remove(int i) {
        return list.remove(i);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean remove(final Object _o) {
                
        if (size() == 0) return false;

        E o = (E)_o;
        final Object on = o.name();
        
        if (size() == 1) {
            if (get(0).name().equals(on)) {
                list.remove(0);
                return true;
            }
            return false;
        }
        
        
        //estimated position according to current priority
        int p = validPosition(positionOf( o )); 
        
        int s = size();
        
        int i = p, j = p - 1;
        boolean finishedUp = false, finishedDown = false;
        do {
            
            if (i < s) {
                E r = list.get( i );
                if ((o == r) || (r.name().equals(on))) {
                    if (list.remove(i)!=null)
                        return true;
                    else
                        continue;
                }
                i++;                
            }
            else
                finishedUp = true;

            if (j >= 0) {
                E r = list.get( j );
                if ((o == r) || (r.name().equals(on))) {
                    if (list.remove(j)!=null)
                        return true;
                    else 
                        continue;
                }
                j--;
            }
            else
                finishedDown = true;
            
        } while ( (!finishedUp) || (!finishedDown) );
                
        throw new RuntimeException(this + "(" + capacity + ") missing for remove: " + o + ", p=" + p + " size=" + s);
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
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
