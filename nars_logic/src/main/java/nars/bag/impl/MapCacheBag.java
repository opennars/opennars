package nars.bag.impl;


import nars.nal.Itemized;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by me on 6/2/15.
 */
abstract public class MapCacheBag<K, V extends Itemized<K>> extends CacheBag<K,V>  {

    protected final Map<K, V> data;


    public MapCacheBag() {
        super();

        data = newMap();

    }

    abstract public Map<K,V> newMap();

    @Override
    public void clear() {
        //throw new RuntimeException("unable to clear() shared concept bag " + concepts);
    }

    @Override
    public V get(K key) {
        return data.get(key);
    }

    @Override
    public V take(K key) {
        return data.remove(key);
    }

    @Override
    public void put(V v) {
        data.put(v.name(), v);
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public Iterator<V> iterator() {
        return data.values().iterator();
    }

}
