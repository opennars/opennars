package objenome.util.bean.listener;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BeanListenerSupportHardRef<T> implements BeanListenerSupport<T> {

    private Collection<T> propertyChangeListeners = new ConcurrentLinkedQueue<>();

    @Override
    public void add(T t) {
        propertyChangeListeners.add(t);
    }

    @Override
    public void remove(T object) {
        propertyChangeListeners.remove(object);
    }

    @Override
    public Iterator<T> iterator() {
        return propertyChangeListeners.iterator();
    }

    @Override
    public BeanListenerSupportHardRef<T> clone() throws CloneNotSupportedException {
        @SuppressWarnings("unchecked") BeanListenerSupportHardRef<T> clone = (BeanListenerSupportHardRef<T>) super.clone();
        clone.propertyChangeListeners = new ConcurrentLinkedQueue<>(propertyChangeListeners);
        return clone;
    }

}
