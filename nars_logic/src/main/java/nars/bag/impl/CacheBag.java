package nars.bag.impl;

import java.util.function.Consumer;


abstract public class CacheBag<K, V> implements Iterable<V> {


    private Consumer<V> onRemoval;

    public abstract void clear();

    public abstract V get(K key);

    public abstract V remove(K key);

    public abstract void put(V v);

    public abstract int size();

    public void setOnRemoval(Consumer<V> onRemoval) {
        this.onRemoval = onRemoval;
    }

    public Consumer<V> getOnRemoval() {
        return onRemoval;
    }
}
