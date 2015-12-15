package nars.bag.impl;


import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;


public class MapCacheBag<K,V> extends AbstractCacheBag<K,V>  {

    public final Map<K,V> data;

    public MapCacheBag(Map<K,V> data) {
        this.data = data;
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public V get(Object key) {
        return data.get(key);
    }

    @Override
    public V remove(K key) {
        return data.remove(key);
    }

    @Override
    public V put(K k, V v) {
        return data.put(k, v);
    }

    @Override
    public int size() {
        return data.size();
    }

    public Iterator<V> iterator() {
        return data.values().iterator();
    }

    /** provides a direct method to forEach,
     * in case the map implements a better
     * alternative to iterator()'s default */
    public void forEach(Consumer<? super V> consumer) {
        data.forEach((k,v) -> consumer.accept(v));
    }

}
