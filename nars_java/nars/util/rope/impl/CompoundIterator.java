
package nars.util.rope.impl;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Iterator;

/**
 *
 * @author http://stackoverflow.com/a/9200304
 */
public class CompoundIterator<T> implements Iterator<T> {

    private final ArrayDeque<Iterator<T>> iteratorQueue;
    private Iterator<T> current;

    public CompoundIterator(final Iterator<T>... iterators) {
        this.iteratorQueue = new ArrayDeque<Iterator<T>>();
        for (final Iterator<T> iterator : iterators) {
            iteratorQueue.push(iterator);
        }
        current = Collections.<T>emptyList().iterator();
    }

    public boolean hasNext() {
        final boolean curHasNext = current.hasNext();
        if (!curHasNext && !iteratorQueue.isEmpty()) {
            current = iteratorQueue.pop();
            return current.hasNext();
        } else {
            return curHasNext;
        }
    }

    public T next() {
        if (current.hasNext()) {
            return current.next();
        }
        if (!iteratorQueue.isEmpty()) {
            current = iteratorQueue.pop();
        }
        return current.next();
    }

    public void remove() {
        throw new UnsupportedOperationException("remove() unsupported");
    }
}