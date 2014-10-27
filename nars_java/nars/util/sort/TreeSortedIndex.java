/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util.sort;

import java.util.logging.Level;
import java.util.logging.Logger;
import nars.entity.Item;
import nars.entity.Item.ItemPriorityComparator;

/**
 *
 * @author me
 */
public class TreeSortedIndex<E extends Item> extends IndexedTreeSet<E> implements SortedIndex<E> {
    
    private int capacity;
    
    public TreeSortedIndex(int capacity) {
        super(new ItemPriorityComparator());

        setCapacity(capacity);
    }

    public TreeSortedIndex() {
        this(1);
    }
    
    

    public float min() {
        if (isEmpty()) return 0;
        return first().getPriority();
    }
    public float max() {
        if (isEmpty()) return 0;
        return last().getPriority();
    }

    @Override
    public E get(int i) {
        return exact(i);
    }

    @Override
    public boolean remove(Object o) {
        System.out.println("remove: " + o + " " + size());
        boolean b = super.remove(o); //To change body of generated methods, choose Tools | Templates.
        System.out.println("  : " + b + " " + size());
        return b;
    }
    
    
    
    
    
    @Override
    public E remove(int index) {        
        
        E e = get(index);
        
        System.out.println(min() + ".."  + max() + " - " + index + "->" + e);

        if (e!=null) {
            remove(e);            
        }
        
        return e;
    }

        

    @Override
    public E getFirst() {
        if (isEmpty()) return null;
        return first();
    }

    @Override
    public E getLast() {
        if (isEmpty()) return null;
        return last();
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    
    
}
