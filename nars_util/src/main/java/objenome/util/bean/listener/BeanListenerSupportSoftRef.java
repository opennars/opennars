package objenome.util.bean.listener;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BeanListenerSupportSoftRef<T> implements BeanListenerSupport<T> {

    private ConcurrentLinkedQueue<Reference<T>> propertyChangeListeners = new ConcurrentLinkedQueue<Reference<T>>();

    private class Itr implements Iterator<T> {

        private final Iterator<Reference<T>> iterator;

        private T prefetched;

        @SuppressWarnings("synthetic-access")
        public Itr() {
            this.iterator = BeanListenerSupportSoftRef.this.propertyChangeListeners.iterator();
            prefetch();
        }

        private void prefetch() {
            while (this.iterator.hasNext()) {
                this.prefetched = this.iterator.next().get();
                if (this.prefetched == null) {
                    this.iterator.remove();
                } else {
                    return;
                }
            }
            this.prefetched = null;
        }

        public boolean hasNext() {
            return this.prefetched != null;
        }

        public T next() {
            final T next = this.prefetched;
            prefetch();
            return next;
        }

        public void remove() {
            this.iterator.remove();
        }

    }

    public void add(final T t) {
        this.propertyChangeListeners.add(new WeakReference<T>(t));
    }

    public void remove(final T object) {
        for (final Iterator<T> it = iterator(); it.hasNext();) {
            final T listener = it.next();
            // yes! we do want to check reference equality here
            if (listener == object) {
                it.remove();
            }
        }
    }

    public Iterator<T> iterator() {
        return new Itr();
    }

    @Override
    public BeanListenerSupportSoftRef<T> clone() throws CloneNotSupportedException {
        @SuppressWarnings("unchecked")
        final BeanListenerSupportSoftRef<T> clone = (BeanListenerSupportSoftRef<T>) super.clone();
        clone.propertyChangeListeners = new ConcurrentLinkedQueue<Reference<T>>(this.propertyChangeListeners);
        return clone;
    }

}
