package nars.bag.impl;

import com.google.common.cache.RemovalListener;
import nars.Memory;


abstract public class CacheBag<K, V> implements Memory.MemoryAware, Iterable<V> {

    protected Memory memory = null;

    @Override public void setMemory(Memory m) {
        this.memory = m;
    }

    public abstract void clear();

    public abstract V get(K key);

    public abstract V take(K key);

    public abstract void put(V v);

    public abstract long size();

}
