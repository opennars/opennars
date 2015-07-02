package nars.util.data.linkedlist;

/** node in a linked list; wraps each Item stored in the bag
 *  TODO the owner field is not necessary but it was helpful during debugging to verify that LevelBag made clear transitions of items from one level to another
 * */
public class DD<E>  {

    public E item;

    /** can be used to identify the current manager of this node, if multiple lists are involved */
    public int owner;

    public DD<E> next;
    public DD<E> prev;

    @Override
    public String toString() {
        return item + "[" +
                (prev!=null ? prev.item : "null")
                + "<<" + owner + ">>" +
                (next!=null ? next.item : "null") +
                ']';
    }

    public int owner() {
        return owner;
    }
}
