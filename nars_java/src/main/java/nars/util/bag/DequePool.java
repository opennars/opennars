package nars.util.bag;

import java.util.ArrayDeque;
import java.util.Deque;

/**
* Created by me on 1/23/15.
*/
abstract public class DequePool<X> {
    final Deque<X> data;

    public DequePool() {
        data = new ArrayDeque();
    }
    public DequePool(int preallocate) {
        data = new ArrayDeque(preallocate);
        for (int i = 0; i < preallocate; i++)
            put(create());
    }

    public void put(X i) {
        data.offer(i);
    }

    public X get() {
        if (data.isEmpty()) return create();
        return data.poll();
    }

    abstract public X create();


}
