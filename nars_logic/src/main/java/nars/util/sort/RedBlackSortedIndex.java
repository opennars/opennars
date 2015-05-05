/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util.sort;

import nars.nal.Item;
import nars.nal.Item.ItemPriorityComparator;
import nars.util.data.sorted.IndexedTreeSet;
import nars.util.data.sorted.SortedIndex;

/**
 * EXPERIMENTAL not ready yet
 * @author me
 */
public class RedBlackSortedIndex<E extends Item> extends IndexedTreeSet<E> implements SortedIndex<E> {
    private int capacity;

    public RedBlackSortedIndex() {
        super(new ItemPriorityComparator());
        setCapacity(1);
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    
    @Override
    public E getFirst() {
        return first();
    }

    @Override
    public E getLast() {
        return last();
    }

    @Override
    public E insert(E i) {
        throw new RuntimeException("Not implemented yet");
    }


    @Override
    public E get(int index) {
        return exact(index);
    }

    @Override
    public boolean add(E e) {
        if (capacity == 500)
            System.out.println("+ " + e + " " + size());
        boolean b = super.add(e); //To change body of generated methods, choose Tools | Templates.
        if (capacity == 500)
            System.out.println("  --> "+  size());
        return b;
    }

    @Override
    public boolean remove(Object o) {
        if (capacity == 500)
            System.out.println("- " + o + " "+  size() + " : " + contains(o));
        
        boolean b = super.remove(o); //To change body of generated methods, choose Tools | Templates.
        
        if (capacity == 500)
            System.out.println(b + "  --> "+  size());
        
        return b;
    }

    
    

    @Override
    public E remove(int index) {        
        E e = get(index);
        if (capacity == 500)
            System.out.println("- " + index + " "+  size() + " : " + e);
        if (e!=null) {
            remove(e);
        }
        if (capacity == 500)
            System.out.println("  --> "+  size());
        return e;
    }

    
}
