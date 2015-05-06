package objenome.util.bean.listener;

public interface BeanListenerSupport<T> extends Iterable<T>, Cloneable {

    void add(T t);

    void remove(T object);

    BeanListenerSupport<T> clone() throws CloneNotSupportedException;

}
