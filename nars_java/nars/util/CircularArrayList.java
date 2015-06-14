package nars.util;

import java.util.AbstractList;
import java.util.Deque;
import java.util.Iterator;
import java.util.RandomAccess;

/* High-performance Circular (Ring) Buffer. Not thread safe, and sacrifices safety for speed in other ways. */
public class CircularArrayList<E> extends AbstractList<E> implements RandomAccess, Deque<E> {
    private final E e = null;
    
    private final int n; // buffer length
    public final Object[] buf;
    private int head = 0;
    private int tail = 0;
    private int size = 0;
  
    public CircularArrayList(Class<E> clazz, int capacity) {
        n = capacity;
        //buf = (E[])Array.newInstance(clazz, capacity);
        buf = new Object[capacity];
    }

    @Override public Iterator<E> iterator() {
        return new Iterator<E>() {
            
            int pos = 0;
            final int max = size;
            
            @Override public boolean hasNext() { return pos < max; }
            @Override public E next() { return get(pos++);  }            
        };
    }

    @Override public Iterator<E> descendingIterator() {
        return new Iterator<E>() {            
            int pos = size-1;
            
            @Override public boolean hasNext() { return pos >= 0; }
            @Override public E next() { return get(pos--);  }
        };
    }
    
    public int capacity() {
        return n - 1;
    }
  
    /*
    private int wrapIndex(final int i) {
        int m = i % n;
        if (m < 0) // java modulus can be negative
            m += n;       
        return m;
    }
    */
  
    // This method is O(n) but will never be called if the
    // CircularArrayList is used in its typical/intended role.
    // TODO use array copy
    private void shiftBlock(final int startIndex, final int endIndex) {
        //assert (endIndex > startIndex);        
        for (int i = endIndex - 1; i >= startIndex; i--) {
            set(i + 1, get(i));
        }
    }
  
    
    @Override
    public int size() {
        return size;
        //return tail - head + (tail < head ? n : 0);
    }
  
    @Override
    public E get(final int i) {
        //same as the original function below but avoid another function call to help guarante inlining
        int m = (head + i) % n;
        //if (m < 0) m += n;        
        return (E)buf[m];
        
        //original code:
        //return buf[wrapIndex(head + i)];
    }
  
    @Override
    public E set(final int i, final E e) {
        /*if (i < 0 || i >= size()) {
            throw new IndexOutOfBoundsException();
        }*/
        //same as the original function below but avoid another function call to help guarante inlining
        int m = (head + i) % n;
        //if (m < 0) m += n;        
                
        E existing = (E)buf[m];
        buf[m] = e;
        return existing;
    }
  
    @Override
    public void add(final int i, final E e) {
        final int s = size;
        /*
        if (s == n - 1) {
            throw new IllegalStateException("Cannot add element."
                    + " CircularArrayList is filled to capacity.");
        }
        if (i < 0 || i > s) {
            throw new IndexOutOfBoundsException();
        }
        */
        if (++tail == n) tail = 0;
        size++;
        
        if (i < s)
            shiftBlock(i, s);
        
        set(i, e);
    }
  
    @Override
    public E remove(final int i) {
        /*final int s = size;        
        if (i < 0 || i >= s) {
            throw new IndexOutOfBoundsException();
        }
        */
        
        E e = get(i);
        if (i > 0)
            shiftBlock(0, i);
                
        if (++head == n) head = 0;
        size--;
        
        return e;
    }

    @Override
    public void addFirst(final E e) {
        add(0, e);
    }

    @Override
    public void addLast(final E e) {
        add(size, e);
    }


    @Override
    public E removeFirst() {
        return remove(0);
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

}