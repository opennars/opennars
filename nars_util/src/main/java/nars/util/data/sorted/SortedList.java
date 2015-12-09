package nars.util.data.sorted;

//package net.sourceforge.nite.other;

import nars.util.data.list.FasterList;

import java.util.Comparator;

/**
 * <p>
 * This class implements a sorted list. It is constructed with a comparator that
 * can compare two objects and sorted objects accordingly. When you add an object
 * to the list, it is inserted in the correct place. Object that are equal
 * according to the comparator, will be in the list in the order that they were
 * added to this list. Add only objects that the comparator can compare.</p>
 */
public class SortedList<E extends Comparable> extends FasterList<E> {

    //private static final Comparator comparator = defaultComparator;

    static final Comparator<Comparable> defaultComparator = Comparable::compareTo;

    private boolean allowDuplicate = false;

//    /**
//     * <p>
//     * Constructs a new sorted list. The objects in the list will be sorted
//     * according to the specified comparator.</p>
//     *
//     * @param c a comparator
//     */
//    public SortedList(Comparator<E> c) {
//        this.comparator = c;
//    }

    public SortedList() {
    }

    public SortedList(int capacity) {
        super(capacity);
    }

    public SortedList(E singleton) {
        super(singleton);
    }

    /** uses array directly */
    @SafeVarargs
    public SortedList(E... x) {
        super(x.length);
        for (E e : x)
            add(e); //must add them sequentially to remove duplicates and sort
    }

    public void setAllowDuplicate(boolean allowDuplicate) {
        this.allowDuplicate = allowDuplicate;
    }


    /**
     * <p>
     * Adds an object to the list. The object will be inserted in the correct
     * place so that the objects in the list are sorted. When the list already
     * contains objects that are equal according to the comparator, the new
     * object will be inserted immediately after these other objects.</p>
     *
     * @param o the object to be added
     */
    @Override
    public final boolean add(E o) {
        int low = 0;


        int s = size();
        if (s == 0)
            return super.add(o);


        //binary search
        int high = s - 1;

        boolean allowDuplicate = this.allowDuplicate;

        Comparator cmpr = defaultComparator;

        while (low <= high) {
            int mid = (low + high) / 2;
            E midVal = get(mid);

            int cmp = cmpr.compare(midVal, o);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                // key found, insert after it
                if (!allowDuplicate)
                    return false;
                super.add(mid, o);
                return true;
            }
        }

        if (low == s) {
            super.add(o); //HACK for using FastList
        }
        else {
            super.add(low, o);
        }

        return true;
    }

//    @Override
//    public Object clone() {
//        return super.clone(); //To change body of generated methods, choose Tools | Templates.
//    }
}
