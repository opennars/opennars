package nars.util;

import nars.Global;
import nars.budget.Itemized;
import nars.util.data.sorted.SortedIndex;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

//import org.apache.commons.collections.iterators.ReverseListIterator;


public class ArraySortedIndex<E extends Itemized> extends SortedIndex<E> implements Serializable {

    protected int capacity;

    final List<E> list;


    @Override
    public final void forEach(Consumer<? super E> consumer) {
        list.forEach(consumer);
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ArraySortedIndex)) return false;
        ArraySortedIndex o = (ArraySortedIndex) obj;
        return list.equals(o.list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override public final List<E> getList() {
        return list;
    }

    public ArraySortedIndex(int capacity) {
        this(Global.newArrayList(capacity), capacity);
    }
    
    public ArraySortedIndex(List<E> list, int capacity) {
        super();
        this.list = list;
        setCapacity(capacity);
    }

    /**
     * any scalar decomposition function of a budget value
     * can be used
     *
     * TODO parameter score function
     */
    static float score(Itemized b) {

        //MODE 0: priority only
        return b.getPriority();

        //MODE 1:
        //return b.getBudget().summary();

        //MODE 2:
        //this ensures that priority is the most significant ordering factor, even if zero
        /*return (1+b.getPriority())*
                (b.getDurability()*b.getQuality());*/
    }

    @Override
    public boolean isSorted() {
        if (size() < 2) return true;

        Iterator<E> ii = this.iterator();
        float pp = Float.MAX_VALUE;

        while (ii.hasNext()) {
            E c = ii.next();
            float sc = score(c);
            if (sc > pp)
                return false;
            pp = sc;
        }

        return true;
    }

    @Override
    public final void setCapacity(final int capacity) {

        if (this.capacity==capacity) {
            return;
        }

        this.capacity = capacity;

        final List<E> l = this.list;

            int n = l.size();
            //remove elements from end
            for (; n - capacity > 0; n--) {
                l.remove(n-1);
            }


    }


    public final int pos(final E o) {
        return pos(score(o));
    }

    public final int pos(float score) {
        int upperBound = 0;
        int lowerBound = size()-1;

        while (upperBound <= lowerBound) {
            final int mid = (upperBound + lowerBound) /2; // >>> 1;
            final float mp = score(get(mid));

//            System.err.println(upperBound + "(" + score(get(upperBound)) +
//                    " " + mid + "(" + mp + " " + lowerBound + "(" + score(get(lowerBound))  );

            if (mp < score) //midpoint is new lowerBound, so we need to go to the upper half
                lowerBound = mid - 1;
            else if (mp > score) //midpoint is new upperBound so go to lowerBound half
                upperBound = mid + 1;
            else
                return mid; // key found
        }
        return lowerBound;
    }

    @Override
    public final E get(final int i) {
        return list.get(i);
    }

    @Override
    public E insert(final E incoming) {

        E removed = null;

        final int s = size();

        int insertPos;
        if (s == 0) {
            //first element in empty list, insert at beginning
            insertPos = 0;
        } else {

            float incomingScore = score(incoming);

            if (s >= capacity) {

                int lastIndex = size() - 1;
                float lowestScore = score(get(lastIndex));

                if (incomingScore < lowestScore) {
                    //priority too low to join this list, bounce
                    return incoming;
                }

                removed = remove(lastIndex);
            }
            else {
                removed = null;
            }

            insertPos = pos(incomingScore);

            if (insertPos < 0) insertPos = 0;
            else
                insertPos++;

        }

        list.add(insertPos, incoming);

        return removed;
    }

    //TODO use last known position, stored in the Map
    //it could be a better predictor of where it was
    @Override public boolean remove(final Object o) {

        final int s = size();
        if (s == 0) return false;

        //final E o = (E)_o;
        //final Object on = o.name();

//        if (s == 1) {
//            if (get(0)==o) {
//                clear();
//                return true;
//            }
//            return false;
//        }


        //estimated position according to current priority,
        //which if it hasnt changed much has a chance of being
        //close to the index
        int p = pos((E)o);
        if (p >= s) p = s-1;
        if (p < 0)  p = 0;

        if (attemptRemoval(o, p))
            return true;

        int r = 0;
        int maxDist = Math.max(s - p, p);

        boolean phase = false;

        //scan in an expanding radius around the point
        do {

            phase = !phase;

            if (phase)
                r++;

            final int u;
            if (phase) {
                u = p + r;
                if (u >= s) continue;
            }
            else {
                u = p - r;
                if (u < 0) continue;
            }

            if (attemptRemoval(o, u))
                return true;

        } while ( r <= maxDist );




//        //try exhaustive removal as a final option
//        if (list.remove(o)) {
//            return true;
//        }

        String err = this + "(" + capacity + ") missing for remove: " + o + ", p=" + p + " size=" + s;
        throw new RuntimeException(err);
        //return false;
    }

    private final boolean attemptRemoval(final Object o, /*final Object oName, */final int i) {
        final List<E> l = this.list;
        if (o == l.get( i ) /*|| (r.name().equals(oName))*/) {
            return l.remove(i)!=null; //shouldnt actually ever return false here
        }
        return false;
    }


    @Override
    public final int capacity() {
        return capacity;
    }

    public final int available() {
        return capacity - size();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public final Iterator<E> descendingIterator() {
        //return new ReverseListIterator(list);
        throw new RuntimeException("unimpl yet");
    }

    @Override public final E remove(int i) {
        return list.remove(i);
    }

    @Override
    public final int size() {
        return list.size();
    }

    @Override public final boolean isEmpty() {
        return list.isEmpty();
    }

    /** this is a potentially very slow O(N) iteration,
      * shouldnt be any reason to use this */
    @Override public final boolean contains(Object o) {
        return list.contains(o);
    }

    /** if possible, use the forEach visitor which wont
     * incur the cost of allocating an iterator */
    @Override public final Iterator<E> iterator() {
        return list.iterator();
    }

    @Override public final void clear() {
        list.clear();
    }

    @Override public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported yet.");
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
