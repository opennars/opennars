package nars.util.sort;

import nars.Global;
import nars.budget.Itemized;
import nars.util.data.sorted.SortedIndex;

import java.io.Serializable;
import java.util.*;

//public class PrioritySortedItemList<E extends Item> extends GapList<E>  {    
//public class PrioritySortedItemList<E extends Item> extends ArrayList<E>  {    
//abstract public class SortedItemList<E> extends FastTable<E> {
public class ArraySortedIndex<E extends Itemized>  implements SortedIndex<E>, Serializable {

    int capacity = Integer.MAX_VALUE;

    public final List<E> list;

    final static private Comparator<Itemized> priorityComparator = new Comparator<Itemized>() {
        @Override public int compare(final Itemized a, final Itemized b) {
            return Float.compare(a.getPriority(), b.getPriority());
        }
    };

    @Override
    public List<E> getList() {
        return list;
    }

    public static <E> List<E> bestList(int capacity) {
        return //new ArrayList(capacity);

                Global.newArrayList(capacity); // : new FastSortedTable();
    }

    public ArraySortedIndex(int capacity) {
        this(capacity, 
                Global.THREADS == 1 ? bestList(capacity) :
                        Collections.synchronizedList(bestList(capacity))
        );
    }
    
    public ArraySortedIndex(int capacity, List<E> list) {
        super();
        setCapacity(capacity);
        this.list = list;
    }

    @Override
    public boolean isSorted() {
        Iterator<E> ii = this.iterator();
        float pp = Float.MAX_VALUE;

        while (ii.hasNext()) {
            E c = ii.next();
            if (c.getPriority() > pp)
                return false;
            pp = c.getPriority();
        }

        return true;
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public final int positionOf(final E o) {
        int low = 0;
        int high = size()-1;
        final float op = o.getPriority();

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final float mp = get(mid).getPriority();

            if (mp > op)
                low = mid + 1;
            else if (mp < op)
                high = mid - 1;
            else
                return mid; // key found
        }
        return low;
        //return -(low + 1);  // key not found
    }

//    public final int positionOf(final E o) {
//        final float y = o.getPriority();
////        if ((y < 0) || (y > 1.0f)) {
////            System.err.println("Invalid priority value: " + o);
////            System.exit(1);
////        }
//
//        final int s = size();
//        if (s == 0) {
//            return 0;
//        }
//
//
//        //binary search
//        int low = 0;
//        int high = s;
//
//        while (low < high) {
//            int mid = (low + high) / 2;
//            /*if ((mid == low) && (mid == high))
//                break;*/
//
//            final float x = get(mid).getPriority();
//
//            if (x < y) {
//                low = mid - 1;
//                if (low < 0) low = 0;
//            } else if (x == y) {
//                return mid;
//            } else /*if (x > y)*/ {
//                high = mid + 1;
//                if (high >= s)   high = s-1;
//            }
//
//        }
//        return low;
//    }

    public int validStorePosition(final int i) {
        final int size = size();
        if (i >= size) return size-1;
        if (i < 0) return 0;
        return i;
    }

//    public int validInsertionPosition(final int i) {
//        final int size = size();
//        if (i > size) return size; //allow i-size for inserting at the end
//        if (i < 0) return 0;
//        return i;
//    }
    
    @Override
    final public E get(final int i) {

        //final int s = list.size();

        //if (s == 0) return null;

        /*if (i >= s)
            i = s - 1;
        if (i < 0)
            i = 0;*/

        return list.get(i);
    }

   
    @Override
    public E insert(final E o) {

        E removed = o;

        final int s = size();

        if (s == 0) {
            list.add(o);
            return null;
        } else {

            int insertPos = positionOf(o);

            if (s >= capacity) {

                if (insertPos == 0) {
                    //priority too low to join this list
                    return o;
                }

                removed = remove(0);
                if (insertPos > 0) insertPos--;
            }
            else {
                removed = null;
            }


            list.add(insertPos, o);

        }


        return removed;
    }

    @Override
    public E getFirst() {
//        if (isEmpty()) {
//            return null;
//        }
        return get(0);
    }

    @Override
    public E getLast() {
//        if (isEmpty()) {
//            return null;
//        }
        return get(size() - 1);
    }

    public int capacity() {
        return capacity;
    }

    public int available() {
        return capacity() - size();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return list.iterator(); //stored in descending order
    }

//    /**
//     * can be handled in subclasses
//     */
//    protected void reject(E removeFirst) {
//    }

    
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

        final int s = size();
        if (s == 0) return false;

        final E o = (E)_o;
        final Object on = o.name();
        
        if (s == 1) {
            if (get(0).name().equals(on)) {
                clear();
                return true;
            }
            return false;
        }
        
        
        //estimated position according to current priority
        final int p = validStorePosition(positionOf(o));
        

        int i = p, j = p - 1;
        int finished = (j == -1) ? 1 : 0;
        do {
            
            if (i < s) {
                if (attemptRemoval(o, /*on, */i))
                    return true;
                i++;
                if (i == s)
                    finished++;
            }

            if (j >= 0) {
                if (attemptRemoval(o, /*on, */j))
                    return true;
                j--;
                if (j < 0)
                    finished++;
            }

        } while ( finished<2 );


        //try exhaustive removal as a final option
        if (list.remove(_o)) {
            return true;
        }
        String err = this + "(" + capacity + ") missing for remove: " + o + ", p=" + p + " size=" + s;
        throw new RuntimeException(err);
    }

    private final boolean attemptRemoval(final Object o, /*final Object oName, */final int i) {
        final E r = list.get( i );
        if ((o == r) /*|| (r.name().equals(oName))*/) {
            if (list.remove(i)!=null)
                return true;
        }
        return false;
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
