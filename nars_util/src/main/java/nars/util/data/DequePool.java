package nars.util.data;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Simple object pool implemented by a Deque (ex: ArrayDeque)
 */
abstract public class DequePool<X> {
    final Deque<X> data;

    public DequePool() {
        data = new ArrayDeque();
    }
    public DequePool(int preallocate) {
        data = new ArrayDeque(preallocate);
        /*for (int i = 0; i < preallocate; i++)
            put(create());*/
    }

    public void put(final X i) {
        data.offer(i);
    }

    public X get() {
        if (data.isEmpty()) return create();
        return data.poll();
    }

    abstract public X create();

}
