package nars.util.bag;

/** node in a linked list; wraps each Item stored in the bag */
public class DD<E extends Object> {
    public E item;
    int level;
    public DD<E> next;
    public DD<E> prev;
}
