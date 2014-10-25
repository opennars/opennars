/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util.sort;

import nars.entity.Item;

/**
 *
 * @author me
 */
public class RedBlackSortedItemList<E extends Item> extends IndexedTreeSet<E> implements SortedItemList<E> {

    public RedBlackSortedItemList() {
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
    public E get(int index) {
        return exact(index);
    }


    @Override
    public E remove(int index) {
        E e = get(index);
        if (e!=null) {
            remove(e);
        }
        return e;
    }

    
}
