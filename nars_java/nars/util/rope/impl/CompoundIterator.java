
package nars.util.rope.impl;

import java.util.Collections;
import java.util.Iterator;

/**
 * TODO write unit test for this, could be useful in several code areas 
 * @author http://stackoverflow.com/a/9200304
 */
public class CompoundIterator<T> implements Iterator<T> {

    private final Iterator<T>[] iteratorQueue;    
    int q;
    private Iterator<T> current;

    public CompoundIterator(final Iterator<T>... iterators) {
        //this.iteratorQueue = new ArrayDeque<Iterator<T>>();
        this.iteratorQueue = iterators;
        this.q = 0;
        this.current = Collections.<T>emptyList().iterator();
    }

    public boolean hasNext() {
        final boolean curHasNext = current.hasNext();
        if (!curHasNext && !iteratorQueueEmpty()) {
            current = pop();
            return current.hasNext();
        } else {
            return curHasNext;
        }
    }
    private boolean iteratorQueueEmpty() {
        return q < iteratorQueue.length;
    }
    
    private Iterator<T> pop() {
        return iteratorQueue[q++];
    }

    public T next() {
        //TODO eliminate need to call hasNext before next()           
        if (current.hasNext()) {
            return current.next();
        }
        if (!iteratorQueueEmpty()) {
            current = pop();
        }
        return current.next();
    }

    public void remove() {
        throw new UnsupportedOperationException("remove() unsupported");
    }
}