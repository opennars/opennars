package nars.util.data;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Simple object pool implemented by a Deque (ex: ArrayDeque)
 */
abstract public class DequePool<X> implements Pool<X> {

    protected final Deque<X> data;
    private int capacity;

    public DequePool() {
        this(16);
    }

    public DequePool(int initialCapacity) {
        data = new ArrayDeque(initialCapacity);
        setCapacity(initialCapacity);
        //data = new CircularArrayList<>(initialCapacity);

        /*for (int i = 0; i < preallocate; i++)
            put(create());*/
    }

    public void setCapacity(int c) {
        this.capacity = c;
    }

    @Override
    public void put(final X i) {
        //synchronized (data) {


        if (data.size() < capacity)
            data.offer(i);

        //else: it is forgotten

    }

    @Override
    public X get() {
        //synchronized (data) {

        if (data.isEmpty()) return create();

        return data.poll();
        //}
    }

    protected void print() {
        System.out.println(data.size());
    }

    public boolean isEnabled() {
        return capacity != 0;
    }

    @Override
    public void delete() {
        capacity = 0;
        data.clear();
    }
}
