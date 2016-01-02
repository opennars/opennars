package objenome.util.bean.listener;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BeanListenerSupportSoftRef<T> implements BeanListenerSupport<T> {

    private ConcurrentLinkedQueue<Reference<T>> propertyChangeListeners = new ConcurrentLinkedQueue<>();

    private class Itr implements Iterator<T> {

        private final Iterator<Reference<T>> iterator;

        private T prefetched;

        @SuppressWarnings("synthetic-access")
        public Itr() {
            iterator = propertyChangeListeners.iterator();
            prefetch();
        }

        private void prefetch() {
            while (iterator.hasNext()) {
                prefetched = iterator.next().get();
                if (prefetched == null) {
                    iterator.remove();
                } else {
                    return;
                }
            }
            prefetched = null;
        }

        @Override
        public boolean hasNext() {
            return prefetched != null;
        }

        @Override
        public T next() {
            T next = prefetched;
            prefetch();
            return next;
        }

        @Override
        public void remove() {
            iterator.remove();
        }

    }

    @Override
    public void add(T t) {
        propertyChangeListeners.add(new WeakReference<>(t));
    }

    @Override
    public void remove(T object) {
        for (Iterator<T> it = iterator(); it.hasNext();) {
            T listener = it.next();
            // yes! we do want to check reference equality here
            if (listener == object) {
                it.remove();
            }
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    @Override
    public BeanListenerSupportSoftRef<T> clone() throws CloneNotSupportedException {
        @SuppressWarnings("unchecked") BeanListenerSupportSoftRef<T> clone = (BeanListenerSupportSoftRef<T>) super.clone();
        clone.propertyChangeListeners = new ConcurrentLinkedQueue<>(propertyChangeListeners);
        return clone;
    }

}
