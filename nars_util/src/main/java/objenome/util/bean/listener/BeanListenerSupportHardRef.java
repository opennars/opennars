package objenome.util.bean.listener;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BeanListenerSupportHardRef<T> implements BeanListenerSupport<T> {

    private Collection<T> propertyChangeListeners = new ConcurrentLinkedQueue<>();

    public void add(final T t) {
        this.propertyChangeListeners.add(t);
    }

    public void remove(final T object) {
        this.propertyChangeListeners.remove(object);
    }

    public Iterator<T> iterator() {
        return this.propertyChangeListeners.iterator();
    }

    @Override
    public BeanListenerSupportHardRef<T> clone() throws CloneNotSupportedException {
        @SuppressWarnings("unchecked")
        final BeanListenerSupportHardRef<T> clone = (BeanListenerSupportHardRef<T>) super.clone();
        clone.propertyChangeListeners = new ConcurrentLinkedQueue<>(this.propertyChangeListeners);
        return clone;
    }

}
