package nars.bag.impl;

import com.google.common.cache.RemovalListener;
import nars.Memory;
import nars.nal.concept.Concept;


abstract public class CacheBag<K, V> implements Iterable<V> {


    public abstract void clear();

    public abstract V get(K key);

    public abstract V remove(K key);

    public abstract void put(V v);

    public abstract int size();


}
