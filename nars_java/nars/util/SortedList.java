package nars.util;

//package net.sourceforge.nite.util;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * <p>This class implements a sorted list. It is constructed with a comparator
 * that can compare two objects and sort objects accordingly. When you add an
 * object to the list, it is inserted in the correct place. Object that are
 * equal according to the comparator, will be in the list in the order that
 * they were added to this list. Add only objects that the comparator can
 * compare.</p>
 */
public class SortedList<E> extends ArrayList<E> {
    
    private final Comparator<E> comparator;

    /**
     * <p>Constructs a new sorted list. The objects in the list will be sorted
     * according to the specified comparator.</p>
     *
     * @param c a comparator
     */
    public SortedList(Comparator<E> c) {
            this.comparator = c;
    }

 


    /**
     * <p>Adds an object to the list. The object will be inserted in the correct
     * place so that the objects in the list are sorted. When the list already
     * contains objects that are equal according to the comparator, the new
     * object will be inserted immediately after these other objects.</p>
     *
     * @param o the object to be added
     */
    @Override
    public boolean add(E o) {        
                
        if (size() > 0)  {
            
            //binary search
            int low = 0;
            int high = size()-1;

            while (low <= high) {
                int mid = (low + high) >>> 1;
                E midVal = get(mid);
                int cmp = comparator.compare(midVal,o);

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

    @Override
    public Object clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
}