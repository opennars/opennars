package nars.bag.impl;


import nars.budget.Itemized;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by me on 6/2/15.
 */
abstract public class MapCacheBag<K, V extends Itemized<K>> extends AbstractCacheBag<K,V>  {

    protected final Map<K, V> data;


    public MapCacheBag(Map<K, V> data) {
        super();

        this.data = data;

    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public V get(K key) {
        return data.get(key);
    }

    @Override
    public V remove(K key) {
        return data.remove(key);
    }

    @Override
    public V put(V v) {
        return data.put(v.name(), v);
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Iterator<V> iterator() {
        return data.values().iterator();
    }

}
