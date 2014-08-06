package nars.util;

import java.util.AbstractList;
import java.util.Deque;
import java.util.Iterator;
import java.util.RandomAccess;

public class CircularArrayList<E> extends AbstractList<E> implements RandomAccess, Deque<E> {
  
    private final int n; // buffer length
    public Object[] buf;
    private int head = 0;
    private int tail = 0;
  
    public CircularArrayList(int capacity) {
        n = capacity + 1;
        buf = new Object[capacity];
    }
  
    public int capacity() {
        return n - 1;
    }
  
    private int wrapIndex(final int i) {
        int m = i % n;
        if (m < 0) { // java modulus can be negative
            m += n;
        }
        return m;
    }
  
    // This method is O(n) but will never be called if the
    // CircularArrayList is used in its typical/intended role.
    private void shiftBlock(int startIndex, int endIndex) {
        assert (endIndex > startIndex);        
        for (int i = endIndex - 1; i >= startIndex; i--) {
            set(i + 1, get(i));
        }
    }
  
    @Override
    public int size() {
        return tail - head + (tail < head ? n : 0);
    }
  
    @Override
    public E get(int i) {
        return (E)buf[wrapIndex(head + i)];
    }
  
    @Override
    public E set(int i, E e) {
        if (i < 0 || i >= size()) {
            throw new IndexOutOfBoundsException();
        }
        Object existing = buf[wrapIndex(head + i)];
        buf[wrapIndex(head + i)] = e;
        if (existing!=null)
            return (E)existing;
        return null;
    }
  
    @Override
    public void add(int i, E e) {
        int s = size();
        if (s == n - 1) {
            throw new IllegalStateException("Cannot add element."
                    + " CircularArrayList is filled to capacity.");
        }
        if (i < 0 || i > s) {
            throw new IndexOutOfBoundsException();
        }
        tail = wrapIndex(tail + 1);
        if (i < s) {
            shiftBlock(i, s);
        }
        set(i, e);
    }
  
    @Override
    public E remove(int i) {
        int s = size();
        if (i < 0 || i >= s) {
            throw new IndexOutOfBoundsException();
        }
        E e = get(i);
        if (i > 0) {
            shiftBlock(0, i);
        }
        head = wrapIndex(head + 1);
        return e;
    }

    @Override
    public void addFirst(E e) {
        add(0, e);
    }

    @Override
    public void addLast(E e) {
        add(size()-1, e);
    }

    @Override
    public boolean offerFirst(E e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean offerLast(E e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E removeFirst() {
        return remove(0);
    }

    @Override
    public E removeLast() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E getFirst() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E getLast() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E peekFirst() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E peekLast() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean offer(E e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E poll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E element() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E peek() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void push(E e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E pop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<E> descendingIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}